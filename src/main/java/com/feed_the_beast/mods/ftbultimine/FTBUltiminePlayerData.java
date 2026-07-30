package com.feed_the_beast.mods.ftbultimine;

import com.feed_the_beast.mods.ftbultimine.shape.BlockMatcher;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import com.feed_the_beast.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FTBUltiminePlayerData
{
	public final UUID id;
	public boolean pressed = false;
	public Shape shape = Shape.getDefault();
	public BlockPos cachedPos = null;
	public ForgeDirection cachedDirection = ForgeDirection.UNKNOWN;
	public Block cachedOriginalBlock = null;
	public int cachedOriginalMeta = -1;
	public ShapeContext cachedContext = null;
	public List<BlockPos> cachedBlocks = Collections.emptyList();
	public boolean prevSneaking = false;

	public FTBUltiminePlayerData(UUID i)
	{
		id = i;
	}

	public void clearCache()
	{
		cachedPos = null;
		cachedDirection = ForgeDirection.UNKNOWN;
		cachedOriginalBlock = null;
		cachedOriginalMeta = -1;
		cachedContext = null;
		cachedBlocks = Collections.emptyList();
	}

	/** Recalculates a shape using an exact Block + metadata matcher. */
	public boolean updateBlocks(EntityPlayerMP player, BlockPos pos, ForgeDirection face, boolean forceUpdate)
	{
		Block block = player.worldObj.getBlock(pos.x, pos.y, pos.z);
		int meta = player.worldObj.getBlockMetadata(pos.x, pos.y, pos.z);

		if (!forceUpdate && pos.equals(cachedPos) && face == cachedDirection && block == cachedOriginalBlock && meta == cachedOriginalMeta)
		{
			return false;
		}

		cachedPos = pos;
		cachedDirection = face;
		cachedOriginalBlock = block;
		cachedOriginalMeta = meta;

		if (block == null || block.isAir(player.worldObj, pos.x, pos.y, pos.z))
		{
			cachedContext = null;
			cachedBlocks = Collections.emptyList();
			return true;
		}

		cachedContext = new ShapeContext(player, pos, face, block, meta, BlockMatcher.MATCH, FTBUltimineConfig.maxBlocks);
		cachedBlocks = shape.getBlocks(cachedContext);
		return true;
	}
}
