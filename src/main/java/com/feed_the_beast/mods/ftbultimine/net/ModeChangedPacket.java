package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Client -> server: cycle to the next/previous shape (sneak + mouse wheel). */
public class ModeChangedPacket implements IMessage
{
	private boolean next;

	public ModeChangedPacket()
	{
	}

	public ModeChangedPacket(boolean n)
	{
		next = n;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		next = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(next);
	}

	public static class Handler implements IMessageHandler<ModeChangedPacket, IMessage>
	{
		@Override
		public IMessage onMessage(final ModeChangedPacket message, MessageContext ctx)
		{
			final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

			FTBUltimine.instance.modeChanged(player, message.next);

			return null;
		}
	}
}
