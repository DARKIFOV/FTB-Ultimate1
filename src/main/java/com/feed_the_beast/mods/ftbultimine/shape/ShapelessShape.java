package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Default 26-neighbour vein search, limited to the nearest matching blocks. */
public class ShapelessShape extends Shape
{
	@Override
	public String getName()
	{
		return "shapeless";
	}

	@Override
	public boolean isDefault()
	{
		return true;
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context)
	{
		List<BlockPos> result = new ArrayList<BlockPos>(context.maxBlocks);
		Queue<BlockPos> queue = new ArrayDeque<BlockPos>();
		Set<BlockPos> seen = new HashSet<BlockPos>();

		queue.add(context.pos);
		seen.add(context.pos);

		while (!queue.isEmpty() && result.size() < context.maxBlocks)
		{
			BlockPos pos = queue.remove();

			if (!context.check(pos))
			{
				continue;
			}

			result.add(pos);

			for (int dx = -1; dx <= 1; dx++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					for (int dz = -1; dz <= 1; dz++)
					{
						if (dx == 0 && dy == 0 && dz == 0)
						{
							continue;
						}

						BlockPos next = pos.offset(dx, dy, dz);

						if (seen.add(next))
						{
							queue.add(next);
						}
					}
				}
			}
		}

		return result;
	}
}
