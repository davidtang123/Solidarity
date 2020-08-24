package com.dtang.solidarity;

import com.dtang.solidarity.block.ContainerScreenBasic;
import com.dtang.solidarity.block.ContainerScreenFurnace;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * The Startup classes for this example are called during startup
 *  See MinecraftByExample class for more information
 */
public class StartupClientOnly
{
    // register the factory that is used on the client to generate a ContainerScreen corresponding to our Container
    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(StartupCommon.containerTypeContainerBasic, ContainerScreenBasic::new);
        ScreenManager.registerFactory(StartupCommon.containerTypeContainerFurnace, ContainerScreenFurnace::new);
    }
}