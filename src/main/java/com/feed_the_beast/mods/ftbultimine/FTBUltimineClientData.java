package com.feed_the_beast.mods.ftbultimine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder for the shape data received from the server.
 *
 * This class must NOT touch any client-only class: SimpleNetworkWrapper loads
 * every registered message handler on both sides, so a handler that references
 * Minecraft classes directly would crash a dedicated server.
 */
public class FTBUltimineClientData
{
	private static String shapeId = "";
	private static List<BlockPos> blocks = Collections.emptyList();

	public static void set(String id, List<BlockPos> list)
	{
		shapeId = id == null ? "" : id;
		blocks = list == null || list.isEmpty() ? Collections.<BlockPos>emptyList() : new ArrayList<BlockPos>(list);
	}

	public static String getShapeId()
	{
		return shapeId;
	}

	public static List<BlockPos> getBlocks()
	{
		return blocks;
	}

	public static void clearBlocks()
	{
		blocks = Collections.emptyList();
	}
}
