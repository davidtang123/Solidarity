package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.BlockSimple;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceBlockInventory;
import com.dtang.solidarity.block.exampleitems.BlockInventoryBasic;
import com.dtang.solidarity.block.exampleitems.BlockInventoryFurnace;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static BlockItem itemLimestone;

    //"Waste" byproducts
    public static Item itemSlag;
    public static Item potash;
    public static Item beechAsh;
    public static Item flyAsh;//Coal
    public static Item sodaAsh;
    public static Item strawAsh;//From wheat and rice straw

    //Powders

    //Gas tanks
    public static Item gasTank;
    public static Item gasTankCO2;
    public static Item gasTankSO2;

    public static final ItemGroup solidarityGroup = new ItemGroup("Solidarity") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(itemSlag);
        }
    };

    private static final Item.Properties PropertiesGeneric = new Item.Properties().group(solidarityGroup)
            .maxStackSize(64);
    private static final Item.Properties PropertiesPowder = new Item.Properties().group(solidarityGroup)
            .maxStackSize(24);
    private static final Item.Properties PropertiesLiquid = new Item.Properties().group(solidarityGroup)
            .maxStackSize(1).containerItem(Items.GLASS_BOTTLE);
    private static final Item.Properties PropertiesGasTank = new Item.Properties().group(solidarityGroup)
            .maxStackSize(1).containerItem(gasTank);

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister(ForgeRegistries.ITEMS, Solidarity.MOD_ID);
    public static final RegistryObject<Item> SLAG = ITEMS.register("slag",
            () -> new Item(PropertiesGeneric));

    //public static final RegistryObject<BlockInventoryBasic> blockInventoryBasic = ITEMS.register("limestone",
    //        () -> new BlockInventoryBasic());
    //public static final RegistryObject<BlockInventoryFurnace> blockInventoryFurnace = ITEMS.register("limestone",
    //        () -> new BlockInventoryFurnace());
    //public static final RegistryObject<RefractoryFurnaceBlockInventory> blockRefractoryFurnace = ITEMS.register("limestone",
    //        () -> new RefractoryFurnaceBlockInventory());

    public static void register(){
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    /*
    @SubscribeEvent
    public static void onItemsRegistration(final RegistryEvent.Register<Item> itemRegisterEvent) {
        ItemGroup solidarityGroup = new ItemGroup("Solidarity") {
            @OnlyIn(Dist.CLIENT)
            public ItemStack createIcon() {
                return new ItemStack(itemSlag);
            }
        };

        Item.Properties PropertiesGeneric = new Item.Properties().group(solidarityGroup)
                .maxStackSize(64);
        Item.Properties PropertiesPowder = new Item.Properties().group(solidarityGroup)
                .maxStackSize(24);
        Item.Properties PropertiesLiquid = new Item.Properties().group(solidarityGroup)
                .maxStackSize(1).containerItem(Items.GLASS_BOTTLE);
        Item.Properties PropertiesGasTank = new Item.Properties().group(solidarityGroup)
                .maxStackSize(1).containerItem(gasTank);

        ModMachines.itemBlockInventoryBasic = new BlockItem(ModMachines.blockInventoryBasic, PropertiesGeneric);
        ModMachines.itemBlockInventoryBasic.setRegistryName(ModMachines.blockInventoryBasic.getRegistryName());
        itemRegisterEvent.getRegistry().register(ModMachines.itemBlockInventoryBasic);

        ModMachines.itemBlockFurnace = new BlockItem(ModMachines.blockFurnace, PropertiesGeneric);
        ModMachines.itemBlockFurnace.setRegistryName(ModMachines.blockFurnace.getRegistryName());
        itemRegisterEvent.getRegistry().register(ModMachines.itemBlockFurnace);

        ModMachines.itemBlockRefractoryFurnace = new BlockItem(ModMachines.blockRefractoryFurnace, PropertiesGeneric);
        ModMachines.itemBlockRefractoryFurnace.setRegistryName(ModMachines.blockRefractoryFurnace.getRegistryName());
        itemRegisterEvent.getRegistry().register(ModMachines.itemBlockRefractoryFurnace);

        itemLimestone = new BlockItem(ModBlocks.limestone, PropertiesGeneric);
        itemLimestone.setRegistryName(ModBlocks.limestone.getRegistryName());
        itemRegisterEvent.getRegistry().register(itemLimestone);

        itemSlag = new Item(PropertiesGeneric);
        itemSlag.setRegistryName("slag");
        itemRegisterEvent.getRegistry().register(itemSlag);
    }

     */
}
