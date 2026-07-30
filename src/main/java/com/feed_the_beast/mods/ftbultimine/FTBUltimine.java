package com.feed_the_beast.mods.ftbultimine;

import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.SendShapePacket;
import com.feed_the_beast.mods.ftbultimine.shape.EscapeTunnelShape;
import com.feed_the_beast.mods.ftbultimine.shape.MiningTunnelShape;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import com.feed_the_beast.mods.ftbultimine.shape.ShapeContext;
import com.feed_the_beast.mods.ftbultimine.shape.ShapelessShape;
import com.feed_the_beast.mods.ftbultimine.shape.SmallSquareShape;
import com.feed_the_beast.mods.ftbultimine.shape.SmallTunnelShape;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(modid = FTBUltimine.MOD_ID, name = FTBUltimine.MOD_NAME, version = FTBUltimine.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*")
public class FTBUltimine
{
	public static final String MOD_ID = "ftbultimine";
	public static final String MOD_NAME = "FTB Ultimine";
	public static final String VERSION = "1.7.10-1.3.4-port.2";

	@Mod.Instance(MOD_ID)
	public static FTBUltimine instance;

	@SidedProxy(clientSide = "com.feed_the_beast.mods.ftbultimine.client.FTBUltimineClient", serverSide = "com.feed_the_beast.mods.ftbultimine.FTBUltimineCommon")
	public static FTBUltimineCommon proxy;

	private Map<UUID, FTBUltiminePlayerData> cachedDataMap = new HashMap<UUID, FTBUltiminePlayerData>();
	private boolean isBreakingBlock = false;
	private int tempBlockDroppedXp = 0;
	private final ItemCollection tempBlockDropsList = new ItemCollection();
	private int tick = 0;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		FTBUltimineConfig.load(event.getSuggestedConfigurationFile());
		FTBUltimineNet.init();
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

		Shape.register(new ShapelessShape());
		Shape.register(new SmallTunnelShape());
		Shape.register(new SmallSquareShape());
		Shape.register(new MiningTunnelShape());
		Shape.register(new EscapeTunnelShape());

		proxy.preinit();
	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event)
	{
		Shape.postInit();
	}

	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		cachedDataMap = new HashMap<UUID, FTBUltiminePlayerData>();
	}

	public FTBUltiminePlayerData get(EntityPlayer player)
	{
		UUID id = player.getUniqueID();
		FTBUltiminePlayerData data = cachedDataMap.get(id);

		if (data == null)
		{
			data = new FTBUltiminePlayerData(id);
			cachedDataMap.put(id, data);
		}

		return data;
	}

	public void setKeyPressed(EntityPlayerMP player, boolean pressed)
	{
		FTBUltiminePlayerData data = get(player);
		data.pressed = pressed;
		data.clearCache();

		if (!pressed)
		{
			FTBUltimineNet.MAIN.sendTo(new SendShapePacket("", Collections.<BlockPos>emptyList()), player);
		}
	}

	public void modeChanged(EntityPlayerMP player, boolean next)
	{
		FTBUltiminePlayerData data = get(player);
		data.shape = next ? data.shape.next() : data.shape.prev();
		data.clearCache();
		FTBUltimineNet.MAIN.sendTo(new SendShapePacket(data.shape.getName(), Collections.<BlockPos>emptyList()), player);
	}

	@SubscribeEvent
	public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			FTBUltiminePlayerData data = get(player);
			FTBUltimineNet.MAIN.sendTo(new SendShapePacket(data.shape.getName(), Collections.<BlockPos>emptyList()), player);
		}
	}

	public static MovingObjectPosition rayTrace(EntityPlayer player)
	{
		double dist = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() : 5D;

		if (!player.capabilities.isCreativeMode)
		{
			dist = Math.max(0D, dist - 0.5D);
		}

		Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 look = player.getLookVec();
		Vec3 end = start.addVector(look.xCoord * dist, look.yCoord * dist, look.zCoord * dist);
		return player.worldObj.rayTraceBlocks(start, end);
	}

	/**
	 * The 1.12 version highlighted blocks through an ASM hook in RenderGlobal
	 * (the coremod). Here the server simply streams the current shape to the
	 * client, which renders the outlines itself.
	 */
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
		{
			return;
		}

		tick++;

		if (tick % 4 != 0)
		{
			return;
		}

		MinecraftServer server = MinecraftServer.getServer();

		if (server == null)
		{
			return;
		}

		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>(server.getConfigurationManager().playerEntityList);

		for (EntityPlayerMP player : players)
		{
			FTBUltiminePlayerData data = get(player);

			if (!data.pressed)
			{
				continue;
			}

			MovingObjectPosition result = rayTrace(player);

			if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			{
				if (!data.cachedBlocks.isEmpty() || data.cachedPos != null)
				{
					data.clearCache();
					FTBUltimineNet.MAIN.sendTo(new SendShapePacket(data.shape.getName(), Collections.<BlockPos>emptyList()), player);
				}

				continue;
			}

			BlockPos pos = new BlockPos(result.blockX, result.blockY, result.blockZ);
			ForgeDirection face = ForgeDirection.getOrientation(result.sideHit);

			if (data.updateBlocks(player, pos, face, false))
			{
				FTBUltimineNet.MAIN.sendTo(new SendShapePacket(data.shape.getName(), data.cachedBlocks), player);
			}
		}
	}

	// ------------------------------------------------------------- breaking

	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockBroken(BlockEvent.BreakEvent event)
	{
		if (event.world.isRemote || isBreakingBlock)
		{
			return;
		}

		EntityPlayer p = event.getPlayer();

		if (!(p instanceof EntityPlayerMP) || p instanceof FakePlayer)
		{
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) p;
		FTBUltiminePlayerData data = get(player);

		if (!data.pressed)
		{
			return;
		}

		if (!player.capabilities.isCreativeMode && player.getFoodStats().getFoodLevel() <= 0)
		{
			return;
		}

		ItemStack heldItem = player.getCurrentEquippedItem();

		if (heldItem == null)
		{
			if (!FTBUltimineConfig.allowHand)
			{
				return;
			}
		}
		else if (FTBUltimineConfig.toolBlacklisted(heldItem.getItem()))
		{
			return;
		}

		Block block = event.block;

		if (FTBUltimineConfig.breakBlacklisted(block) || !FTBUltimineConfig.breakWhitelisted(block))
		{
			return;
		}

		MovingObjectPosition result = rayTrace(player);

		if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
		{
			return;
		}

		BlockPos origin = new BlockPos(event.x, event.y, event.z);
		ForgeDirection face = ForgeDirection.getOrientation(result.sideHit);
		data.clearCache();
		data.updateBlocks(player, origin, face, true);

		List<BlockPos> blocks = data.cachedBlocks;
		ShapeContext context = data.cachedContext;

		if (context == null || blocks.isEmpty())
		{
			return;
		}

		isBreakingBlock = true;
		tempBlockDropsList.clear();
		tempBlockDroppedXp = 0;
		boolean hadTool = heldItem != null;

		try
		{
			for (BlockPos pos : blocks)
			{
				// Strict identity check: only the exact original Block + metadata
				// may be harvested, including blocks supplied by other mods.
				if (!context.matchesOriginal(pos))
				{
					continue;
				}

				Block b = event.world.getBlock(pos.x, pos.y, pos.z);

				if (FTBUltimineConfig.breakBlacklisted(b) || !FTBUltimineConfig.breakWhitelisted(b))
				{
					continue;
				}

				// This fires a nested BreakEvent, so protection mods can cancel
				// every individual block. The recursion guard only skips this mod.
				if (!player.theItemInWorldManager.tryHarvestBlock(pos.x, pos.y, pos.z))
				{
					continue;
				}

				if (!player.capabilities.isCreativeMode && FTBUltimineConfig.exhaustionPerBlock > 0D)
				{
					// The original 1.12 mod multiplies this setting by 0.005.
					player.addExhaustion((float) (FTBUltimineConfig.exhaustionPerBlock * 0.005D));

					if (player.getFoodStats().getFoodLevel() <= 0)
					{
						break;
					}
				}

				if (hadTool && player.getCurrentEquippedItem() == null)
				{
					break;
				}
			}
		}
		finally
		{
			isBreakingBlock = false;
		}

		if (!tempBlockDropsList.isEmpty())
		{
			tempBlockDropsList.collect(event.world, player, origin, FTBUltimineConfig.dropItems);
		}

		if (tempBlockDroppedXp > 0)
		{
			if (FTBUltimineConfig.dropItems == 2)
			{
				player.addExperience(tempBlockDroppedXp);
			}
			else
			{
				double x = FTBUltimineConfig.dropItems == 0 ? origin.x + 0.5D : player.posX;
				double y = FTBUltimineConfig.dropItems == 0 ? origin.y + 0.5D : player.posY + 0.5D;
				double z = FTBUltimineConfig.dropItems == 0 ? origin.z + 0.5D : player.posZ;
				EntityXPOrb orb = new EntityXPOrb(event.world, x, y, z, tempBlockDroppedXp);
				event.world.spawnEntityInWorld(orb);
			}

			tempBlockDroppedXp = 0;
		}

		data.clearCache();
		// The origin was harvested through tryHarvestBlock above. Cancel the
		// outer vanilla break to prevent it from being processed a second time.
		event.setCanceled(true);
	}

	/** Swallows drops and XP orbs while ultimining, exactly like the original. */
	@SubscribeEvent
	public void entityJoinedWorld(EntityJoinWorldEvent event)
	{
		if (!isBreakingBlock || event.world.isRemote)
		{
			return;
		}

		if (event.entity instanceof EntityItem)
		{
			ItemStack stack = ((EntityItem) event.entity).getEntityItem();

			if (stack != null && stack.stackSize > 0)
			{
				tempBlockDropsList.add(stack);
				event.setCanceled(true);
			}
		}
		else if (event.entity instanceof EntityXPOrb)
		{
			tempBlockDroppedXp += ((EntityXPOrb) event.entity).getXpValue();
			event.setCanceled(true);
		}
	}

	// ---------------------------------------------------------------- twerk

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote || FTBUltimineConfig.twerkChance <= 0D)
		{
			return;
		}

		EntityPlayer player = event.player;
		FTBUltiminePlayerData data = get(player);
		boolean sneaking = player.isSneaking();

		if (sneaking == data.prevSneaking)
		{
			return;
		}

		data.prevSneaking = sneaking;

		if (!sneaking)
		{
			return;
		}

		World world = player.worldObj;
		int x0 = MathHelper.floor_double(player.posX);
		int y0 = MathHelper.floor_double(player.posY);
		int z0 = MathHelper.floor_double(player.posZ);
		int r = FTBUltimineConfig.twerkRadius;

		for (int x = x0 - r; x <= x0 + r; x++)
		{
			for (int y = y0 - 1; y <= y0 + 1; y++)
			{
				for (int z = z0 - r; z <= z0 + r; z++)
				{
					if (y < 0 || y > 255 || !world.blockExists(x, y, z))
					{
						continue;
					}

					Block block = world.getBlock(x, y, z);

					if (!(block instanceof IGrowable) || !FTBUltimineConfig.canTwerkGrow(block))
					{
						continue;
					}

					if (world.rand.nextFloat() > FTBUltimineConfig.twerkChance)
					{
						continue;
					}

					IGrowable growable = (IGrowable) block;

					// func_149851_a = canGrow, func_149852_a = canUseBonemeal, func_149853_b = grow
					if (growable.func_149851_a(world, x, y, z, false) && growable.func_149852_a(world, world.rand, x, y, z))
					{
						growable.func_149853_b(world, world.rand, x, y, z);
						world.playAuxSFX(2005, x, y, z, 0);
					}
				}
			}
		}
	}
}
