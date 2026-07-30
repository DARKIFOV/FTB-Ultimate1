package com.feed_the_beast.mods.ftbultimine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

/** Configuration for the 1.7.10 backport. */
public class FTBUltimineConfig
{
	public static final String GENERAL = "general";

	private static Configuration config;

	public static int maxBlocks = 64;
	public static int dropItems = 0;
	public static double exhaustionPerBlock = 4D;
	public static boolean allowHand = true;
	/** Retained for old config compatibility. Strict matching always wins. */
	public static boolean mergeStone = false;
	public static double twerkChance = 0D;
	public static int twerkRadius = 4;
	public static boolean twerkPlants = false;
	/** -1: overlay text event, 0+: manual vertical offset in font heights. */
	public static int renderTextManually = -1;

	private static final CachedList BREAK_BLACKLIST = new CachedList();
	private static final CachedList BREAK_WHITELIST = new CachedList();
	private static final CachedList TOOL_BLACKLIST = new CachedList();
	private static final CachedList TWERK_BLACKLIST = new CachedList();
	private static final CachedList TWERK_WHITELIST = new CachedList();

	public static class CachedList
	{
		private String[] values = new String[0];
		private HashSet<String> cache = null;

		public void set(String[] v)
		{
			values = v == null ? new String[0] : v;
			cache = null;
		}

		public boolean isEmpty()
		{
			return values.length == 0;
		}

		public boolean contains(String id)
		{
			if (id == null)
			{
				return false;
			}

			if (cache == null)
			{
				cache = new HashSet<String>(Arrays.asList(values));
			}

			return cache.contains(id);
		}
	}

	public static void load(File file)
	{
		config = new Configuration(file);
		config.load();
		sync();
	}

	public static void sync()
	{
		maxBlocks = config.getInt("maxBlocks", GENERAL, 64, 1, 512, "Max blocks that can be mined at once.");
		dropItems = config.getInt("dropItems", GENERAL, 0, 0, 2, "0 - Drop at position where mined block was\n1 - drop at player position\n2 - place directly in inventory");
		exhaustionPerBlock = config.get(GENERAL, "exhaustionPerBlock", 4D, "Exhaustion setting. The mod applies value * 0.005 per mined block.", 0D, 1000D).getDouble(4D);
		allowHand = config.getBoolean("allowHand", GENERAL, true, "Allow ultimining with an empty hand.");
		mergeStone = config.getBoolean("mergeStone", GENERAL, false, "Legacy option kept for config compatibility. This port always matches exact block metadata.");
		twerkChance = config.get(GENERAL, "twerkChance", 0D, "Chance of a plant growing while twerking.", 0D, 1D).getDouble(0D);
		twerkRadius = config.getInt("twerkRadius", GENERAL, 4, 1, 20, "Radius in which plants can grow while twerking.");
		twerkPlants = config.getBoolean("twerkPlants", GENERAL, false, "Sneaking repeatedly makes nearby plants grow.");
		renderTextManually = config.getInt("renderTextManually", GENERAL, -1, -1, 100, "-1 uses the normal overlay text event. 0 or more draws manually with this vertical offset in font heights.");

		BREAK_BLACKLIST.set(config.getStringList("breakBlacklist", GENERAL, new String[0], "Blocks that can never be ultimined, format modid:block."));
		BREAK_WHITELIST.set(config.getStringList("breakWhitelist", GENERAL, new String[0], "If not empty, only these blocks can be ultimined."));
		TOOL_BLACKLIST.set(config.getStringList("toolBlacklist", GENERAL, new String[0], "Items that can't be used for ultimining, format modid:item."));
		TWERK_BLACKLIST.set(config.getStringList("twerkBlacklist", GENERAL, new String[] {"minecraft:grass", "minecraft:tallgrass"}, "Blocks that can't grow while twerking."));
		TWERK_WHITELIST.set(config.getStringList("twerkWhitelist", GENERAL, new String[0], "If not empty, only these blocks can grow while twerking."));

		if (config.hasChanged())
		{
			config.save();
		}
	}

	public static String getId(Block block)
	{
		Object name = block == null ? null : Block.blockRegistry.getNameForObject(block);
		return name == null ? "" : name.toString();
	}

	public static String getId(Item item)
	{
		Object name = item == null ? null : Item.itemRegistry.getNameForObject(item);
		return name == null ? "" : name.toString();
	}

	public static boolean breakBlacklisted(Block block)
	{
		return BREAK_BLACKLIST.contains(getId(block));
	}

	public static boolean breakWhitelisted(Block block)
	{
		return BREAK_WHITELIST.isEmpty() || BREAK_WHITELIST.contains(getId(block));
	}

	public static boolean toolBlacklisted(Item item)
	{
		return TOOL_BLACKLIST.contains(getId(item));
	}

	public static boolean canTwerkGrow(Block block)
	{
		String id = getId(block);

		if (TWERK_BLACKLIST.contains(id))
		{
			return false;
		}

		// Saplings are the original always-supported twerk target. Other
		// IGrowable blocks require twerkPlants and respect the whitelist.
		if (block instanceof BlockSapling)
		{
			return true;
		}

		return twerkPlants && (TWERK_WHITELIST.isEmpty() || TWERK_WHITELIST.contains(id));
	}

	public static Configuration getConfig()
	{
		return config;
	}
}
