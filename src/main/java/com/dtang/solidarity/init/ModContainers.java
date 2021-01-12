package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceContainer;
import com.dtang.solidarity.block.exampleitems.ContainerBasic;
import com.dtang.solidarity.block.exampleitems.ContainerFurnace;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister(ForgeRegistries.CONTAINERS,
            Solidarity.MOD_ID);

    public static final RegistryObject<ContainerType<ContainerBasic>> INVBASIC = CONTAINERS.register("mbe30_container_registry_name",
            () -> IForgeContainerType.create(ContainerBasic::createContainerClientSide));
    public static final RegistryObject<ContainerType<ContainerFurnace>> INVFURNACE = CONTAINERS.register("mbe31_container_registry_name",
            () -> IForgeContainerType.create(ContainerFurnace::createContainerClientSide));
    public static final RegistryObject<ContainerType<RefractoryFurnaceContainer>> REFRACTORY = CONTAINERS.register("containertype_refractory_furnace",
            () -> IForgeContainerType.create(RefractoryFurnaceContainer::createContainerClientSide));
    /*

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        containerTypeContainerBasic = IForgeContainerType.create(ContainerBasic::createContainerClientSide);
        containerTypeContainerBasic.setRegistryName("mbe30_container_registry_name");
        event.getRegistry().register(containerTypeContainerBasic);

        containerTypeContainerFurnace = IForgeContainerType.create(ContainerFurnace::createContainerClientSide);
        containerTypeContainerFurnace.setRegistryName("mbe31_container_registry_name");
        event.getRegistry().register(containerTypeContainerFurnace);

        //Note to self: make sure to update the registry name in Container...Furnace file as well

        containerTypeRefractoryFurnace = IForgeContainerType.create(RefractoryFurnaceContainer::createContainerClientSide);
        containerTypeRefractoryFurnace.setRegistryName("containertype_refractory_furnace");
        event.getRegistry().register(containerTypeRefractoryFurnace);
    }
     */
}
