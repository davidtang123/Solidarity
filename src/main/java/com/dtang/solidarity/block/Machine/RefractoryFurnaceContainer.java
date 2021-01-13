package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.init.ModContainers;
import com.dtang.solidarity.init.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: David Tang
 * Date: 1/5/2021
 */
public class RefractoryFurnaceContainer extends Container {

    public static RefractoryFurnaceContainer createContainerServerSide(int windowID, PlayerInventory playerInventory,
                                                                       FurnaceContents furnaceContents,
                                                                       AdvancedFurnaceStateData furnaceStateData) {
        return new RefractoryFurnaceContainer(windowID, playerInventory, furnaceContents, furnaceStateData);
    }

    public static RefractoryFurnaceContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        FurnaceContents furnaceContents = FurnaceContents.createForClientSideContainer(7);
        AdvancedFurnaceStateData furnaceStateData = new AdvancedFurnaceStateData();

        // on the client side there is no parent TileEntity to communicate with, so we:
        // 1) use dummy inventories and furnace state data (tracked ints)
        // 2) use "do nothing" lambda functions for canPlayerAccessInventory and markDirty
        return new RefractoryFurnaceContainer(windowID, playerInventory, furnaceContents, furnaceStateData);
    }

    // must assign a slot index to each of the slots used by the GUI.
    // For this container, we can see the furnace fuel, input, and output slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container using addSlotToContainer(), it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 = fuel slot (furnaceStateData 0)
    //  37 = paper slot (furnaceStateData 1)
    //  38 = gas tank slot (furnaceStateData 2)
    //  39 = input slot (3)
    //  40 = main output slot (4)
    //  41 = ash output slot (5)
    //  42 = gas output slot (6)

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    public static final int GUI_HEIGHT = 166;

    public static final int FURNACE_SLOTS_COUNT = 7;
    public static final int FUEL_INDEX = 0;
    public static final int PAPER_INDEX = 1;
    public static final int GAS_TANK_INDEX = 2;
    public static final int INPUT_INDEX = 3;
    public static final int MAIN_OUTPUT_INDEX = 4;
    public static final int ASH_OUTPUT_INDEX = 5;
    public static final int GAS_OUTPUT_INDEX = 6;

    // slot index is the unique index for all slots in this container i.e. 0 - 35 for invPlayer then 36 - 49 for furnaceContents
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int HOTBAR_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX;
    private static final int PLAYER_INVENTORY_FIRST_SLOT_INDEX = HOTBAR_FIRST_SLOT_INDEX + HOTBAR_SLOT_COUNT;
    private static final int FIRST_FURNACE_SLOT_INDEX = PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT;

    // slot number is the slot number within each component;
    // i.e. invPlayer slots 0 - 35 (hotbar 0 - 8 then main inventory 9 to 35)
    // and furnace: inputZone slots 0 - 4, outputZone slots 0 - 4, fuelZone 0 - 3

    public RefractoryFurnaceContainer(int windowID, PlayerInventory invPlayer,
                                      FurnaceContents furnaceContents,
                                      AdvancedFurnaceStateData furnaceStateData) {
        super(ModContainers.REFRACTORY.get(), windowID);
        if (ModContainers.REFRACTORY.get() == null)
            throw new IllegalStateException("Must initialise containerType.ContainerRefractoryFurnace before constructing a ContainerRefractoryFurnace!");
        this.furnaceContents = furnaceContents;
        this.furnaceStateData = furnaceStateData;
        this.world = invPlayer.player.world;

        trackIntArray(furnaceStateData);    // tell vanilla to keep the AdvancedFurnaceStateData synchronised between client and server Containers

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;
        final int HOTBAR_XPOS = 8;
        final int HOTBAR_YPOS = GUI_HEIGHT-24;
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            addSlot(new Slot(invPlayer, x, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }

        final int PLAYER_INVENTORY_XPOS = 8;
        final int PLAYER_INVENTORY_YPOS = GUI_HEIGHT-82;
        // Add the rest of the players inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(invPlayer, slotNumber,  xpos, ypos));
            }
        }

        addSlot(new SlotFuel(furnaceContents, FUEL_INDEX, 30, 50));
        addSlot(new SlotPaperInput(furnaceContents, PAPER_INDEX, 50, 65));
        addSlot(new SlotSmeltableInput(furnaceContents, GAS_TANK_INDEX, 120, 10));
        addSlot(new SlotSmeltableInput(furnaceContents, INPUT_INDEX, 30, 15));
        addSlot(new SlotOutput(furnaceContents, MAIN_OUTPUT_INDEX, 90, 35));
        addSlot(new SlotOutput(furnaceContents, ASH_OUTPUT_INDEX, 75, 65));
        addSlot(new SlotOutput(furnaceContents, GAS_OUTPUT_INDEX, 145, 10));
    }

    // Checks each tick to make sure the player is still able to access the inventory and if not closes the gui
    @Override
    public boolean canInteractWith(PlayerEntity player)
    {
        return furnaceContents.isUsableByPlayer(player);
    }

    // This is where you specify what happens when a player shift clicks a slot in the gui
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int sourceSlotIndex)
    {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()){
            return ItemStack.EMPTY;
        }
        ItemStack sourceItemStack = sourceSlot.getStack();
        ItemStack sourceStackBeforeMerge = sourceItemStack.copy();
        boolean successfulTransfer = false;

        SlotZone sourceZone = SlotZone.getZoneFromIndex(sourceSlotIndex);

        switch (sourceZone) {
            case FURNACE_ZONE: // Moving from furnace, identify slot.
                int furnaceIndex = sourceSlotIndex - FIRST_FURNACE_SLOT_INDEX;
                switch (furnaceIndex){
                    // taking out of the output zone - try the hotbar first, then main inventory.  fill from the end.
                    case 4: case 5: case 6:
                        successfulTransfer = mergeItemStack(sourceItemStack, HOTBAR_FIRST_SLOT_INDEX,
                                PLAYER_INVENTORY_FIRST_SLOT_INDEX+PLAYER_INVENTORY_SLOT_COUNT+1, true);
                        //if (!successfulTransfer) {
                        //    successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, true);
                        //}
                        if (successfulTransfer) {  // removing from output means we have just crafted an item -> need to inform
                            sourceSlot.onSlotChange(sourceItemStack, sourceStackBeforeMerge);
                        }
                        break;
                    case 0: case 1: case 2: case 3:// any input
                        successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, false);
                        if (!successfulTransfer) {
                            successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceItemStack, false);
                        }
                        break;
                }
                break;
            case PLAYER_HOTBAR:
            case PLAYER_MAIN_INVENTORY: // taking out of inventory - find the appropriate furnace zone
                if(sourceItemStack.getItem() == Items.PAPER){
                    successfulTransfer = mergeIntoFurnaceSlot(PAPER_INDEX, sourceItemStack);
                } else if(sourceItemStack.getItem() == ModItems.GAS_TANK.get()){
                    successfulTransfer = mergeIntoFurnaceSlot(GAS_TANK_INDEX, sourceItemStack);
                }

                if (!RefractoryFurnaceTileEntity.getSmeltingResultsForItem(world, sourceItemStack).isEmpty()) { // smeltable -> add to input
                    successfulTransfer = mergeIntoFurnaceSlot(INPUT_INDEX, sourceItemStack);
                }
                if (!successfulTransfer && RefractoryFurnaceTileEntity.getItemBurnTime(world, sourceItemStack) > 0) { //burnable -> add to fuel from the bottom slot first
                    successfulTransfer = mergeIntoFurnaceSlot(FUEL_INDEX, sourceItemStack);
                }
                if (!successfulTransfer) {  // didn't fit into furnace; try player main inventory or hotbar
                    if (sourceZone == SlotZone.PLAYER_HOTBAR) { // main inventory
                        successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, false);
                    } else {
                        successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceItemStack, false);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("unexpected sourceZone:" + sourceZone);
        }
        if (!successfulTransfer) return ItemStack.EMPTY;

        // If source stack is empty (the entire stack was moved) set slot contents to empty
        if (sourceItemStack.isEmpty()) {
            sourceSlot.putStack(ItemStack.EMPTY);
        } else {
            sourceSlot.onSlotChanged();
        }

        // if source stack is still the same as before the merge, the transfer failed somehow?  not expected.
        if (sourceItemStack.getCount() == sourceStackBeforeMerge.getCount()) {
            return ItemStack.EMPTY;
        }
        sourceSlot.onTake(player, sourceItemStack);
        return sourceStackBeforeMerge;
    }

    /**
     * Try to merge from the given source ItemStack into the given SlotZone.
     * @param destinationZone the zone to merge into
     * @param sourceItemStack the itemstack to merge from
     * @param fillFromEnd if true: try to merge from the end of the zone instead of from the start
     * @return true if a successful transfer occurred
     */
    private boolean mergeInto(SlotZone destinationZone, ItemStack sourceItemStack, boolean fillFromEnd) {
        return mergeItemStack(sourceItemStack, destinationZone.firstIndex, destinationZone.lastIndexPlus1, fillFromEnd);
    }

    private boolean mergeIntoFurnaceSlot(int index, ItemStack sourceItemStack) {
        int trueindex = SlotZone.FURNACE_ZONE.firstIndex + index;
        return mergeItemStack(sourceItemStack, trueindex, trueindex+1, false);//fillFromEnd order shouldn't matter
    }

    // -------- methods used by the ContainerScreen to render parts of the display

    /**
     * Returns the amount of fuel remaining on the currently burning item in the given fuel slot.
     * @return fraction remaining, between 0.0 - 1.0
     */
    public double fractionOfFuelRemaining() {
        if (furnaceStateData.burnTimeRemaining <= 0) return 0;
        double fraction = furnaceStateData.burnTimeRemaining / (double)furnaceStateData.burnTimeInitialValue;
        return MathHelper.clamp(fraction, 0.0, 1.0);
    }

    /**
     * return the remaining burn time of the fuel in the given slot
     * @return seconds remaining
     */
    public int secondsOfFuelRemaining()	{
        if (furnaceStateData.burnTimeRemaining <= 0 ) return 0;
        return furnaceStateData.burnTimeRemaining / 20; // 20 ticks per second
    }

    /**
     * Returns the amount of cook time completed on the currently cooking item.
     * @return fraction remaining, between 0 - 1
     */
    public double fractionOfCookTimeComplete() {
        if (furnaceStateData.cookTimeTotal == 0) return 0;
        double fraction = furnaceStateData.cookTime / (double)furnaceStateData.cookTimeTotal;
        return MathHelper.clamp(fraction, 0.0, 1.0);
    }

    // --------- Customise the different slots (in particular - what items they will accept)


    // SlotFuel is a slot for fuel items
    public class SlotFuel extends Slot {
        public SlotFuel(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean isItemValid(ItemStack stack) {
            return RefractoryFurnaceTileEntity.isItemValidForFuelSlot(stack);
        }
    }

    // SlotSmeltableInput is a slot for input item
    public class SlotSmeltableInput extends Slot {
        public SlotSmeltableInput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean isItemValid(ItemStack stack) {
            return RefractoryFurnaceTileEntity.isItemValidForInputSlot(stack);
        }
    }

    public class SlotPaperInput extends Slot {
        public SlotPaperInput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() == Items.PAPER;
        }
    }

    // SlotOutput is a slot that will not accept any item
    public class SlotOutput extends Slot {
        public SlotOutput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean isItemValid(ItemStack stack) {
            return RefractoryFurnaceTileEntity.isItemValidForOutputSlot(stack);
        }
    }

    private FurnaceContents furnaceContents;
    private AdvancedFurnaceStateData furnaceStateData;

    private World world; //needed for some helper methods
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Helper enum to make the code more readable
     */
    private enum SlotZone {
        FURNACE_ZONE(FIRST_FURNACE_SLOT_INDEX, FURNACE_SLOTS_COUNT),
        PLAYER_MAIN_INVENTORY(PLAYER_INVENTORY_FIRST_SLOT_INDEX, PLAYER_INVENTORY_SLOT_COUNT),
        PLAYER_HOTBAR(HOTBAR_FIRST_SLOT_INDEX, HOTBAR_SLOT_COUNT);

        SlotZone(int firstIndex, int numberOfSlots) {
            this.firstIndex = firstIndex;
            this.slotCount = numberOfSlots;
            this.lastIndexPlus1 = firstIndex + numberOfSlots;
        }

        public final int firstIndex;
        public final int slotCount;
        public final int lastIndexPlus1;

        public static SlotZone getZoneFromIndex(int slotIndex) {
            for (SlotZone slotZone : SlotZone.values()) {
                if (slotIndex >= slotZone.firstIndex && slotIndex < slotZone.lastIndexPlus1) return slotZone;
            }
            throw new IndexOutOfBoundsException("Unexpected slotIndex");
        }
    }
}