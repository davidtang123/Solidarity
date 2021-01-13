package com.dtang.solidarity.item.crafting;

import com.dtang.solidarity.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;

public class GasTankItem extends Item {
    public GasTankItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(ModItems.GAS_TANK.get());
    }
/*
    @Deprecated
    public boolean hasContainerItem() {
        return true;
    }
 */
}
