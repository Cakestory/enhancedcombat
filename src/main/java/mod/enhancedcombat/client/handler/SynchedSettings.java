package mod.enhancedcombat.client.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import mod.enhancedcombat.EnhancedCombat;
import mod.enhancedcombat.util.ModConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * All child classes of this parent use variables synced from the server
 * configuration file on connection.
 */
public class SynchedSettings {

	private static Map<String, Boolean> settingsBoolean = new HashMap<String, Boolean>();
	private static Map<String, Double> settingsDouble = new HashMap<String, Double>();
	private static Map<String, Integer> settingsInteger = new HashMap<String, Integer>();

	public SynchedSettings() {

		// Put all boolean values that must be synchronized here!
		settingsBoolean.put("isFistWeapon", ModConfig.settings.isFistWeapon);
		settingsBoolean.put("requireFullEnergy", ModConfig.settings.requireFullEnergy);
		settingsBoolean.put("refoundEnergy", ModConfig.settings.refoundEnergy);
		settingsBoolean.put("moreSprint", ModConfig.settings.moreSprint);
		settingsBoolean.put("critHitOnGround", ModConfig.settings.critHitOnGround);

		// Put all double values that must be synchronized here!
		settingsInteger.put("attackLength", ModConfig.settings.attackLength);

		// Put all double values that must be synchronized here!
		settingsDouble.put("offHandEfficiency", ModConfig.settings.offHandEfficiency);
		settingsDouble.put("critChance", ModConfig.settings.critChance);
		settingsDouble.put("attackWidth", ModConfig.settings.attackWidth);
	}

	protected static Map<String, Boolean> getBooleanSettings() {
		return settingsBoolean;
	}

	protected static Map<String, Integer> getIntegerSettings() {
		return settingsInteger;
	}

	protected static Map<String, Double> getDoubleSettings() {
		return settingsDouble;
	}

	public static enum EnumSettingsType {
		INTEGER, BOOLEAN, DOUBLE, LIST_MAINHAND, LIST_OFFHAND, LIST_OFFHANDBLACKLIST;

		public static boolean isList(EnumSettingsType type) {
			return (type == LIST_MAINHAND || type == LIST_OFFHAND || type == LIST_OFFHANDBLACKLIST);
		}

		public static EnumSettingsType getType(String name) {

			switch (name) {
			case "INTEGER":
				return EnumSettingsType.INTEGER;
			case "BOOLEAN":
				return EnumSettingsType.BOOLEAN;
			case "DOUBLE":
				return EnumSettingsType.DOUBLE;
			case "LIST_MAINHAND":
				return EnumSettingsType.LIST_MAINHAND;
			case "OFFHAND":
				return EnumSettingsType.LIST_OFFHAND;
			case "OFFHANDBLACKLIST":
				return EnumSettingsType.LIST_OFFHANDBLACKLIST;
			}

			return null;
		}

	}

	protected enum EnumWhitelistType {
		CLASS, NAME;

		public static final EnumWhitelistType[] VALUES = values();
	}

	protected enum EnumBlacklistType {
		ACTION, CLASS, NAME, ENTITYCLASS, ENTITYNAME;

		public static final EnumBlacklistType[] VALUES = values();
	}

	// All fields and methods below must be client only
	@SideOnly(Side.CLIENT)
	protected static SynchedSettings INSTANCE = new SynchedSettings();

	@SideOnly(Side.CLIENT)
	private Map<EnumWhitelistType, List<Object>> syncedMainhandWeapons = new EnumMap<>(EnumWhitelistType.class);
	@SideOnly(Side.CLIENT)
	private Map<EnumWhitelistType, List<Object>> syncedOffhandWeapons = new EnumMap<>(EnumWhitelistType.class);
	@SideOnly(Side.CLIENT)
	private Map<EnumBlacklistType, List<Object>> syncedOffhandBlacklist = new EnumMap<>(EnumBlacklistType.class);

