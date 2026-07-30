package com.feed_the_beast.mods.ftbultimine.client;

import com.feed_the_beast.mods.ftbultimine.BlockPos;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineClientData;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineCommon;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineConfig;
import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.KeyPressedPacket;
import com.feed_the_beast.mods.ftbultimine.net.ModeChangedPacket;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Client proxy, same class name and role as in the original mod:
 * one hold-key, sneak + mouse wheel to change shape, HUD info text.
 */
public class FTBUltimineClient extends FTBUltimineCommon
{
	public static KeyBinding keyBinding;

	private boolean pressed = false;
	private boolean hasScrolled = false;

	@Override
	public void preinit()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		// Original used KeyConflictContext.IN_GAME, which does not exist in 1.7.10.
		keyBinding = new KeyBinding("key.ftbultimine", Keyboard.KEY_GRAVE, "key.categories.ftbultimine");
		ClientRegistry.registerKeyBinding(keyBinding);
	}

	private static boolean isDown(KeyBinding key)
	{
		int code = key.getKeyCode();

		if (code < 0)
		{
			return Mouse.isButtonDown(code + 100);
		}

		return code != 0 && Keyboard.isKeyDown(code);
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase != TickEvent.Phase.START)
		{
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		boolean down = mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null && isDown(keyBinding);

		if (down != pressed)
		{
			pressed = down;

			if (!pressed)
			{
				FTBUltimineClientData.clearBlocks();
				hasScrolled = false;
			}

			FTBUltimineNet.MAIN.sendToServer(new KeyPressedPacket(pressed));
		}
	}

	/**
	 * Shift + scroll changes shape. If Ultimine itself is rebound to either
	 * Shift key, Ctrl becomes the modifier so the combination remains usable.
	 */
	@SubscribeEvent
	public void mouseEvent(MouseEvent event)
	{
		if (!pressed || event.dwheel == 0 || !isShapeModifierDown())
		{
			return;
		}

		hasScrolled = true;
		FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket(event.dwheel < 0));
		event.setCanceled(true);
	}

	private boolean isShapeModifierDown()
	{
		int code = keyBinding.getKeyCode();

		if (code == Keyboard.KEY_LSHIFT || code == Keyboard.KEY_RSHIFT)
		{
			return GuiScreen.isCtrlKeyDown();
		}

		return GuiScreen.isShiftKeyDown();
	}

	private void addPressedInfo(List<String> list)
	{
		list.add(I18n.format("ftbultimine.active", new Object[0]));

		if (!hasScrolled)
		{
			list.add(EnumChatFormatting.GRAY + I18n.format("ftbultimine.change_shape", new Object[0]));
		}

		Shape current = Shape.get(FTBUltimineClientData.getShapeId());

		if (current != null)
		{
			if (isShapeModifierDown())
			{
				list.add("");
				list.add(EnumChatFormatting.GRAY + "^ " + I18n.format("ftbultimine.shape." + current.prev().getName(), new Object[0]));
			}

			list.add("- " + I18n.format("ftbultimine.shape." + current.getName(), new Object[0]));

			if (isShapeModifierDown())
			{
				list.add(EnumChatFormatting.GRAY + "v " + I18n.format("ftbultimine.shape." + current.next().getName(), new Object[0]));
			}
		}
	}

	@SubscribeEvent
	public void info(RenderGameOverlayEvent.Text event)
	{
		if (pressed && FTBUltimineConfig.renderTextManually == -1)
		{
			addPressedInfo(event.left);
		}
	}

	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Post event)
	{
		if (!pressed || FTBUltimineConfig.renderTextManually == -1 || event.type != RenderGameOverlayEvent.ElementType.ALL)
		{
			return;
		}

		List<String> list = new ArrayList<String>();
		addPressedInfo(list);

		Minecraft mc = Minecraft.getMinecraft();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < list.size(); i++)
		{
			mc.fontRenderer.drawStringWithShadow(list.get(i), 2, 2 + (i + FTBUltimineConfig.renderTextManually) * mc.fontRenderer.FONT_HEIGHT, 0xFFFFFF);
		}

		GL11.glDisable(GL11.GL_BLEND);
	}

	/** Replacement for the coremod RenderGlobal hook of the original mod. */
	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event)
	{
		List<BlockPos> shapeBlocks = FTBUltimineClientData.getBlocks();

		if (!pressed || shapeBlocks.isEmpty())
		{
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		if (player == null || mc.theWorld == null)
		{
			return;
		}

		float pt = event.partialTicks;
		double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * pt;
		double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * pt;
		double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pt;

		GL11.glPushMatrix();
		GL11.glTranslated(-px, -py, -pz);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(2F);
		GL11.glColor4f(0.15F, 0.9F, 1F, 0.85F);

		for (BlockPos pos : shapeBlocks)
		{
			Block block = mc.theWorld.getBlock(pos.x, pos.y, pos.z);

			if (block == null || block.isAir(mc.theWorld, pos.x, pos.y, pos.z))
			{
				continue;
			}

			block.setBlockBoundsBasedOnState(mc.theWorld, pos.x, pos.y, pos.z);
			drawOutline(AxisAlignedBB.getBoundingBox(
					pos.x + block.getBlockBoundsMinX() - 0.002D,
					pos.y + block.getBlockBoundsMinY() - 0.002D,
					pos.z + block.getBlockBoundsMinZ() - 0.002D,
					pos.x + block.getBlockBoundsMaxX() + 0.002D,
					pos.y + block.getBlockBoundsMaxY() + 0.002D,
					pos.z + block.getBlockBoundsMaxZ() + 0.002D));
		}

		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}

	private static void drawOutline(AxisAlignedBB b)
	{
		GL11.glBegin(GL11.GL_LINES);

		line(b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ);
		line(b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ);
		line(b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ);
		line(b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ);

		line(b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ);
		line(b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ);
		line(b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ);
		line(b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ);

		line(b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ);
		line(b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ);
		line(b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ);
		line(b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ);

		GL11.glEnd();
	}

	private static void line(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		GL11.glVertex3d(x1, y1, z1);
		GL11.glVertex3d(x2, y2, z2);
	}
}
