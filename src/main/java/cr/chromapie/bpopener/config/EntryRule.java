package cr.chromapie.bpopener.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import cr.chromapie.bpopener.core.OpenAction;

/** Immutable item matching rule. Format: "modid:itemname|action" */
public final class EntryRule {

    public final String pattern;
    public final OpenAction action;

    private final String modId;
    private final String itemPattern;
    private final boolean isWildcard;
    private final boolean hasPrefix;
    private final boolean hasSuffix;
    private final String matchPart;

    private EntryRule(String pattern, OpenAction action) {
        this.pattern = pattern;
        this.action = action;

        int colonIndex = pattern.indexOf(':');
        if (colonIndex > 0) {
            this.modId = pattern.substring(0, colonIndex);
            this.itemPattern = pattern.substring(colonIndex + 1);
        } else {
            this.modId = "";
            this.itemPattern = pattern;
        }

        this.isWildcard = "*".equals(itemPattern);
        this.hasPrefix = !isWildcard && itemPattern.endsWith("*");
        this.hasSuffix = !isWildcard && itemPattern.startsWith("*");

        String extracted;
        if (hasPrefix && hasSuffix) {
            extracted = itemPattern.substring(1, itemPattern.length() - 1);
        } else if (hasPrefix) {
            extracted = itemPattern.substring(0, itemPattern.length() - 1);
        } else if (hasSuffix) {
            extracted = itemPattern.substring(1);
        } else {
            extracted = itemPattern;
        }

        this.matchPart = (extracted.isEmpty() && !isWildcard) ? null : extracted;
    }

    public static EntryRule parse(String ruleString) {
        if (ruleString == null || ruleString.isEmpty()) return null;

        String[] parts = ruleString.split("\\|");
        if (parts.length != 2) return null;

        String pattern = parts[0].trim();
        String actionStr = parts[1].trim()
            .toUpperCase();

        OpenAction action;
        try {
            action = OpenAction.valueOf(actionStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (pattern.isEmpty()) return null;

        return new EntryRule(pattern, action);
    }

    public boolean matches(ItemStack stack) {
        if (stack == null) return false;

        Item item = stack.getItem();
        if (item == null) return false;

        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
        if (uid == null) return false;

        String stackModId = uid.modId;
        String stackItemName = uid.name;

        if (!modId.isEmpty() && !"*".equals(modId) && !modId.equals(stackModId)) {
            return false;
        }

        if (isWildcard || matchPart == null) {
            return true;
        }

        if (hasPrefix && hasSuffix) {
            return stackItemName.contains(matchPart);
        } else if (hasPrefix) {
            return stackItemName.startsWith(matchPart);
        } else if (hasSuffix) {
            return stackItemName.endsWith(matchPart);
        } else {
            return stackItemName.equals(matchPart);
        }
    }

    @Override
    public String toString() {
        return pattern + "|" + action.name();
    }
}
