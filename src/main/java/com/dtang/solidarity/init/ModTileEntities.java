package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceTileEntity;
import com.dtang.solidarity.block.exampleitems.TileEntityFurnace;
import com.dtang.solidarity.block.exampleitems.TileEntityInventoryBasic;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES =
            new DeferredRegister(ForgeRegistries.TILE_ENTITIES, Solidarity.MOD_ID);

    public static final RegistryObject<TileEntityType<TileEntityInventoryBasic>> INVBASIC =
            TILE_ENTITY_TYPES.register("mbe30_tile_entity_type_registry_name",
                    () -> TileEntityType.Builder.create(TileEntityInventoryBasic::new,
                            ModBlocks.blockInventoryBasic.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntityFurnace>> INVFURNACE =
            TILE_ENTITY_TYPES.register("mbe31_tile_entity_type_registry_name",
                    () -> TileEntityType.Builder.create(TileEntityFurnace::new,
                            ModBlocks.blockInventoryFurnace.get()).build(null));

    public static final RegistryObject<TileEntityType<RefractoryFurnaceTileEntity>> REFRACTORY =
            TILE_ENTITY_TYPES.register("tile_entity_type_refractory_furnace",
                    () -> TileEntityType.Builder.create(RefractoryFurnaceTileEntity::new,
                            ModBlocks.blockRefractoryFurnace.get()).build(null));
    /*
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

        tileEntityTypeRefractoryFurnace = TileEntityType.Builder.create(RefractoryFurnaceTileEntity::new, blockRefractoryFurnace)
                .build(null);
        tileEntityTypeRefractoryFurnace.setRegistryName("solidarity:tile_entity_type_refractory_furnace");
        event.getRegistry().register(tileEntityTypeRefractoryFurnace);
    }
     */
}
