package com.dtang.solidarity.block.Machine;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Predicate;

/**
 * Created by David Tang on 1/6/2021.
*/
public class FurnaceContents implements IInventory {

    /**
     * @param size  the max number of ItemStacks in the inventory
     * @param canPlayerAccessInventoryLambda the function that the container should call in order to decide if the given player
     *                                       can access the container's contents not.  Usually, this is a check to see
     *                                       if the player is closer than 8 blocks away.
     * @param markDirtyNotificationLambda  the function that the container should call in order to tell the parent TileEntity
     *                                     that the contents of its inventory have been changed and need to be saved.  Usually,
     *                                     this is TileEntity::markDirty
     * @return the new ChestContents.
     */
    public static FurnaceContents createForTileEntity(int size,
                                                      Predicate<PlayerEntity> canPlayerAccessInventoryLambda,
                                                      Notify markDirtyNotificationLambda) {
        return new FurnaceContents(size, canPlayerAccessInventoryLambda, markDirtyNotificationLambda);
    }

    /**
     * @param size  the max number of ItemStacks in the inventory
     * @return the new ChestContents
     */
    public static FurnaceContents createForClientSideContainer(int size) {
        return new FurnaceContents(size);
    }

    /**
     * Writes the chest contents to a CompoundNBT tag (used to save the contents to disk)
     * @return the tag containing the contents
     */
    public CompoundNBT serializeNBT()  {
        return furnaceComponentContents.serializeNBT();
    }

    /**
     * Fills the chest contents from the nbt; resizes automatically to fit.  (used to load the contents from disk)
     * @param nbt
     */
    public void deserializeNBT(CompoundNBT nbt)   {
        furnaceComponentContents.deserializeNBT(nbt);
    }

    public void setCanPlayerAccessInventoryLambda(Predicate<PlayerEntity> canPlayerAccessInventoryLambda) {
        this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
    }
    public void setMarkDirtyNotificationLambda(Notify markDirtyNotificationLambda) {
        this.markDirtyNotificationLambda = markDirtyNotificationLambda;
    }
    public void setOpenInventoryNotificationLambda(Notify openInventoryNotificationLambda) {
        this.openInventoryNotificationLambda = openInventoryNotificationLambda;
    }
    public void setCloseInventoryNotificationLambda(Notify closeInventoryNotificationLambda) {
        this.closeInventoryNotificationLambda = closeInventoryNotificationLambda;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccessInventoryLambda.test(player);  // on the client, this does nothing. on the server, ask our parent TileEntity.
    }
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return furnaceComponentContents.isItemValid(index, stack);
    }

    @FunctionalInterface
    public interface Notify {   // Some folks use Runnable, but I prefer not to use it for non-thread-related tasks
        void invoke();
    }
    @Override
    public void markDirty() {
        markDirtyNotificationLambda.invoke();
    }
    @Override
    public void openInventory(PlayerEntity player) {
        openInventoryNotificationLambda.invoke();
    }
    @Override
    public void closeInventory(PlayerEntity player) {
        closeInventoryNotificationLambda.invoke();
    }

    @Override
    public int getSizeInventory() {
        return furnaceComponentContents.getSlots();
    }
    @Override
    public boolean isEmpty() {
        for (int i = 0; i < furnaceComponentContents.getSlots(); ++i) {
            if (!furnaceComponentContents.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }
    @Override
    public ItemStack getStackInSlot(int index) {
        return furnaceComponentContents.getStackInSlot(index);
    }
    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (count < 0) throw new IllegalArgumentException("count should be >= 0:" + count);
        return furnaceComponentContents.extractItem(index, count, false);
    }
    @Override
    public ItemStack removeStackFromSlot(int index) {
        int maxPossibleItemStackSize = furnaceComponentContents.getSlotLimit(index);
        return furnaceComponentContents.extractItem(index, maxPossibleItemStackSize, false);
    }
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        furnaceComponentContents.setStackInSlot(index, stack);
    }
    @Override
    public void clear() {
        for (int i = 0; i < furnaceComponentContents.getSlots(); ++i) {
            furnaceComponentContents.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    //--------- useful functions that aren't in IInventory but are useful anyway

    /**
     *  Tries to insert the given ItemStack into the given slot.
     * @param index the slot to insert into
     * @param itemStackToInsert the itemStack to insert.  Is not mutated by the function.
     * @return if successful insertion: ItemStack.EMPTY.  Otherwise, the leftover itemstack
     *         (eg if ItemStack has a size of 23, and only 12 will fit, then ItemStack with a size of 11 is returned
     */
    public ItemStack increaseStackSize(int index, ItemStack itemStackToInsert) {
        return furnaceComponentContents.insertItem(index, itemStackToInsert, false);
    }

    /**
     *  Checks if the given slot will accept all of the given itemStack
     * @param index the slot to insert into
     * @param itemStackToInsert the itemStack to insert
     * @return if successful insertion: ItemStack.EMPTY.  Otherwise, the leftover itemstack
     *         (eg if ItemStack has a size of 23, and only 12 will fit, then ItemStack with a size of 11 is returned
     */
    public boolean doesItemStackFit(int index, ItemStack itemStackToInsert) {
        ItemStack leftoverItemStack = furnaceComponentContents.insertItem(index, itemStackToInsert, true);
        return leftoverItemStack.isEmpty();
    }

    // ---------

    private FurnaceContents(int size) {
        this.furnaceComponentContents = new ItemStackHandler(size);
    }

    private FurnaceContents(int size, Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
        this.furnaceComponentContents = new ItemStackHandler(size);
        this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
        this.markDirtyNotificationLambda = markDirtyNotificationLambda;
    }

    // the function that the container should call in order to decide if the
    // given player can access the container's Inventory or not.  Only valid server side
    //  default is "true".
    private Predicate<PlayerEntity> canPlayerAccessInventoryLambda = x-> true;

    // the function that the container should call in order to tell the parent TileEntity that the
    // contents of its inventory have been changed.
    // default is "do nothing"
    private Notify markDirtyNotificationLambda = ()->{};

    // the function that the container should call in order to tell the parent TileEntity that the
    // container has been opened by a player (eg so that the chest can animate its lid being opened)
    // default is "do nothing"
    private Notify openInventoryNotificationLambda = ()->{};

    // the function that the container should call in order to tell the parent TileEntity that the
    // container has been closed by a player
    // default is "do nothing"
    private Notify closeInventoryNotificationLambda = ()->{};

    private final ItemStackHandler furnaceComponentContents;
}
