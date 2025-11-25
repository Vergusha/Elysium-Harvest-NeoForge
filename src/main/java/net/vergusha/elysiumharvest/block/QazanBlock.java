package net.vergusha.elysiumharvest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;

public class QazanBlock extends BaseEntityBlock {
    public static final MapCodec<QazanBlock> CODEC = simpleCodec(QazanBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 1.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
            Block.box(1.0D, 3.0D, 1.0D, 15.0D, 5.0D, 2.0D),
            Block.box(1.0D, 3.0D, 14.0D, 15.0D, 5.0D, 15.0D),
            Block.box(1.0D, 3.0D, 1.0D, 2.0D, 5.0D, 15.0D),
            Block.box(14.0D, 3.0D, 1.0D, 15.0D, 5.0D, 15.0D));

    public QazanBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QazanBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return !level.isClientSide()
                ? createTickerHelper(type, ElysiumHarvest.QAZAN_BLOCK_ENTITY.get(), QazanBlockEntity::serverTick)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof QazanBlockEntity qazanEntity) {
                ItemStack handItem = player.getMainHandItem();

                // Если есть готовая еда - её можно забрать ТОЛЬКО миской
                if (qazanEntity.hasResult()) {
                    if (handItem.is(Items.BOWL)) {
                        ItemStack result = qazanEntity.extractResult();

                        // Уменьшаем стак мисок
                        if (!player.getAbilities().instabuild) {
                            handItem.shrink(1);
                        }

                        // Даём игроку готовую еду
                        if (!player.getInventory().add(result)) {
                            player.drop(result, false);
                        }

                        // Звук зачерпывания
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                        return InteractionResult.SUCCESS;
                    } else {
                        // Показываем сообщение что нужна миска
                        player.displayClientMessage(Component.translatable("message.elysiumharvest.qazan.need_bowl"),
                                true);
                        return InteractionResult.CONSUME;
                    }
                }

                // Если нет готовой еды - открываем GUI для добавления ингредиентов
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.openMenu(qazanEntity);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof QazanBlockEntity qazanEntity) {
            if (!level.isClientSide()) {
                Containers.dropContents(level, pos, qazanEntity);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
