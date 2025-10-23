package net.vergusha.elysiumharvest.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.vergusha.elysiumharvest.ElysiumHarvest;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void elysiumharvest$adjustSoupStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        Item item = self.getItem();
        if (item == Items.BEETROOT_SOUP || item == ElysiumHarvest.HARVEST_STEW.get()) {
            cir.setReturnValue(32);
        }
    }
}
