package net.vergusha.elysiumharvest.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.Item;

@Mixin(Item.class)
public interface ItemAccessor {
    @Accessor("maxStackSize")
    void setMaxStackSize(int maxStackSize);
}
