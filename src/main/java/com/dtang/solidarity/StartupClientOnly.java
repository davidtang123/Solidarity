package com.dtang.solidarity;

import com.dtang.solidarity.block.exampleitems.ContainerScreenBasic;
import com.dtang.solidarity.block.exampleitems.ContainerScreenFurnace;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceContainerScreen;
import com.dtang.solidarity.init.ModContainers;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class StartupClientOnly
{
    // register the factory that is used on the client to generate a ContainerScreen corresponding to our Container
    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainers.INVBASIC.get(), ContainerScreenBasic::new);
        ScreenManager.registerFactory(ModContainers.INVFURNACE.get(), ContainerScreenFurnace::new);
        ScreenManager.registerFactory(ModContainers.REFRACTORY.get(), RefractoryFurnaceContainerScreen::new);
    }
}