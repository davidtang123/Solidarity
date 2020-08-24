package com.dtang.solidarity.block;

import com.dtang.solidarity.Solidarity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    //The BLOCKS deferred register in which you can register blocks.
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Solidarity.MOD_ID);

    //Register the tutorial block with "tutorial_block" as registry name and default ROCK properties
    public static final RegistryObject<Block> LIMESTONE = BLOCKS.register("limestone",
            () -> new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.6f, 10.0f)));

    private RegistryObject<Block> MakeBlock(String name, Material mat, float hard, float res, SoundType snd){
        return BLOCKS.register( name, () -> new Block(Block.Properties.create(mat).hardnessAndResistance(hard, res).sound(snd)));
    }
}
