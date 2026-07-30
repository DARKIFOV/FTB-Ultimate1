package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.block.Block;

/**
 * Determines whether a candidate block is the same exact block variant as the
 * block the player started mining. In 1.7.10 block variants are represented by
 * the Block registry entry plus metadata, so both values are compared.
 */
public interface BlockMatcher
{
	boolean check(ShapeContext context, Block block, int meta);

	BlockMatcher MATCH = new BlockMatcher()
	{
		@Override
		public boolean check(ShapeContext context, Block block, int meta)
		{
			return block == context.originalBlock && meta == context.originalMeta;
		}
	};
}
