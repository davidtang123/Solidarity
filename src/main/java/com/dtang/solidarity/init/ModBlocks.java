package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.BlockSimple;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceBlockInventory;
import com.dtang.solidarity.block.exampleitems.BlockInventoryBasic;
import com.dtang.solidarity.block.exampleitems.BlockInventoryFurnace;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister(ForgeRegistries.BLOCKS, Solidarity.MOD_ID);
    public static final RegistryObject<Block> LIMESTONE = registerBlockWithDefaultItem("limestone",
            () -> new BlockSimple(0.6f, 10.0f, 1, ToolType.PICKAXE));

    public static final RegistryObject<BlockInventoryBasic> blockInventoryBasic = registerBlockWithDefaultItem(
            "mbe30_block_registry_name",
            () -> new BlockInventoryBasic());
    public static final RegistryObject<BlockInventoryFurnace> blockInventoryFurnace = registerBlockWithDefaultItem(
            "mbe31_block_inventory_furnace_registry_name",
            () -> new BlockInventoryFurnace());
    public static final RegistryObject<RefractoryFurnaceBlockInventory> blockRefractoryFurnace = registerBlockWithDefaultItem(
            "refractory_furnace",
            () -> new RefractoryFurnaceBlockInventory());

    public static void register(){
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static <T extends Block> RegistryObject<T> registerBlockWithDefaultItem(String name, Supplier<? extends T>blockSupplier)
    {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().group(ModItems.solidarityGroup)));
        return block;
    }

    /*
    public static Block limestone;

    @SubscribeEvent
    public static void onBlocksRegistration(final RegistryEvent.Register<Block> blockRegisterEvent) {
        ModMachines.blockInventoryBasic = new BlockInventoryBasic().setRegistryName("mbe30_block_registry_name");
        blockRegisterEvent.getRegistry().register(ModMachines.blockInventoryBasic);
        ModMachines.blockFurnace = new BlockInventoryFurnace().setRegistryName("mbe31_block_inventory_furnace_registry_name");
        blockRegisterEvent.getRegistry().register(ModMachines.blockFurnace);
        ModMachines.blockRefractoryFurnace = new RefractoryFurnaceBlockInventory().setRegistryName("refractory_furnace");
        blockRegisterEvent.getRegistry().register(ModMachines.blockRefractoryFurnace);

        limestone = new BlockSimple(0.6f, 10.0f, 1, ToolType.PICKAXE).setRegistryName("limestone");
        blockRegisterEvent.getRegistry().register(limestone);
    }
    */
}
