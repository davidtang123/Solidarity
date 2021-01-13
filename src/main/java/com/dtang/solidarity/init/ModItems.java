package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.item.crafting.GasTankItem;
import net.minecraft.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems {
    public static final ItemGroup solidarityGroup = new ItemGroup("Solidarity") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(ModItems.SLAG.get());
        }
    };

    private static final Item.Properties PropertiesGeneric = new Item.Properties().group(solidarityGroup)
            .maxStackSize(64);

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister(ForgeRegistries.ITEMS, Solidarity.MOD_ID);
    public static final RegistryObject<Item> SLAG = ITEMS.register("slag",
            () -> new Item(PropertiesGeneric));

    //Powders
    private static final Item.Properties PropertiesPowder = new Item.Properties().group(solidarityGroup)
            .maxStackSize(24);
    public static final RegistryObject<Item> QUICKLIME = ITEMS.register("quicklime",
            () -> new Item(PropertiesPowder));
    public static final RegistryObject<Item> FLY_ASH = ITEMS.register("fly_ash",
            () -> new Item(PropertiesPowder));
    public static final RegistryObject<Item> WOOD_ASH = ITEMS.register("wood_ash",
            () -> new Item(PropertiesPowder));
    public static final RegistryObject<Item> POTASH = ITEMS.register("potash",
            () -> new Item(PropertiesPowder));
    public static final RegistryObject<Item> SODA_ASH = ITEMS.register("soda_ash",
            () -> new Item(PropertiesPowder));

    private static final Item.Properties PropertiesLiquid = new Item.Properties().group(solidarityGroup)
            .maxStackSize(1).containerItem(Items.GLASS_BOTTLE);

    //Gas Tanks
    public static final RegistryObject<Item> GAS_TANK = ITEMS.register("gas_tank",
            () -> new Item(PropertiesGeneric));

    private static final Item.Properties PropertiesGasTank = new Item.Properties().group(solidarityGroup)//solidarityGroup)
            .maxStackSize(1);

    public static final RegistryObject<Item> GAS_TANK_CO2 = ITEMS.register("gas_tank_co2",
            () -> new GasTankItem(PropertiesGasTank));
    public static final RegistryObject<Item> GAS_TANK_SO2 = ITEMS.register("gas_tank_so2",
            () -> new GasTankItem(PropertiesGasTank));
    public static final RegistryObject<Item> GAS_TANK_SI_FUMES = ITEMS.register("gas_tank_si_fumes",
            () -> new GasTankItem(PropertiesGasTank));
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
