package net.vergusha.elysiumharvest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class QazanBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 1.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
            Block.box(1.0D, 3.0D, 1.0D, 15.0D, 5.0D, 2.0D),
            Block.box(1.0D, 3.0D, 14.0D, 15.0D, 5.0D, 15.0D),
            Block.box(1.0D, 3.0D, 1.0D, 2.0D, 5.0D, 15.0D),
            Block.box(14.0D, 3.0D, 1.0D, 15.0D, 5.0D, 15.0D)
    );

    public QazanBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
