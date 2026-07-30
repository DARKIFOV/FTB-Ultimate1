package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Client -> server: ultimine key held / released. */
public class KeyPressedPacket implements IMessage
{
	private boolean pressed;

	public KeyPressedPacket()
	{
	}

	public KeyPressedPacket(boolean p)
	{
		pressed = p;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pressed = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(pressed);
	}

	public static class Handler implements IMessageHandler<KeyPressedPacket, IMessage>
	{
		@Override
		public IMessage onMessage(final KeyPressedPacket message, MessageContext ctx)
		{
			final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

			// Forge 1.7.10 has no MinecraftServer#addScheduledTask API.
			// SimpleNetworkWrapper handlers in this version commonly update the
			// player state directly; all actual block work still happens on ticks.
			FTBUltimine.instance.setKeyPressed(player, message.pressed);

			return null;
		}
	}
}
