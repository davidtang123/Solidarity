package com.dtang.solidarity;

import com.dtang.solidarity.block.ModBlocks;
import com.dtang.solidarity.item.ModItems;
import net.minecraft.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
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

    // get a reference to the event bus for this mod;  Registration events are fired on this bus.
    public static IEventBus MOD_EVENT_BUS;

    public Solidarity() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        //ModItems.ITEMS.register(MOD_EVENT_BUS);
        //ModBlocks.BLOCKS.register(MOD_EVENT_BUS);

        MOD_EVENT_BUS.register(StartupCommon.class);
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