	@SideOnly(Side.CLIENT)
	public boolean getSyncedBoolean(String name) {
		return settingsBoolean.get(name);
	}

	@SideOnly(Side.CLIENT)
	public int getSyncedInteger(String name) {
		return settingsInteger.get(name);
	}

	@SideOnly(Side.CLIENT)
	public double getSyncedDouble(String name) {
		return settingsDouble.get(name);
	}

	@SideOnly(Side.CLIENT)
	public Map<EnumWhitelistType, List<Object>> getSyncedWhitelist(EnumSettingsType type) {
		switch (type) {
		case LIST_MAINHAND:
			return syncedMainhandWeapons;
		case LIST_OFFHAND:
			return syncedOffhandWeapons;
		default:
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	public Map<EnumBlacklistType, List<Object>> getSyncedBlacklist(EnumSettingsType type) {
		switch (type) {
		case LIST_OFFHANDBLACKLIST:
			return syncedOffhandBlacklist;
		default:
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	private void convertWeaponsArrayToMap(final EnumSettingsType listType, final String[] list) {

		if (listType == null) {
			EnhancedCombat.LOG.log(Level.WARN, "List type could not be resolved. A packet might have been corrupted.");
			return;
		}

		if (list == null || list.length == 0) {
			EnhancedCombat.LOG.log(Level.WARN, "Received empty list from server. A packet might have been corrupted.");
			return;
		}

		switch (listType) {
		case LIST_MAINHAND: {
			Arrays.stream(EnumWhitelistType.VALUES).forEach(t -> syncedMainhandWeapons.put(t, new ArrayList<>()));

			Arrays.stream(list).forEach(s -> {
				int colonIndex = s.indexOf(':');
				if (colonIndex > 0) {
					String typeStr = s.substring(0, colonIndex);
					try {
						EnumWhitelistType type = EnumWhitelistType.valueOf(typeStr);
						String value = s.substring(colonIndex + 1);
						Integer metadata = null;

						// Check if the value contains metadata (damage) information
						if (StringUtils.countMatches(value, ':') == 2) {
							value = value.substring(0, value.lastIndexOf(':'));
							try {
								metadata = Integer.parseInt(s.substring(s.lastIndexOf(':') + 1, s.length()));
							} catch (NumberFormatException ignored) {
							}
						}

						switch (type) {
						case CLASS: {
							try {
								syncedMainhandWeapons.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {

							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));

							if (item != null) {
								if (metadata == null)
									syncedMainhandWeapons.get(type).add(new ItemStack(item, 1));
								else
									syncedMainhandWeapons.get(type).add(new ItemStack(item, 1, metadata));
							}
						}
							break;
						}
					} catch (IllegalArgumentException ex) {
						EnhancedCombat.LOG.log(Level.WARN, String.format("Unknown whitelist type: %s", typeStr));
					}
				}
			});

		}
			break;
		case LIST_OFFHAND: {

			Arrays.stream(EnumWhitelistType.VALUES).forEach(t -> syncedOffhandWeapons.put(t, new ArrayList<>()));

			Arrays.stream(list).forEach(s -> {
				int colonIndex = s.indexOf(':');
				if (colonIndex > 0) {
					String typeStr = s.substring(0, colonIndex);
					try {
						EnumWhitelistType type = EnumWhitelistType.valueOf(typeStr);
						String value = s.substring(colonIndex + 1);
						Integer metadata = null;

						// Check if the value contains metadata (damage) information
						if (StringUtils.countMatches(value, ':') == 2) {
							value = value.substring(0, value.lastIndexOf(':'));
							try {
								metadata = Integer.parseInt(s.substring(s.lastIndexOf(':') + 1, s.length()));
							} catch (NumberFormatException ignored) {
							}
						}

						switch (type) {
						case CLASS: {
							try {
								syncedOffhandWeapons.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {
							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
							if (item != null) {
								if (metadata == null)
									syncedOffhandWeapons.get(type).add(new ItemStack(item, 1));
								else
									syncedOffhandWeapons.get(type).add(new ItemStack(item, 1, metadata));
							}
						}
							break;
						}
					} catch (IllegalArgumentException ex) {
						EnhancedCombat.LOG.log(Level.WARN, String.format("Unknown whitelist type: %s", typeStr));
					}
				}
			});
		}
			break;
		case LIST_OFFHANDBLACKLIST: {

			Arrays.stream(EnumBlacklistType.VALUES).forEach(t -> syncedOffhandBlacklist.put(t, new ArrayList<>()));

			Arrays.stream(list).forEach(s -> {
				int colonIndex = s.indexOf(':');
				if (colonIndex > 0) {
					String typeStr = s.substring(0, colonIndex);
					try {
						EnumBlacklistType type = EnumBlacklistType.valueOf(typeStr);
						String value = s.substring(colonIndex + 1);
						Integer metadata = null;

						// Check if the value contains metadata (damage) information
						if (StringUtils.countMatches(value, ':') == 2) {
							value = value.substring(0, value.lastIndexOf(':'));
							try {
								metadata = Integer.parseInt(s.substring(s.lastIndexOf(':') + 1, s.length()));
							} catch (NumberFormatException ignored) {
							}
						}

						switch (type) {
						case ACTION: {
							try {
								syncedOffhandBlacklist.get(type).add(EnumAction.valueOf(value));
							} catch (IllegalArgumentException ex) {
								EnhancedCombat.LOG.log(Level.WARN, String.format("Unknown action type: %s", value));
							}
						}
							break;
						case CLASS: {
							try {
								syncedOffhandBlacklist.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case ENTITYCLASS: {
							try {
								syncedOffhandBlacklist.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {
							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
							if (item != null) {
								if (metadata == null)
									syncedOffhandBlacklist.get(type).add(new ItemStack(item, 1));
								else
									syncedOffhandBlacklist.get(type).add(new ItemStack(item, 1, metadata));
							}
						}
							break;
						case ENTITYNAME: {
							Class<?> cls = EntityList.getClass(new ResourceLocation(value));
							if (cls != null) {
								syncedOffhandBlacklist.get(type).add(cls);
							}
						}
							break;
						}
					} catch (IllegalArgumentException ex) {
						EnhancedCombat.LOG.log(Level.WARN, String.format("Unknown blacklist type: %s", typeStr));
					}
				}
			});
		}
		default: // Other (non-list) types do not matter here
			break;
		}
	}

	@SideOnly(Side.CLIENT)
	private void convertWeaponsArrayToMap(String[] mainhandWeapons, String[] offhandWeapons, String[] offhandBlacklist) {
		convertWeaponsArrayToMap(EnumSettingsType.LIST_MAINHAND, mainhandWeapons);
		convertWeaponsArrayToMap(EnumSettingsType.LIST_OFFHAND, offhandWeapons);
		convertWeaponsArrayToMap(EnumSettingsType.LIST_OFFHANDBLACKLIST, offhandBlacklist);
	}

	/**
	 * Initialize variables in postInit event. Load the client settings until a
	 * connection to a server is made and settings get synced.
	 */
	@SideOnly(Side.CLIENT)
	public static void init() {
		INSTANCE.convertWeaponsArrayToMap(ModConfig.settings.mainhandWeapons, ModConfig.settings.offhandWeapons, ModConfig.settings.offhandBlacklist);
	}

	@SideOnly(Side.CLIENT)
	protected void syncList(EnumSettingsType type, String[] list) {
		convertWeaponsArrayToMap(type, list);
	}

	@SideOnly(Side.CLIENT)
	protected void syncInteger(String name, int value) {
		settingsInteger.replace(name, value);
	}

	@SideOnly(Side.CLIENT)
	protected void syncBoolean(String name, boolean value) {
		settingsBoolean.replace(name, value);
	}

	@SideOnly(Side.CLIENT)
	protected void syncDouble(String name, double value) {
		settingsDouble.replace(name, value);
	}

}
