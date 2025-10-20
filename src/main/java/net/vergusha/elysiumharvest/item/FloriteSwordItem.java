package net.vergusha.elysiumharvest.item;

import net.minecraft.world.item.Item;

public class FloriteSwordItem extends Item {
    public FloriteSwordItem(Properties properties) {
        super(properties);
    }

    // Мечи не требуют переопределения canPerformAction
    // так как они не имеют специальных взаимодействий с блоками
}
