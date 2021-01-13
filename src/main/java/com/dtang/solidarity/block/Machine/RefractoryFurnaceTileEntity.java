package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.init.ModItems;
import com.dtang.solidarity.init.ModTileEntities;
import com.dtang.solidarity.util.SetBlockStateFlag;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class RefractoryFurnaceTileEntity extends SolidarityFurnaceTileEntity implements INamedContainerProvider, ITickableTileEntity {
    public static final int TOTAL_SLOTS_COUNT = 7;

    public static final int FUEL_INDEX = 0;
    public static final int PAPER_INDEX = 1;
    public static final int GAS_TANK_INDEX = 2;
    public static final int INPUT_INDEX = 3;
    public static final int MAIN_OUTPUT_INDEX = 4;
    public static final int ASH_OUTPUT_INDEX = 5;
    public static final int GAS_OUTPUT_INDEX = 6;

    public RefractoryFurnaceTileEntity(){
        super(ModTileEntities.REFRACTORY.get());
        furnaceContents = FurnaceContents.createForTileEntity(TOTAL_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::markDirty);
    }

    // This method is called every tick to update the tile entity, i.e.
    // - see if the fuel has run out, and if so turn the furnace "off" and slowly uncook the current item (if any)
    // - see if the current smelting input item has finished smelting; if so, convert it to output
    // - burn fuel slots
    // It runs both on the server and the client but we only need to do updates on the server side.
    @Override
    public void tick() {
        if (world.isRemote) return; // do nothing on client.
        ItemStack currentlySmeltingItem = getCurrentlySmeltingInputItem();

        // if user has changed the input slots, reset the smelting time
        if (!ItemStack.areItemsEqual(currentlySmeltingItem, currentlySmeltingItemLastTick) ||
                //Fuel is not hot enough to burn.
                furnaceStateData.burnTimeInitialValue*4 < getCookTemp(this.world, currentlySmeltingItem)/40) {
            furnaceStateData.cookTime = 0;
        }
        currentlySmeltingItemLastTick = currentlySmeltingItem.copy();

        if (!currentlySmeltingItem.isEmpty()) {
            if (furnaceStateData.burnTimeRemaining > 0) {
                furnaceStateData.burnTimeRemaining--;
            }

            boolean inventoryChanged = false;

            if (furnaceStateData.burnTimeRemaining == 0) {
                ItemStack fuelItemStack = furnaceContents.getStackInSlot(FUEL_INDEX);
                if (!fuelItemStack.isEmpty() && this.getItemBurnTime(this.world, fuelItemStack) > 0) {
                    int burnTimeForItem = this.getItemBurnTime(this.world, fuelItemStack);
                    furnaceStateData.burnTimeRemaining = burnTimeForItem;//Burns 4 times as fast=lasts 1/4 the time
                    furnaceStateData.burnTimeInitialValue = burnTimeForItem;
                    convertToAsh(fuelItemStack);//Add relevant ashes and consume paper

                    furnaceContents.decrStackSize(FUEL_INDEX, 1);
                    inventoryChanged = true;

                    // If the stack size now equals 0 set the slot contents to the item container item. This is for fuel
                    // item such as lava buckets so that the bucket is not consumed. If the item dose not have
                    // a container item, getContainerItem returns ItemStack.EMPTY which sets the slot contents to empty
                    if (fuelItemStack.isEmpty()) {
                        ItemStack containerItem = fuelItemStack.getContainerItem();
                        furnaceContents.setInventorySlotContents(FUEL_INDEX, containerItem);
                    }
                }
            }
            if (inventoryChanged) markDirty();

            // If fuel is available, keep cooking the item, otherwise start "uncooking" it at double speed
            if (isBurning()) {
                furnaceStateData.cookTime+=4;
            }	else {
                furnaceStateData.cookTime -= 2;
                if (furnaceStateData.cookTime < 0)
                    furnaceStateData.cookTime = 0;
            }

            int cookTimeForCurrentItem = getCookTime(this.world, currentlySmeltingItem);
            furnaceStateData.cookTimeTotal = cookTimeForCurrentItem;
            // If cookTime has reached maxCookTime smelt the item and reset cookTime
            if (furnaceStateData.cookTime >= cookTimeForCurrentItem) {
                smeltInputItem();
                furnaceStateData.cookTime = 0;
            }
        }	else {
            furnaceStateData.cookTime = 0;
        }

        // The block update (for renderer) is only required on client side, but the lighting is required on both, since
        //    the client needs it for rendering and the server needs it for crop growth etc
        BlockState currentBlockState = world.getBlockState(this.pos);
        BlockState newBlockState = currentBlockState.with(SolidarityFurnaceBlock.LIT, isBurning());
        if (!newBlockState.equals(currentBlockState)) {
            final int FLAGS = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
            world.setBlockState(this.pos, newBlockState, FLAGS);
            markDirty();
        }
    }

    private void convertToAsh(ItemStack fuelItemStack){
        super.convertToAsh(fuelItemStack, ASH_OUTPUT_INDEX, PAPER_INDEX);
    }

    /**
     * Check if any of the input item are smeltable and there is sufficient space in the output slots
     * @return the ItemStack of the first input item that can be smelted; ItemStack.EMPTY if none
     */
    private ItemStack getCurrentlySmeltingInputItem() {return smeltInputItem(false);}

    /**
     * Smelt an input item into an output slot, if possible
     */
    private void smeltInputItem() {
        smeltInputItem(true);
    }

    /**
     * checks that there is an item to be smelted in one of the input slots and that there is room for the result in the output slots
     * If desired, performs the smelt
     * @param performSmelt if true, perform the smelt.  if false, check whether smelting is possible, but don't change the inventory
     * @return a copy of the ItemStack of the input item smelted or to-be-smelted
     */
    private ItemStack smeltInputItem(boolean performSmelt)
    {
        ItemStack result = ItemStack.EMPTY;
        ItemStack gasresult = ItemStack.EMPTY;

        // finds the first input slot which is smeltable and whose result fits into an output slot (stacking if possible)
        ItemStack itemStackToSmelt = furnaceContents.getStackInSlot(INPUT_INDEX);
        if (!itemStackToSmelt.isEmpty()) {
            result = getSmeltingResultsForItem(this.world, itemStackToSmelt);
            gasresult = getGasResultsForItem(this.world,itemStackToSmelt);
            if ((result.isEmpty() && gasresult.isEmpty())//No real recipe
                    || !willItemStackFit(furnaceContents, MAIN_OUTPUT_INDEX, result)) {
                return ItemStack.EMPTY;
            }
        }

        if(performSmelt) {
            // alter input and output
            furnaceContents.decrStackSize(INPUT_INDEX, 1);
            furnaceContents.increaseStackSize(MAIN_OUTPUT_INDEX, result);

            if(!gasresult.isEmpty()) {
                if (willItemStackFit(furnaceContents, GAS_OUTPUT_INDEX, gasresult)) {
                    //Add the gas to the gas result
                    furnaceContents.decrStackSize(GAS_TANK_INDEX, 1);
                    furnaceContents.increaseStackSize(GAS_OUTPUT_INDEX, gasresult);
                } else {
                    //TODO:Pollute the atmosphere
                    if (gasresult.isItemEqual(new ItemStack(ModItems.GAS_TANK_CO2.get()))){
                        //increase CO2 pollution
                    } else if(gasresult.isItemEqual(new ItemStack(ModItems.GAS_TANK_SO2.get()))){
                        //Increase SO2 pollution
                    } else {
                        //Eventually, will not be a gas so display error
                    }
                }
            }

            markDirty();
        }
        return furnaceContents.getStackInSlot(INPUT_INDEX).copy();
    }

    // returns the smelting result for the given stack. Returns ItemStack.EMPTY if the given stack can not be smelted
    public static ItemStack getSmeltingResultsForItem(World world, ItemStack primaryInput) {
        Optional<RefractoryFurnaceRecipe> matchingRecipe = getMatchingRecipeForInput(world, primaryInput);
        if (!matchingRecipe.isPresent()) return ItemStack.EMPTY;
        return matchingRecipe.get().getRecipeOutput().copy();  // beware! You must deep copy otherwise you will alter the recipe itself
        //should be functionally the same as matchingRecipe.get().getCraftingResult(primaryInput)
    }

    public static ItemStack getGasResultsForItem(World world, ItemStack primaryInput) {
        Optional<RefractoryFurnaceRecipe> matchingRecipe = getMatchingRecipeForInput(world, primaryInput);
        if (!matchingRecipe.isPresent()) return ItemStack.EMPTY;
        return matchingRecipe.get().getGasResult();
    }

    // gets the recipe which matches the given input, or Missing if none.
    public static Optional<RefractoryFurnaceRecipe> getMatchingRecipeForInput(World world, ItemStack primaryInput) {
        RecipeManager recipeManager = world.getRecipeManager();
        Inventory singleItemInventory = new Inventory(primaryInput);
        return recipeManager.getRecipe(RefractoryFurnaceRecipe.TYPE, singleItemInventory, world);
    }

    /**
     * Gets the cooking time for this recipe input
     * @param world
     * @param itemStack the input item to be smelted
     * @return cooking time (ticks) or 0 if no matching recipe
     */
    public static int getCookTime(World world, ItemStack itemStack) {
        Optional<RefractoryFurnaceRecipe> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(RefractoryFurnaceRecipe::getCookTime).orElse(0);
        //Not quite sure what the above line means, but my environment tells me it's functionally the same as:
        //if (!matchingRecipe.isPresent()) return 0;
        //return matchingRecipe.get().getCookTime();
    }/**
     * Gets the cooking time for this recipe input
     * @param world
     * @param itemStack the input item to be smelted
     * @return cooking time (ticks) or 0 if no matching recipe
     */
    public static int getCookTemp(World world, ItemStack itemStack) {
        Optional<RefractoryFurnaceRecipe> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(RefractoryFurnaceRecipe::getCookTemperature).orElse(0);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    public static boolean isItemValidForFuelSlot(ItemStack itemStack)
    {
        return (FurnaceTileEntity.isFuel(itemStack) && net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack) >= 1200);//Must be at least as efficient as logs
    }

    // returns the number of ticks the given item will burn. Returns 0 if the given item is not a valid fuel
    public static int getItemBurnTime(World world, ItemStack stack)
    {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack)/4;//Everything burns at 4x the speed
    }

    //Write is in SolidarityFurnaceTileEntity

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void read(CompoundNBT nbtTagCompound)
    {
        super.read(nbtTagCompound); // The super call is required to save and load the tile's location

        furnaceStateData.readFromNBT(nbtTagCompound);

        CompoundNBT inventoryNBT = nbtTagCompound.getCompound(FURNACE_SLOTS_NBT);
        furnaceContents.deserializeNBT(inventoryNBT);

        if (furnaceContents.getSizeInventory() != TOTAL_SLOTS_COUNT
        )
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
    }

    // -------------  The following two methods are used to make the TileEntity perform as a NamedContainerProvider, i.e.
    //  1) Provide a name used when displaying the container, and
    //  2) Creating an instance of container on the server, and linking it to the inventory items stored within the TileEntity

    /**
     *  standard code to look up what the human-readable name is.
     *  Can be useful when the tileentity has a customised name (eg "David's footlocker")
     */
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("Refractory Furnace");
    }

    /**
     * The name is misleading; createMenu has nothing to do with creating a Screen, it is used to create the Container on the server only
     * @param windowID
     * @param playerInventory
     * @param playerEntity
     * @return
     */
    @Nullable
    @Override
    public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return RefractoryFurnaceContainer.createContainerServerSide(windowID, playerInventory,
                furnaceContents, furnaceStateData);
    }

    private ItemStack currentlySmeltingItemLastTick = ItemStack.EMPTY;
}
