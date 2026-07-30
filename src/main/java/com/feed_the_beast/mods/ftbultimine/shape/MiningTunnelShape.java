package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.BlockPos;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/** Diagonal 1x1 tunnel descending one block per step. */
public class MiningTunnelShape extends Shape
{
	@Override
	public String getName()
	{
		return "mining_tunnel";
	}

	@Override
	public List<BlockPos> getBlocks(ShapeContext context)
	{
		List<BlockPos> list = new ArrayList<BlockPos>(context.maxBlocks);
		ForgeDirection face = context.getTunnelFace();

		for (int i = 0; i < context.maxBlocks; i++)
		{
			BlockPos pos = context.pos.offset(-face.offsetX * i, -i, -face.offsetZ * i);

			if (!context.check(pos))
			{
				break;
			}

			list.add(pos);
		}

		return list;
	}
}
