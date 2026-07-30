package com.feed_the_beast.mods.ftbultimine.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class FTBUltimineNet
{
	public static final SimpleNetworkWrapper MAIN = NetworkRegistry.INSTANCE.newSimpleChannel("ftbultimine");

	public static void init()
	{
		MAIN.registerMessage(KeyPressedPacket.Handler.class, KeyPressedPacket.class, 0, Side.SERVER);
		MAIN.registerMessage(ModeChangedPacket.Handler.class, ModeChangedPacket.class, 1, Side.SERVER);
		MAIN.registerMessage(SendShapePacket.Handler.class, SendShapePacket.class, 2, Side.CLIENT);
	}
}
