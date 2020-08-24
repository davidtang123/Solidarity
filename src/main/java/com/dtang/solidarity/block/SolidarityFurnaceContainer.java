package com.dtang.solidarity.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SolidarityFurnaceContainer extends AbstractFurnaceContainer {
    private final IInventory furnaceInventory;
    private final IIntArray furnaceData;
    protected final World world;
    private final IRecipeType<? extends AbstractCookingRecipe> recipeType;

    protected SolidarityFurnaceContainer(ContainerType<?> type, IRecipeType<? extends AbstractCookingRecipe> recipeType, int p_i50103_3_, PlayerInventory playerInventory) {
        this(type, recipeType, p_i50103_3_, playerInventory, new Inventory(7), new IntArray(4));
    }

    //Copied from decompiled MC source. A lot of this is guesswork
    protected SolidarityFurnaceContainer(ContainerType<?> type, IRecipeType<? extends AbstractCookingRecipe> recipeType, int p_i50104_3_, PlayerInventory playerInventory, IInventory fInventory, IIntArray fData) {
        super(type, recipeType, p_i50104_3_, playerInventory, fInventory, fData);
        this.recipeType = recipeType;
        assertInventorySize(fInventory, 7);
        assertIntArraySize(fData, 5);
        this.furnaceInventory = fInventory;
        this.furnaceData = fData;
        this.world = playerInventory.player.world;
        //I think the arguments are the source container, index of slot in container, x coord in pixels, y coord in pixels
        this.addSlot(new Slot(fInventory, 0, 56, 17));
        this.addSlot(new FurnaceFuelSlot((AbstractFurnaceContainer) this, fInventory, 1, 56, 53));
        this.addSlot(new FurnaceResultSlot(playerInventory.player, fInventory, 2, 116, 35));
        //this.addSlot(new BottleSlot())

        //I think this makes the slots for the player's inventory
        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.trackIntArray(fData);
    }

    public void fillStackedContents(RecipeItemHelper p_201771_1_) {
        if (this.furnaceInventory instanceof IRecipeHelperPopulator) {
            ((IRecipeHelperPopulator)this.furnaceInventory).fillStackedContents(p_201771_1_);
        }

    }

    public void clear() {
        this.furnaceInventory.clear();
    }

    /*
    No idea what this does

    public void func_217056_a(boolean p_217056_1_, IRecipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
        (new ServerRecipePlacerFurnace(this)).place(p_217056_3_, p_217056_2_, p_217056_1_);
    }
    */
    public boolean matches(IRecipe<? super IInventory> fInventory) {
        return fInventory.matches(this.furnaceInventory, this.world);
    }

    public int getOutputSlot() {
        return 2;
    }

    public int getWidth() {
        return 1;
    }

    public int getHeight() {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 4;
    }

    public boolean canInteractWith(PlayerEntity player) {
        return this.furnaceInventory.isUsableByPlayer(player);
    }

    public ItemStack transferStackInSlot(PlayerEntity player, int slotIdx) {
        ItemStack curstack = ItemStack.EMPTY;
        Slot curSlot = (Slot)this.inventorySlots.get(slotIdx);
        if (curSlot != null && curSlot.getHasStack()) {
            ItemStack slotContents = curSlot.getStack();
            curstack = slotContents.copy();
            if (slotIdx == 2) {
                if (!this.mergeItemStack(slotContents, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                curSlot.onSlotChange(slotContents, curstack);
            } else if (slotIdx != 1 && slotIdx != 0) {
                if (this.func_217057_a(slotContents)) {
                    if (!this.mergeItemStack(slotContents, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(slotContents)) {
                    if (!this.mergeItemStack(slotContents, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIdx >= 3 && slotIdx < 30) {
                    if (!this.mergeItemStack(slotContents, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIdx >= 30 && slotIdx < 39 && !this.mergeItemStack(slotContents, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotContents, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (slotContents.isEmpty()) {
                curSlot.putStack(ItemStack.EMPTY);
            } else {
                curSlot.onSlotChanged();
            }

            if (slotContents.getCount() == curstack.getCount()) {
                return ItemStack.EMPTY;
            }

            curSlot.onTake(player, slotContents);
        }

        return curstack;
    }

    //can cook?
    protected boolean func_217057_a(ItemStack p_217057_1_) {
        return this.world.getRecipeManager().getRecipe(this.recipeType, new Inventory(new ItemStack[]{p_217057_1_}), this.world).isPresent();
    }

    protected boolean isFuel(ItemStack itemStack) {
        //return itemStack.getItem() == Items.CROSSBOW;
        return AbstractFurnaceTileEntity.isFuel(itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    public int getCookProgressionScaled() {
        int cookedProgress = this.furnaceData.get(2);
        int totalProgress = this.furnaceData.get(3);
        return totalProgress != 0 && cookedProgress != 0 ? cookedProgress * 24 / totalProgress : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBurnLeftScaled() {
        int burnLeft = this.furnaceData.get(1);
        if (burnLeft == 0) {
            burnLeft = 200;
        }

        return this.furnaceData.get(0) * 13 / burnLeft;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean func_217061_l() {
        return this.furnaceData.get(0) > 0;
    }
}
