package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.BlockPos;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/** 3x3 square on the plane of the hit face (original switched on face axis). */
public class SmallSquareShape extends Shape
{
	@Override
	public String getName()
	{
		return "small_square";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context)
	{
		List<BlockPos> list = new ArrayList<BlockPos>(9);
		list.add(context.pos);

		ForgeDirection face = context.face;

		for (int a = -1; a <= 1; a++)
		{
			for (int b = -1; b <= 1; b++)
			{
				if (a == 0 && b == 0)
				{
					continue;
				}

				BlockPos pos;

				if (face.offsetX != 0)
				{
					pos = context.pos.offset(0, a, b);
				}
				else if (face.offsetY != 0)
				{
					pos = context.pos.offset(a, 0, b);
				}
				else
				{
					pos = context.pos.offset(a, b, 0);
				}

				if (list.size() < context.maxBlocks && context.check(pos))
				{
					list.add(pos);
				}
			}
		}

		return list;
	}
}
