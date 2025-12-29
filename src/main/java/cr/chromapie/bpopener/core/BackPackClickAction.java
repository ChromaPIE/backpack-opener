package cr.chromapie.bpopener.core;

import net.minecraft.item.ItemStack;

/** Delayed click action for opening backpack after slot swap. */
public class BackPackClickAction {

    public final OpenAction action;
    public final ItemStack itemStack;
    public int ticksRemaining;

    public BackPackClickAction(OpenAction action, ItemStack itemStack, int ticksRemaining) {
        this.action = action;
        this.itemStack = itemStack != null ? itemStack.copy() : null;
        this.ticksRemaining = ticksRemaining;
    }

    public boolean tick() {
        return --ticksRemaining <= 0;
    }
}
