package cr.chromapie.bpopener.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import cr.chromapie.bpopener.BPOpenerMod;
import cr.chromapie.bpopener.core.OpenAction;

public class BPOConfig {

    private static Configuration config;

    public static boolean addTooltip = true;
    public static boolean returnToInventory = true;
    public static int openDelay = 0;
    public static boolean debug = false;

    private static String[] itemRules = new String[] { "Backpacks:*|USE", "IronBackpacks:*|USE",
        "adventurebackpack:*|USE", "BetterStorage:*backpack*|USE" };

    private static List<EntryRule> parsedRules = new ArrayList<EntryRule>();

    public static void init(File configDirectory) {
        config = new Configuration(new File(configDirectory, "bpopener.cfg"));
        load();
    }

    public static void load() {
        try {
            config.load();

            addTooltip = config
                .getBoolean("addTooltip", Configuration.CATEGORY_GENERAL, true, "Show tooltip hint on openable items");

            returnToInventory = config.getBoolean(
                "returnToInventory",
                Configuration.CATEGORY_GENERAL,
                true,
                "Return to inventory GUI after closing backpack");

            openDelay = config.getInt(
                "openDelay",
                Configuration.CATEGORY_GENERAL,
                0,
                0,
                100,
                "Ticks to wait after swap before opening (0=immediate)");

            debug = config.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Enable debug logging");

            Property rulesProp = config.get(
                Configuration.CATEGORY_GENERAL,
                "itemRules",
                itemRules,
                "Item matching rules. Format: modid:itemname|action or modid:*|action\n"
                    + "action: USE or SNEAK_USE. Use * as wildcard.");
            itemRules = rulesProp.getStringList();

            parseRules();

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            BPOpenerMod.getLogger()
                .error("Failed to load config", e);
        }
    }

    private static void parseRules() {
        parsedRules.clear();
        for (String rule : itemRules) {
            EntryRule parsed = EntryRule.parse(rule);
            if (parsed != null) {
                parsedRules.add(parsed);
            } else if (debug) {
                BPOpenerMod.getLogger()
                    .warn("Invalid rule: " + rule);
            }
        }
        if (debug) {
            BPOpenerMod.getLogger()
                .info("Loaded " + parsedRules.size() + " rules");
        }
    }

    public static void reload() {
        load();
    }

    public static void save() {
        if (config != null && config.hasChanged()) {
            config.save();
        }
    }

    public static OpenAction getOpenAction(ItemStack stack) {
        if (stack == null) return null;
        for (EntryRule rule : parsedRules) {
            if (rule.matches(stack)) {
                return rule.action;
            }
        }
        return null;
    }

    public static boolean canOpen(ItemStack stack) {
        return getOpenAction(stack) != null;
    }
}
