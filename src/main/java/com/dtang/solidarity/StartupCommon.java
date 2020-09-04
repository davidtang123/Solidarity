package com.dtang.solidarity;

import com.dtang.solidarity.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * The Startup class for this example is called during startup
 *  See MinecraftByExample class for more information
 */
public class StartupCommon
{
    public static Block blockInventoryBasic;  // this holds the unique instance of your block
    public static BlockItem itemBlockInventoryBasic; // and the corresponding item form that block
    public static TileEntityType<TileEntityInventoryBasic> tileEntityTypeMBE30;  // Holds the type of our tile entity; needed for the TileEntityData constructor
    public static ContainerType<ContainerBasic> containerTypeContainerBasic;

    public static Block blockFurnace;  // this holds the unique instance of your block
    public static BlockItem itemBlockFurnace; // and the corresponding item form that block
    public static TileEntityType<TileEntityFurnace> tileEntityTypeMBE31;  // Holds the type of our tile entity; needed for the TileEntityData constructor
    public static ContainerType<ContainerFurnace> containerTypeContainerFurnace;

    public static Block limestone;
    public static BlockItem itemLimestone;


    public static Item itemSlag;


    @SubscribeEvent
    public static void onBlocksRegistration(final RegistryEvent.Register<Block> blockRegisterEvent) {
        blockInventoryBasic = new BlockInventoryBasic().setRegistryName("mbe30_block_registry_name");
        blockRegisterEvent.getRegistry().register(blockInventoryBasic);
        blockFurnace = new BlockInventoryFurnace().setRegistryName("mbe31_block_inventory_furnace_registry_name");
        blockRegisterEvent.getRegistry().register(blockFurnace);
        limestone = new BlockSimple(0.6f, 10.0f, 1, ToolType.PICKAXE).setRegistryName("limestone");
        blockRegisterEvent.getRegistry().register(limestone);
    }

    @SubscribeEvent
    public static void onItemsRegistration(final RegistryEvent.Register<Item> itemRegisterEvent) {
        // We need to create a BlockItem so the player can carry this block in their hand and it can appear in the inventory
        final int MAXIMUM_STACK_SIZE = 1;  // player can only hold 1 of this block in their hand at once

        Item.Properties PropertiesBuildBlock = new Item.Properties()
                .maxStackSize(MAXIMUM_STACK_SIZE)
                .group(ItemGroup.BUILDING_BLOCKS);
        Item.Properties PropertiesMisc = new Item.Properties()
                .maxStackSize(MAXIMUM_STACK_SIZE)
                .group(ItemGroup.MISC);

        itemBlockInventoryBasic = new BlockItem(blockInventoryBasic, PropertiesBuildBlock);
        itemBlockInventoryBasic.setRegistryName(blockInventoryBasic.getRegistryName());
        itemRegisterEvent.getRegistry().register(itemBlockInventoryBasic);

        itemBlockFurnace = new BlockItem(blockFurnace, PropertiesBuildBlock);
        itemBlockFurnace.setRegistryName(blockFurnace.getRegistryName());
        itemRegisterEvent.getRegistry().register(itemBlockFurnace);

        itemLimestone = new BlockItem(limestone, PropertiesBuildBlock);
        itemLimestone.setRegistryName(limestone.getRegistryName());
        itemRegisterEvent.getRegistry().register(itemLimestone);

        itemSlag = new Item(PropertiesMisc);
        itemSlag.setRegistryName("slag");
        itemRegisterEvent.getRegistry().register(itemSlag);
    }

    @SubscribeEvent
    public static void onTileEntityTypeRegistration(final RegistryEvent.Register<TileEntityType<?>> event) {
        tileEntityTypeMBE30 = TileEntityType.Builder.create(TileEntityInventoryBasic::new, blockInventoryBasic)
                .build(null);
        // you probably don't need a datafixer --> null should be fine
        tileEntityTypeMBE30.setRegistryName("solidarity:mbe30_tile_entity_type_registry_name");
        event.getRegistry().register(tileEntityTypeMBE30);

        tileEntityTypeMBE31 = TileEntityType.Builder.create(TileEntityFurnace::new, blockFurnace)
                .build(null);
        // you probably don't need a datafixer --> null should be fine
        tileEntityTypeMBE31.setRegistryName("solidarity:mbe31_tile_entity_type_registry_name");
        event.getRegistry().register(tileEntityTypeMBE31);
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        containerTypeContainerBasic = IForgeContainerType.create(ContainerBasic::createContainerClientSide);
        containerTypeContainerBasic.setRegistryName("mbe30_container_registry_name");
        event.getRegistry().register(containerTypeContainerBasic);

        containerTypeContainerFurnace = IForgeContainerType.create(ContainerFurnace::createContainerClientSide);
        containerTypeContainerFurnace.setRegistryName("mbe31_container_registry_name");
        event.getRegistry().register(containerTypeContainerFurnace);
    }

}