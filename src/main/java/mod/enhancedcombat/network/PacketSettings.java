package mod.enhancedcombat.network;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import io.netty.buffer.ByteBuf;
import mod.enhancedcombat.EnhancedCombat;
import mod.enhancedcombat.client.handler.SynchedSettings;
import mod.enhancedcombat.client.handler.SynchedSettings.EnumSettingsType;
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
public class PacketSettings implements IMessage {

	private EnumSettingsType type;
	private String name;

	private String[] list;
	private Integer listElements = 0;
	private int settingInteger = 0;
	private double settingDouble = 0;
	private boolean settingBoolean = false;

	public PacketSettings() {
	}

	public PacketSettings(String name, int settingInteger) {
		this.name = name;
		this.type = EnumSettingsType.INTEGER;
		this.settingInteger = settingInteger;
	}

	public PacketSettings(String name, double settingDouble) {
		this.name = name;
		this.type = EnumSettingsType.DOUBLE;
		this.settingDouble = settingDouble;
	}

	public PacketSettings(String name, boolean settingBoolean) {
		this.name = name;
		this.type = EnumSettingsType.BOOLEAN;
		this.settingBoolean = settingBoolean;
	}

	public PacketSettings(EnumSettingsType type, String[] settingList) {
		this.type = type;

		if (!EnumSettingsType.isList(type)) {
			EnhancedCombat.LOG.log(Level.ERROR, "PacketSettings: Construction failed! The provided type parameter is not a list!");
			return;
		}

		if (settingList == null || settingList.length == 0) {
			EnhancedCombat.LOG.log(Level.ERROR, "PacketSettings: Construction failed! The provided list parameter is null or an empty array!");
			return;
		}

		this.list = settingList;
	}

	@Override
	public void fromBytes(ByteBuf buf) {

		type = EnumSettingsType.getType(ByteBufUtils.readUTF8String(buf)); // Read type from string

		switch (type) {
		case INTEGER: {
			name = ByteBufUtils.readUTF8String(buf); // Read name
			settingInteger = buf.readInt(); // Read value
		}
			break;
		case DOUBLE: {
			name = ByteBufUtils.readUTF8String(buf); // Read name
			settingDouble = buf.readDouble(); // Read value
		}
			break;
		case BOOLEAN: {
			name = ByteBufUtils.readUTF8String(buf); // Read name
			settingBoolean = buf.readBoolean(); // Read value
		}
			break;
		default:
			if (EnumSettingsType.isList(type)) {

				ArrayList<String> receivedList = new ArrayList<String>();

				this.listElements = ByteBufUtils.readVarInt(buf, 4); // Read the number of list elements

				for (int i = 0; i < listElements; i++) {
					receivedList.add(ByteBufUtils.readUTF8String(buf)); // Read all list elements
				}

				if (receivedList.size() > 0) {
					this.list = receivedList.toArray(new String[receivedList.size()]); // Convert ArrayList to String[]
				}

			} else {
				EnhancedCombat.LOG.log(Level.ERROR, "PacketSettings: Unknown type received! Buffer cannot be read without a correct type!!");
			}
		}

	}

	@Override
	public void toBytes(ByteBuf buf) {

		ByteBufUtils.writeUTF8String(buf, type.toString()); // Write type as string

		switch (type) {
		case INTEGER: {
			ByteBufUtils.writeUTF8String(buf, type.toString()); // Write type as string
			ByteBufUtils.writeUTF8String(buf, name); // Write name
			buf.writeInt(settingInteger); // Write value
		}
			break;
		case DOUBLE: {
			ByteBufUtils.writeUTF8String(buf, type.toString()); // Write type as string
			ByteBufUtils.writeUTF8String(buf, name); // Write name
			buf.writeDouble(settingDouble); // Write value
		}
			break;
		case BOOLEAN: {
			ByteBufUtils.writeUTF8String(buf, type.toString()); // Write type as string
			ByteBufUtils.writeUTF8String(buf, name); // Write name
			buf.writeBoolean(settingBoolean); // Write value
		}
			break;
		default:
			if (EnumSettingsType.isList(type)) {

				ByteBufUtils.writeVarInt(buf, this.list.length, 4); // Write the number of list elements that will be send

				for (String s : this.list)
					ByteBufUtils.writeUTF8String(buf, s); // Sends all list elements as single UTF8 strings

			} else {
				EnhancedCombat.LOG.log(Level.ERROR, "PacketSettings: Unknown type to write! Packet information not written to buffer!!");
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static class ClientHandler extends SynchedSettings implements IMessageHandler<PacketSettings, IMessage> {

		@Override
		public IMessage onMessage(PacketSettings message, MessageContext ctx) {

			switch (message.type) {
			case INTEGER: {
				syncInteger(message.name, message.settingInteger);
			}
				break;
			case DOUBLE: {
				syncDouble(message.name, message.settingDouble);
			}
				break;
			case BOOLEAN: {
				syncBoolean(message.name, message.settingBoolean);
			}
				break;
			default:
				if (EnumSettingsType.isList(message.type)) {
					syncList(message.type, message.list);
				}
			}

			return null;
		}
	}

}
