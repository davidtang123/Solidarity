package com.dtang.solidarity.init;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceRecipe;
import com.dtang.solidarity.block.Machine.RefractoryFurnaceSerializer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

//Based off of BetterEndForge ModRecipeSerializers
public class ModRecipeSerializers {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS =
            new DeferredRegister(ForgeRegistries.RECIPE_SERIALIZERS, Solidarity.MOD_ID);

    public static final RegistryObject<RefractoryFurnaceSerializer<RefractoryFurnaceRecipe>> REFRACTORY =
            RECIPE_SERIALIZERS.register("refractory_smelting",
            () -> new RefractoryFurnaceSerializer<>(RefractoryFurnaceRecipe::new, 1000));

    public static <T extends IRecipe<?>> IRecipeType<T> registerRecipeType(String type)
    {
        ResourceLocation recipeTypeId = new ResourceLocation(Solidarity.MOD_ID, type);
        return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new IRecipeType<T>()
        {
            public String toString()
            {
                return type;
            }
        });
    }


    /*
    @SubscribeEvent
    public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        //event.getRegistry().register()
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));

    }
    @SubscribeEvent
    public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        refractoryFurnaceRecipeSerializer = (RefractoryFurnaceSerializer<RefractoryFurnaceRecipe>) new RefractoryFurnaceSerializer<RefractoryFurnaceRecipe>(RefractoryFurnaceRecipe::new, 1000).setRegistryName("recipe_refractory_furnace");
        event.getRegistry().register(refractoryFurnaceRecipeSerializer);
    }
     */
}