package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ShapeContext
{
	public final EntityPlayer player;
	public final World world;
	public final BlockPos pos;
	public final ForgeDirection face;
	public final Block originalBlock;
	public final int originalMeta;
	public final BlockMatcher matcher;
	public final int maxBlocks;

	public ShapeContext(EntityPlayer player, BlockPos pos, ForgeDirection face, Block originalBlock, int originalMeta, BlockMatcher matcher, int maxBlocks)
	{
		this.player = player;
		this.world = player.worldObj;
		this.pos = pos;
		this.face = face == null ? ForgeDirection.UNKNOWN : face;
		this.originalBlock = originalBlock;
		this.originalMeta = originalMeta;
		this.matcher = matcher;
		this.maxBlocks = maxBlocks;
	}

	public boolean check(BlockPos p)
	{
		if (p == null || p.y < 0 || p.y > 255 || !world.blockExists(p.x, p.y, p.z))
		{
			return false;
		}

		Block block = world.getBlock(p.x, p.y, p.z);

		if (block == null || block.isAir(world, p.x, p.y, p.z))
		{
			return false;
		}

		return matcher.check(this, block, world.getBlockMetadata(p.x, p.y, p.z));
	}

	/**
	 * Re-checks the exact Block + metadata pair immediately before harvesting.
	 * This prevents a changed block, including a modded block variant, from
	 * being broken because it happened to occupy a cached position.
	 */
	public boolean matchesOriginal(BlockPos p)
	{
		return check(p);
	}

	/**
	 * Face used by straight tunnels. Horizontal faces are replaced with the
	 * opposite of the player's horizontal facing, matching the 1.12 bytecode;
	 * subtracting this face then advances in the direction the player looks.
	 */
	public ForgeDirection getTunnelFace()
	{
		if (face == ForgeDirection.UNKNOWN || face.offsetY == 0)
		{
			return getHorizontalFacing().getOpposite();
		}

		return face;
	}

	public ForgeDirection getHorizontalFacing()
	{
		int dir = net.minecraft.util.MathHelper.floor_double((player.rotationYaw * 4F / 360F) + 0.5D) & 3;

		switch (dir)
		{
			case 0:
				return ForgeDirection.SOUTH;
			case 1:
				return ForgeDirection.WEST;
			case 2:
				return ForgeDirection.NORTH;
			default:
				return ForgeDirection.EAST;
		}
	}
}
