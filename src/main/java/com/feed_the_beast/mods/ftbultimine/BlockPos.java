package com.feed_the_beast.mods.ftbultimine;

/**
 * Minecraft 1.7.10 has no BlockPos class, so the backport ships its own
 * immutable position holder with value semantics (needed for HashSet lookups
 * during flood fill).
 */
public final class BlockPos
{
	public final int x;
	public final int y;
	public final int z;

	public BlockPos(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockPos offset(int dx, int dy, int dz)
	{
		return new BlockPos(x + dx, y + dy, z + dz);
	}

	public double distanceSq(BlockPos pos)
	{
		double dx = x - pos.x;
		double dy = y - pos.y;
		double dz = z - pos.z;
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof BlockPos))
		{
			return false;
		}
		BlockPos p = (BlockPos) o;
		return x == p.x && y == p.y && z == p.z;
	}

	@Override
	public int hashCode()
	{
		int h = x;
		h = 31 * h + y;
		h = 31 * h + z;
		return h;
	}

	@Override
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}
}
