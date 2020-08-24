package com.dtang.solidarity.item;

import com.dtang.solidarity.Solidarity;
import com.dtang.solidarity.block.ModBlocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    //The ITEMS deferred register in which you can register items.
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Solidarity.MOD_ID);

    //public static

    //Register the items as registry name and default properties
    public static final RegistryObject<Item> SLAG = ITEMS.register("slag", () ->
            new Item(new Item.Properties().group(ItemGroup.MISC)));

    //Register the tutorial block's item so a player can place it.
    public static final RegistryObject<Item> LIMESTONE = ITEMS.register("limestone", () ->
            new BlockItem(ModBlocks.LIMESTONE.get(), new Item.Properties()));
}
