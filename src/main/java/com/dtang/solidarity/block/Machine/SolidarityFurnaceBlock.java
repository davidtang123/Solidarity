package com.dtang.solidarity.block.Machine;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import net.minecraft.block.AbstractFurnaceBlock;

import javax.annotation.Nullable;

public class SolidarityFurnaceBlock extends AbstractFurnaceBlock
{
    //public static final DirectionProperty FACING;
    //public static final BooleanProperty LIT;

    public SolidarityFurnaceBlock()
    {
        super(Block.Properties.create(Material.ROCK)
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    public int getLightValue(BlockState p_149750_1_) {
        return p_149750_1_.get(LIT) ? super.getLightValue(p_149750_1_) : 0;
    }

    // ---------------------

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return createNewTileEntity(world);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new RefractoryFurnaceTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    INamedContainerProvider namedContainerProvider;

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!worldIn.isRemote) {
            namedContainerProvider = this.getContainer(state, worldIn, pos);
            if (namedContainerProvider == null) {return ActionResultType.FAIL;}
            this.interactWith(worldIn, pos, player);
        }

        return ActionResultType.SUCCESS;
    }

    //@Override
    protected void interactWith(World worldIn, BlockPos pos, PlayerEntity player){
        if (namedContainerProvider != null) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider, (packetBuffer)->{});
            // (packetBuffer)->{} is just a do-nothing because we have no extra data to send
        }
    }

    //public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
    //    return (BlockState)this.getDefaultState().with(FACING, p_196258_1_.getPlacementHorizontalFacing().getOpposite());
    //}

    //public void onBlockPlacedBy(World p_180633_1_, BlockPos p_180633_2_, BlockState p_180633_3_, LivingEntity p_180633_4_, ItemStack p_180633_5_) {
    //    if (p_180633_5_.hasDisplayName()) {
    //        TileEntity lvt_6_1_ = p_180633_1_.getTileEntity(p_180633_2_);
    //        if (lvt_6_1_ instanceof AbstractFurnaceTileEntity) {
    //            ((AbstractFurnaceTileEntity)lvt_6_1_).setCustomName(p_180633_5_.getDisplayName());
    //        }
    //    }

    //}

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    // Code is copied directly from vanilla eg ChestBlock, CampfireBlock
    @Override
    public void onReplaced(BlockState state, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(blockPos);
            if (tileentity instanceof RefractoryFurnaceTileEntity) {
                RefractoryFurnaceTileEntity tileEntityFurnace = (RefractoryFurnaceTileEntity)tileentity;
                tileEntityFurnace.dropAllContents(world, blockPos);
            }
//      worldIn.updateComparatorOutputLevel(pos, this);  if the inventory is used to set redstone power for comparators
            super.onReplaced(state, world, blockPos, newState, isMoving);  // call it last, because it removes the TileEntity
        }
    }

    // ---------------------------
    // If you want your container to provide redstone power to a comparator based on its contents, implement these methods
    //  see vanilla for examples
    //public boolean hasComparatorInputOverride(BlockState p_149740_1_) {
    //    return true;
    //}

    //public int getComparatorInputOverride(BlockState p_180641_1_, World p_180641_2_, BlockPos p_180641_3_) {
    //    return Container.calcRedstone(p_180641_2_.getTileEntity(p_180641_3_));
    //}

    //public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
    //    return (BlockState)p_185499_1_.with(FACING, p_185499_2_.rotate((Direction)p_185499_1_.get(FACING)));
    //}

    //public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
    //    return p_185471_1_.rotate(p_185471_2_.toRotation((Direction)p_185471_1_.get(FACING)));
    //}

    //protected void fillStateContainer(StateContainer.Builder<Block, BlockState> p_206840_1_) {
    //    p_206840_1_.add(new IProperty[]{FACING, LIT});
    //}

    //static {
    //    FACING = HorizontalBlock.HORIZONTAL_FACING;
    //    LIT = RedstoneTorchBlock.LIT;
    //}
    //------------------------------------------------------------
    //  The code below isn't necessary for illustrating the Inventory Furnace concepts, it's just used for rendering.
    //  For more background information see MBE03

    // render using a BakedModel
    // required because the default (super method) is INVISIBLE for BlockContainers.
    @Override
    public BlockRenderType getRenderType(BlockState iBlockState) {
        return BlockRenderType.MODEL;
    }
}
