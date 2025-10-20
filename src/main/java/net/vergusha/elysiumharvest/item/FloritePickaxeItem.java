package net.vergusha.elysiumharvest.item;

import net.minecraft.world.item.Item;

public class FloritePickaxeItem extends Item {
    public FloritePickaxeItem(Properties properties) {
        super(properties);
    }

    // Кирки не требуют переопределения canPerformAction
    // так как они не имеют специальных взаимодействий с блоками
}
