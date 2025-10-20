package net.vergusha.elysiumharvest.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Кастомный эффект для визуального отображения бонуса от полного сета
 * флоритовой брони
 */
public class FloriteSetBonusEffect extends MobEffect {

    public FloriteSetBonusEffect() {
        // BENEFICIAL = положительный эффект (синяя рамка)
        // Цвет эффекта в инвентаре (RGB): зелёный оттенок для темы роста растений
        super(MobEffectCategory.BENEFICIAL, 0x4CAF50);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Эффект не имеет активного действия (только визуальное отображение)
        return false;
    }
}
