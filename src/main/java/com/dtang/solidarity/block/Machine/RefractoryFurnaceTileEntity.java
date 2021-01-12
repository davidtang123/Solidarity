package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.init.ModTileEntities;
import com.dtang.solidarity.util.SetBlockStateFlag;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class RefractoryFurnaceTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
    public static final int TOTAL_SLOTS_COUNT = 7;

    public static final int FUEL_INDEX = 0;
    public static final int PAPER_INDEX = 1;
    public static final int GAS_TANK_INDEX = 2;
    public static final int INPUT_INDEX = 3;
    public static final int MAIN_OUTPUT_INDEX = 4;
    public static final int ASH_OUTPUT_INDEX = 5;
    public static final int GAS_OUTPUT_INDEX = 6;

    private FurnaceContents furnaceContents;
    private ItemStack potentialGasOutput;

    private final AdvancedFurnaceStateData furnaceStateData = new AdvancedFurnaceStateData();

    public RefractoryFurnaceTileEntity(){
        super(ModTileEntities.REFRACTORY.get());
        furnaceContents = FurnaceContents.createForTileEntity(TOTAL_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::markDirty);
    }

    // Return true if the given player is able to use this block. In this case it checks that
    // 1) the world tileentity hasn't been replaced in the meantime, and
    // 2) the player isn't too far away from the centre of the block
    public boolean canPlayerAccessInventory(PlayerEntity player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        final double X_CENTRE_OFFSET = 0.5;
        final double Y_CENTRE_OFFSET = 0.5;
        final double Z_CENTRE_OFFSET = 0.5;
        final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;

        return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET, pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
    }

    private boolean isBurning() {
        return furnaceStateData.burnTimeRemaining > 0;
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
        if (!ItemStack.areItemsEqual(currentlySmeltingItem, currentlySmeltingItemLastTick)) {  // == and != don't work!
            furnaceStateData.cookTime = 0;
        }
        currentlySmeltingItemLastTick = currentlySmeltingItem.copy();

        if (!currentlySmeltingItem.isEmpty()) {
            if (furnaceStateData.burnTimeRemaining > 0) {
                --furnaceStateData.burnTimeRemaining;
            }

            boolean inventoryChanged = false;

            if (furnaceStateData.burnTimeRemaining == 0) {
                ItemStack fuelItemStack = furnaceContents.getStackInSlot(FUEL_INDEX);
                if (!fuelItemStack.isEmpty() && getItemBurnTime(this.world, fuelItemStack) > 0) {
                    int burnTimeForItem = getItemBurnTime(this.world, fuelItemStack);
                    furnaceStateData.burnTimeRemaining = burnTimeForItem;
                    furnaceStateData.burnTimeInitialValue = burnTimeForItem;
                    //If paper is in the paper slot and ash output is not empty/is same item, add the relevant ash item to the slot
                    ItemStack paperItemStack = furnaceContents.getStackInSlot(PAPER_INDEX);
                    //TODO: Implement ash system

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
                furnaceStateData.cookTime++;
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
        BlockState newBlockState = currentBlockState.with(RefractoryFurnaceBlockInventory.LIT, isBurning());
        if (!newBlockState.equals(currentBlockState)) {
            final int FLAGS = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
            world.setBlockState(this.pos, newBlockState, FLAGS);
            markDirty();
        }
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

        // finds the first input slot which is smeltable and whose result fits into an output slot (stacking if possible)
        ItemStack itemStackToSmelt = furnaceContents.getStackInSlot(INPUT_INDEX);
        if (!itemStackToSmelt.isEmpty()) {
            result = getSmeltingResultsForItem(this.world, itemStackToSmelt);
            if (/*result.isEmpty() ||*/ !willItemStackFit(furnaceContents, MAIN_OUTPUT_INDEX, result)) {
                return ItemStack.EMPTY;
            }
        }

        if(performSmelt) {
            // alter input and output
            furnaceContents.decrStackSize(INPUT_INDEX, 1);
            furnaceContents.increaseStackSize(MAIN_OUTPUT_INDEX, result);

            //Note, result is overwritten and now represents the gas output
            result = getGasResultsForItem(this.world, itemStackToSmelt);
            ItemStack slotToBeOutput = furnaceContents.getStackInSlot(ASH_OUTPUT_INDEX);

            int numToTransfer = 0;//Number of ash to store
            if(slotToBeOutput.isEmpty() || furnaceContents.getStackInSlot(ASH_OUTPUT_INDEX).isItemEqual(result)){
                int currentlythere = slotToBeOutput.getCount();//Number of items already there

                //Same item but won't fit,
                if(!willItemStackFit(furnaceContents, ASH_OUTPUT_INDEX, result) ){
                    //Get the number to transfer

                    //Same item but won't fit
                    //Fill what we can, the rest goes into the atmosphere

                }
            }

            markDirty();
        }
        return furnaceContents.getStackInSlot(INPUT_INDEX).copy();
    }

    /**
     * Will the given ItemStack fully fit into the target slot?
     * @param furnaceContents
     * @param slotIndex
     * @param itemStackOrigin
     * @return true if the given ItemStack will fit completely; false otherwise
     */
    public boolean willItemStackFit(FurnaceContents furnaceContents, int slotIndex, ItemStack itemStackOrigin) {
        ItemStack itemStackDestination = furnaceContents.getStackInSlot(slotIndex);

        if (itemStackDestination.isEmpty() || itemStackOrigin.isEmpty()) {
            return true;
        }

        if (!itemStackOrigin.isItemEqual(itemStackDestination)) {
            return false;
        }

        int sizeAfterMerge = itemStackDestination.getCount() + itemStackOrigin.getCount();
        return sizeAfterMerge <= furnaceContents.getInventoryStackLimit() && sizeAfterMerge <= itemStackDestination.getMaxStackSize();
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

    // returns the number of ticks the given item will burn. Returns 0 if the given item is not a valid fuel
    public static int getItemBurnTime(World world, ItemStack stack)
    {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack);
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
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the fuel slots
    static public boolean isItemValidForFuelSlot(ItemStack itemStack)
    {
        return true;
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForInputSlot(ItemStack itemStack)
    {
        return true;
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    static public boolean isItemValidForOutputSlot(ItemStack itemStack)
    {
        return false;
    }

    //------------------------------
    private final String FURNACE_SLOTS_NBT = "furnaceSlots";

    // This is where you save any data that you don't want to lose when the tile entity unloads
    // In this case, it saves the state of the furnace (burn time etc) and the itemstacks stored in the fuel, input, and output slots
    @Override
    public CompoundNBT write(CompoundNBT parentNBTTagCompound)
    {
        super.write(parentNBTTagCompound); // The super call is required to save and load the tile's location

        furnaceStateData.putIntoNBT(parentNBTTagCompound);
        parentNBTTagCompound.put(FURNACE_SLOTS_NBT, furnaceContents.serializeNBT());

        /*
        * How many times the recipe has been used
        parentNBTTagCompound.putShort("RecipesUsedSize", (short)this.field_214022_n.size());
        int i = 0;

        for(Iterator var3 = this.field_214022_n.entrySet().iterator(); var3.hasNext(); ++i) {
            Map.Entry<ResourceLocation, Integer> entry = (Map.Entry)var3.next();
            parentNBTTagCompound.putString("RecipeLocation" + i, ((ResourceLocation)entry.getKey()).toString());
            parentNBTTagCompound.putInt("RecipeAmount" + i, (Integer)entry.getValue());
        }
*/
        return parentNBTTagCompound;
    }

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

    //	// When the world loads from disk, the server needs to send the TileEntity information to the client
//	//  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT updateTagDescribingTileEntityState = getUpdateTag();
        final int METADATA = 42; // arbitrary.
        return new SUpdateTileEntityPacket(this.pos, METADATA, updateTagDescribingTileEntityState);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT updateTagDescribingTileEntityState = pkt.getNbtCompound();
        handleUpdateTag(updateTagDescribingTileEntityState);
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
       Warning - although our getUpdatePacket() uses this method, vanilla also calls it directly, so don't remove it.
     */
    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) { read(tag); }

    /**
     * When this tile entity is destroyed, drop all of its contents into the world
     * @param world
     * @param blockPos
     */
    public void dropAllContents(World world, BlockPos blockPos) {
        InventoryHelper.dropInventoryItems(world, blockPos, furnaceContents);
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
