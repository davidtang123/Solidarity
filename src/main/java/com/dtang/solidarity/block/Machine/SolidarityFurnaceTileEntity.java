package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.block.exampleitems.TileEntityInventoryBasic;
import com.dtang.solidarity.init.ModItems;
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
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class SolidarityFurnaceTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {

    protected FurnaceContents furnaceContents;

    protected final AdvancedFurnaceStateData furnaceStateData = new AdvancedFurnaceStateData();

    public SolidarityFurnaceTileEntity(TileEntityType<?> entity){
        super(entity);
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

    protected boolean isBurning() {
        return furnaceStateData.burnTimeRemaining > 0;
    }

    // This method is called every tick to update the tile entity, i.e.
    // - see if the fuel has run out, and if so turn the furnace "off" and slowly uncook the current item (if any)
    // - see if the current smelting input item has finished smelting; if so, convert it to output
    // - burn fuel slots
    // It runs both on the server and the client but we only need to do updates on the server side.
    @Override
    public abstract void tick();

    protected void convertToAsh(ItemStack fuelItemStack, int ASH_OUTPUT_INDEX, int PAPER_INDEX){
        ItemStack ashOutput = ItemStack.EMPTY;
        if(fuelItemStack.isItemEqual(new ItemStack(Items.COAL_BLOCK))){
            ashOutput = new ItemStack(ModItems.FLY_ASH.get(), 9);
        } else if(fuelItemStack.isItemEqual(new ItemStack(Items.COAL))){
            ashOutput = new ItemStack(ModItems.FLY_ASH.get());
        } else if(fuelItemStack.isItemEqual(new ItemStack(Items.DRIED_KELP_BLOCK))) {
            ashOutput = new ItemStack(ModItems.SODA_ASH.get());
        //} else if(){//Beech wood
          //  Beech wood ash
        } else if(fuelItemStack.isItemEqual(new ItemStack(Items.OAK_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_OAK_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.DARK_OAK_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_DARK_OAK_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BIRCH_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_BIRCH_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.SPRUCE_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_SPRUCE_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.ACACIA_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_ACACIA_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.JUNGLE_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.STRIPPED_JUNGLE_LOG)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.CHARCOAL))
        ) {
            ashOutput = new ItemStack(ModItems.WOOD_ASH.get());
            //All other vanilla fuels should give wood ash
        }/* else if(fuelItemStack.isItemEqual(new ItemStack(Items.LAVA_BUCKET)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BLAZE_ROD)) ||
                //Any wool TODO: There should be a better way to do this...
                fuelItemStack.isItemEqual(new ItemStack(Items.WHITE_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.LIGHT_GRAY_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.GRAY_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.BLACK_WOOL)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BROWN_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.RED_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.ORANGE_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.YELLOW_WOOL)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.LIME_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.GREEN_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.CYAN_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.LIGHT_BLUE_WOOL)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BLUE_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.PURPLE_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.MAGENTA_WOOL)) || fuelItemStack.isItemEqual(new ItemStack(Items.PINK_WOOL)) ||
                //Any carpet
                fuelItemStack.isItemEqual(new ItemStack(Items.WHITE_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.LIGHT_GRAY_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.GRAY_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.BLACK_CARPET)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BROWN_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.RED_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.ORANGE_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.YELLOW_CARPET)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.LIME_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.GREEN_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.CYAN_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.LIGHT_BLUE_CARPET)) ||
                fuelItemStack.isItemEqual(new ItemStack(Items.BLUE_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.PURPLE_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.MAGENTA_CARPET)) || fuelItemStack.isItemEqual(new ItemStack(Items.PINK_CARPET))
        ) {
            //Nothing here, means shouldn't produce any ash
        }*/ else {
        }


        //TODO: Pollute atmosphere from fuel
        //if(fuelItemStack.isItemEqual(ModItems.)){
        //  //Pollute SO2
        //} else {
        //  //Pollute CO2
        //}

        ItemStack ashSlot = furnaceContents.getStackInSlot(ASH_OUTPUT_INDEX);
        ItemStack paperSlot = furnaceContents.getStackInSlot(PAPER_INDEX);
        if(paperSlot.isEmpty() || (!ashSlot.isEmpty() && !ashSlot.isItemEqual(ashOutput))){
            return;//No paper, or already a different ash there
        } else if(paperSlot.getCount() < ashOutput.getCount()) {//Not enough paper for the output
            ashOutput.setCount(paperSlot.getCount());//set it to as much as will fit
        }

        //Stuff as many as will fit
        ItemStack leftovers = furnaceContents.increaseStackSize(ASH_OUTPUT_INDEX, ashOutput);
        //Subtract paper excluding the overflow
        furnaceContents.decrStackSize(PAPER_INDEX, ashOutput.getCount() - leftovers.getCount());
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

    // returns the number of ticks the given item will burn. Returns 0 if the given item is not a valid fuel
    public static int getItemBurnTime(World world, ItemStack stack)
    {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    public static boolean isItemValidForFuelSlot(ItemStack itemStack)
    {
        return FurnaceTileEntity.isFuel(itemStack);
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
    protected final String FURNACE_SLOTS_NBT = "furnaceSlots";

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
    public void read(CompoundNBT nbtTagCompound){
        super.read(nbtTagCompound);
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
    public abstract ITextComponent getDisplayName();

    /**
     * The name is misleading; createMenu has nothing to do with creating a Screen, it is used to create the Container on the server only
     * @param windowID
     * @param playerInventory
     * @param playerEntity
     * @return
     */
    @Nullable
    @Override
    public abstract Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity);
}
