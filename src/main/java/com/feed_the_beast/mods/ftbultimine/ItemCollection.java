package com.feed_the_beast.mods.ftbultimine;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects everything an ultimine operation dropped and re-drops it according
 * to the dropItems config option. The original used ItemStackHandler from the
 * 1.11+ item capability API, which does not exist in 1.7.10, so stacks are
 * merged manually.
 */
public class ItemCollection
{
	public final List<ItemStack> items = new ArrayList<ItemStack>();

	public void add(ItemStack stack)
	{
		if (stack == null || stack.stackSize <= 0)
		{
			return;
		}

		for (ItemStack is : items)
		{
			if (is.stackSize < is.getMaxStackSize() && ItemStack.areItemStacksEqual(dummy(is), dummy(stack)))
			{
				int space = is.getMaxStackSize() - is.stackSize;
				int add = Math.min(space, stack.stackSize);
				is.stackSize += add;
				stack.stackSize -= add;

				if (stack.stackSize <= 0)
				{
					return;
				}
			}
		}

		items.add(stack.copy());
	}

	private static ItemStack dummy(ItemStack stack)
	{
		ItemStack is = stack.copy();
		is.stackSize = 1;
		return is;
	}

	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	public void clear()
	{
		items.clear();
	}

	/**
	 * @param mode 0 - drop at the mined block, 1 - drop at the player, 2 - straight into the inventory.
	 */
	public void collect(World world, EntityPlayer player, BlockPos origin, int mode)
	{
		for (ItemStack stack : items)
		{
			if (stack == null || stack.stackSize <= 0)
			{
				continue;
			}

			if (mode == 2)
			{
				ItemStack copy = stack.copy();

				if (!player.inventory.addItemStackToInventory(copy))
				{
					drop(world, player.posX, player.posY, player.posZ, copy);
				}
				else if (copy.stackSize > 0)
				{
					drop(world, player.posX, player.posY, player.posZ, copy);
				}

				player.inventoryContainer.detectAndSendChanges();
			}
			else if (mode == 1)
			{
				drop(world, player.posX, player.posY, player.posZ, stack.copy());
			}
			else
			{
				drop(world, origin.x + 0.5D, origin.y + 0.5D, origin.z + 0.5D, stack.copy());
			}
		}

		clear();
	}

	private static void drop(World world, double x, double y, double z, ItemStack stack)
	{
		EntityItem item = new EntityItem(world, x, y, z, stack);
		item.motionX = 0D;
		item.motionY = 0D;
		item.motionZ = 0D;
		item.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(item);
	}
}
