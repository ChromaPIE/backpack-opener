package cr.chromapie.bpopener.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cr.chromapie.bpopener.BPOpenerMod;
import cr.chromapie.bpopener.config.BPOConfig;
import cr.chromapie.bpopener.core.BackPackClickAction;
import cr.chromapie.bpopener.core.OpenAction;

/**
 * Core event handler. Right-click logic is injected via MixinGuiContainer
 * since MouseEvent doesn't fire when GUI is open in 1.7.10 Forge.
 */
public class BPOHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static boolean activated = false;
    private static int lastSlot1 = -1;
    private static int lastSlot2 = -1;
    private static boolean previousSneaking = false;
    private static BackPackClickAction pendingAction = null;

    /** Called by MixinGuiContainer. Returns true to cancel native handling. */
    public static boolean onRightClickInGui(GuiContainer container, Slot slot) {
        if (activated || GuiScreen.isShiftKeyDown()) return false;
        if (slot == null) return false;

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return false;
        if (player.inventory.getItemStack() != null) return false;
        if (slot.inventory != player.inventory) return false;

        ItemStack stack = slot.getStack();
        if (stack == null || stack.stackSize > 1) return false;

        OpenAction action = BPOConfig.getOpenAction(stack);
        if (action == null) return false;

        lastSlot1 = slot.getSlotIndex();
        lastSlot2 = player.inventory.currentItem;

        if (BPOConfig.debug) {
            BPOpenerMod.getLogger()
                .info(
                    "Swap slot " + lastSlot1
                        + " (slotNumber="
                        + slot.slotNumber
                        + ") with hotbar "
                        + lastSlot2
                        + ", GUI: "
                        + container.getClass()
                            .getName());
        }

        int windowSlotIndex = isInventoryGui(mc.currentScreen) ? slot.getSlotIndex() : slot.slotNumber;
        boolean swapped = false;

        if (lastSlot1 < 9) {
            player.inventory.currentItem = lastSlot1;
        } else {
            swapped = true;
            doSwap(container.inventorySlots.windowId, windowSlotIndex, player.inventory.currentItem);
        }

        if (!isItemEqual(player.getHeldItem(), stack)) {
            if (swapped) {
                doSwap(container.inventorySlots.windowId, windowSlotIndex, player.inventory.currentItem);
            }
            return false;
        }

        if (swapped && BPOConfig.openDelay > 0) {
            pendingAction = new BackPackClickAction(action, stack, BPOConfig.openDelay);
        } else {
            doClickAction(action, player);
        }

        return true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (pendingAction != null && pendingAction.tick()) {
            EntityClientPlayerMP player = mc.thePlayer;
            if (player != null && isItemEqual(player.getHeldItem(), pendingAction.itemStack)) {
                doClickAction(pendingAction.action, player);
            }
            pendingAction = null;
        }
    }

    private static void doClickAction(OpenAction action, EntityClientPlayerMP player) {
        activated = true;
        boolean shouldSneak = action.isSneaking();
        previousSneaking = player.movementInput.sneak;

        if (shouldSneak != previousSneaking) {
            setPlayerSneakState(shouldSneak);
        }

        mc.playerController.sendUseItem(player, mc.theWorld, player.getHeldItem());

        if (shouldSneak != previousSneaking) {
            setPlayerSneakState(previousSneaking);
        }
    }

    private static void setPlayerSneakState(boolean sneak) {
        mc.thePlayer.movementInput.sneak = sneak;
        mc.thePlayer.setSneaking(sneak);
        mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, sneak ? 1 : 2));
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        try {
            if (pendingAction != null) {
                EntityClientPlayerMP player = mc.thePlayer;
                if (player != null && lastSlot1 >= 9) {
                    GuiInventory tempGui = new GuiInventory(player);
                    doSwap(tempGui.inventorySlots.windowId, lastSlot1, lastSlot2);
                } else if (player != null && lastSlot1 >= 0) {
                    player.inventory.currentItem = lastSlot2;
                }
                pendingAction = null;
                return;
            }

            if (activated && event.gui == null) {
                activated = false;
                EntityClientPlayerMP player = mc.thePlayer;

                GuiInventory guiInventory = new GuiInventory(player);
                event.gui = guiInventory;

                if (lastSlot1 < 9) {
                    player.inventory.currentItem = lastSlot2;
                } else {
                    doSwap(guiInventory.inventorySlots.windowId, lastSlot1, lastSlot2);
                }

                if (!BPOConfig.returnToInventory) {
                    event.gui = null;
                }
            }
        } catch (Exception e) {
            BPOpenerMod.getLogger()
                .error("Error handling GUI close:", e);
            reset();
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!BPOConfig.addTooltip) return;
        if (activated || GuiScreen.isShiftKeyDown()) return;

        ItemStack stack = event.itemStack;
        if (stack == null) return;

        if (!(mc.currentScreen instanceof GuiContainer)) return;

        GuiContainer container = (GuiContainer) mc.currentScreen;
        Slot slot = getSlotUnderMouse(container);
        if (slot == null || slot.inventory != event.entityPlayer.inventory) return;

        if (BPOConfig.getOpenAction(stack) != null) {
            event.toolTip.add(new ChatComponentTranslation("tooltip.bpopener.open.name").getFormattedText());
        }
    }

    private static Slot getSlotUnderMouse(GuiContainer gui) {
        try {
            return cpw.mods.fml.common.ObfuscationReflectionHelper
                .getPrivateValue(GuiContainer.class, gui, "theSlot", "field_147006_u");
        } catch (Exception e) {
            if (BPOConfig.debug) {
                BPOpenerMod.getLogger()
                    .error("Failed to get theSlot:", e);
            }
            return null;
        }
    }

    private static boolean isInventoryGui(GuiScreen screen) {
        return screen instanceof GuiInventory || screen instanceof GuiContainerCreative;
    }

    private static void doSwap(int windowId, int slotIndex, int hotbarIndex) {
        if (slotIndex != hotbarIndex) {
            mc.playerController.windowClick(windowId, slotIndex, hotbarIndex, 2, mc.thePlayer);
        }
    }

    private static boolean isItemEqual(ItemStack a, ItemStack b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.isItemEqual(b);
    }

    public static void reset() {
        activated = false;
        lastSlot1 = -1;
        lastSlot2 = -1;
        pendingAction = null;
    }

    public static boolean isActivated() {
        return activated;
    }
}
