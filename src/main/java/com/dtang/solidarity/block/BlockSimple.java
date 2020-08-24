package com.dtang.solidarity.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class BlockSimple extends Block {
    public BlockSimple()
    {
        super(Block.Properties.create(Material.ROCK)  // look at Block.Properties for further options
                // typically useful: hardnessAndResistance(), harvestLevel(), harvestTool()
        );
    }

    public BlockSimple(float hard, float res, int harvest, ToolType tool)
    {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hard, res).harvestLevel(harvest).harvestTool(tool));
    }

    // render using a BakedModel (mbe01_block_simple.json --> mbe01_block_simple_model.json)
    // not strictly required because the default (super method) is MODEL.
    //@Override
    //public BlockRenderType getRenderType(BlockState blockState) {
    //    return BlockRenderType.MODEL;
    //}
}
