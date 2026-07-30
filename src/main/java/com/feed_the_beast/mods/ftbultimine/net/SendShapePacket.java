package com.feed_the_beast.mods.ftbultimine.net;

import com.feed_the_beast.mods.ftbultimine.BlockPos;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineClientData;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server -> client. The original only sent the shape id, because block
 * highlighting was done by an ASM hook in RenderGlobal. This port has no
 * coremod, so the block list travels with the packet and the client renders it.
 */
public class SendShapePacket implements IMessage
{
	public String id = "";
	public List<BlockPos> blocks = Collections.emptyList();

	public SendShapePacket()
	{
	}

	public SendShapePacket(String i, List<BlockPos> b)
	{
		id = i == null ? "" : i;
		blocks = b == null ? Collections.<BlockPos>emptyList() : b;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		id = ByteBufUtils.readUTF8String(buf);
		int size = buf.readInt();
		List<BlockPos> list = new ArrayList<BlockPos>(Math.max(0, size));

		for (int i = 0; i < size; i++)
		{
			list.add(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
		}

		blocks = list;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, id);
		buf.writeInt(blocks.size());

		for (BlockPos pos : blocks)
		{
			buf.writeInt(pos.x);
			buf.writeInt(pos.y);
			buf.writeInt(pos.z);
		}
	}

	public static class Handler implements IMessageHandler<SendShapePacket, IMessage>
	{
		@Override
		public IMessage onMessage(SendShapePacket message, MessageContext ctx)
		{
			FTBUltimineClientData.set(message.id, message.blocks);
			return null;
		}
	}
}
