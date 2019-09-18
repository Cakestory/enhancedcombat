package mod.enhancedcombat.network;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import mod.enhancedcombat.client.handler.SynchedSettings;
import mod.enhancedcombat.client.handler.SynchedSettings.EnumListType;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Receives a list from the server´s configuration settings. The list´s name
 * must be provided as (string) parameter.
 */
public class PacketSettingList implements IMessage {

	EnumListType type;
	String[] list;

	int listElements = 0;

	public PacketSettingList() {
	}

	public PacketSettingList(EnumListType type, String[] list) {
		this.type = type;
		this.list = list;
	}

	@Override
	public void fromBytes(ByteBuf buf) {

		ArrayList<String> receivedList = new ArrayList<String>();

		type = EnumListType.getListForString(ByteBufUtils.readUTF8String(buf)); // Read the name of this list and convert back to enum

		this.listElements = ByteBufUtils.readVarInt(buf, 4); // Read the number of list elements

		for (int i = 0; i < listElements; i++) {
			receivedList.add(ByteBufUtils.readUTF8String(buf)); // Read all list elements
		}

		if (receivedList.size() > 0) {
			this.list = receivedList.toArray(new String[receivedList.size()]); // Convert ArrayList to String[]
		}

	}

	@Override
	public void toBytes(ByteBuf buf) {

		ByteBufUtils.writeUTF8String(buf, this.type.toString()); // Write the name of this list as a string

		ByteBufUtils.writeVarInt(buf, this.list.length, 4); // Write the number of list elements that will be send

		for (String s : this.list)
			ByteBufUtils.writeUTF8String(buf, s); // Sends all list elements as single UTF8 strings
	}

	@SideOnly(Side.CLIENT)
	public static class ClientHandler extends SynchedSettings implements IMessageHandler<PacketSettingList, IMessage> {

		@Override
		public IMessage onMessage(PacketSettingList message, MessageContext ctx) {
			convertWeaponsArrayToMap(message.type, message.list);
			return null;
		}
	}

}
