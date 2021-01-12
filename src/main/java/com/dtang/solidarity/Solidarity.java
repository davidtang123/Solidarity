package com.dtang.solidarity;

import com.dtang.solidarity.init.*;
import net.minecraft.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



// The value here should match an entry in the META-INF/mods.toml file
@Mod(Solidarity.MOD_ID)
public class Solidarity
{
    public static final String MOD_ID = "solidarity";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final int maxPowderStackSize = 16;
    public static final int maxGasTankStackSize = 16;

    // get a reference to the event bus for this mod;  Registration events are fired on this bus.
    public static IEventBus MOD_EVENT_BUS;

    public Solidarity() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        //ModItems.ITEMS.register(MOD_EVENT_BUS);
        //ModBlocks.BLOCKS.register(MOD_EVENT_BUS);

        //MOD_EVENT_BUS.register(ModBlocks.class);
        //MOD_EVENT_BUS.register(ModItems.class);
        //MOD_EVENT_BUS.register(ModContainers.class);
        //MOD_EVENT_BUS.register(ModTileEntities.class);
        //MOD_EVENT_BUS.register(ModRecipeSerializers.class);

        //ModBlocks.register();
        ModBlocks.BLOCKS.register(MOD_EVENT_BUS);
        ModItems.ITEMS.register(MOD_EVENT_BUS);
        //ModSurfaceBuilders.SURFACE_BUILDERS.register(MOD_EVENT_BUS);
        //ModEnchantments.ENCHANTMENTS.register(MOD_EVENT_BUS);
        //ModEffects.EFFECTS.register(MOD_EVENT_BUS);
        //ModPotions.POTIONS.register(MOD_EVENT_BUS);
        //ModParticleTypes.PARTICLE_TYPES.register(MOD_EVENT_BUS);
        ModTileEntities.TILE_ENTITY_TYPES.register(MOD_EVENT_BUS);
        //ModEntityTypes.ENTITY_TYPES.register(MOD_EVENT_BUS);
        ModContainers.CONTAINERS.register(MOD_EVENT_BUS);
        //ModSoundEvents.SOUND_EVENTS.register(MOD_EVENT_BUS);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(MOD_EVENT_BUS);


        DistExecutor.runWhenOn(Dist.CLIENT, () -> Solidarity::registerClientOnlyEvents);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    public static void registerClientOnlyEvents() {
        MOD_EVENT_BUS.register(StartupClientOnly.class);
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
        //    LOGGER.info("HELLO from Register Block");
        }
    }
}
