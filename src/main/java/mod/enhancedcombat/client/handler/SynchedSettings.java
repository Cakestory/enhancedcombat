package mod.enhancedcombat.client.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
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
@SideOnly(Side.CLIENT)
public class SynchedSettings {

	protected static SynchedSettings INSTANCE = new SynchedSettings();

	private Map<EnumWhitelistType, List<Object>> mainhandWeapons = new EnumMap<>(EnumWhitelistType.class);
	private Map<EnumWhitelistType, List<Object>> offhandWeapons = new EnumMap<>(EnumWhitelistType.class);
	private Map<EnumBlacklistType, List<Object>> offhandBlacklist = new EnumMap<>(EnumBlacklistType.class);

	protected boolean isFistWeapon;
	protected boolean requireFullEnergy;
	protected boolean refoundEnergy;
	protected boolean moreSprint;
	protected boolean critHitOnGround;

	protected double offHandEfficiency;
	protected double critChance;
	protected double attackWidth;

	protected int attackLength;

	public Map<EnumWhitelistType, List<Object>> getMainhandWeapons() {
		return mainhandWeapons;
	}

	public Map<EnumWhitelistType, List<Object>> getOffhandWeapons() {
		return offhandWeapons;
	}

	public Map<EnumBlacklistType, List<Object>> getOffhandBlacklist() {
		return offhandBlacklist;
	}

	public boolean isFistWeapon() {
		return isFistWeapon;
	}

	public boolean isRequireFullEnergy() {
		return requireFullEnergy;
	}

	public boolean isRefoundEnergy() {
		return refoundEnergy;
	}

	public boolean isMoreSprint() {
		return moreSprint;
	}

	public boolean isCritHitOnGround() {
		return critHitOnGround;
	}

	public double getOffHandEfficiency() {
		return offHandEfficiency;
	}

	public double getCritChance() {
		return critChance;
	}

	public double getAttackWidth() {
		return attackWidth;
	}

	public int getAttackLength() {
		return attackLength;
	}

	protected void setFistWeapon(boolean isFistWeapon) {
		this.isFistWeapon = isFistWeapon;
	}

	protected void setRequireFullEnergy(boolean requireFullEnergy) {
		this.requireFullEnergy = requireFullEnergy;
	}

	protected void setRefoundEnergy(boolean refoundEnergy) {
		this.refoundEnergy = refoundEnergy;
	}

	protected void setMoreSprint(boolean moreSprint) {
		this.moreSprint = moreSprint;
	}

	protected void setCritHitOnGround(boolean critHitOnGround) {
		this.critHitOnGround = critHitOnGround;
	}

	protected void setOffHandEfficiency(double offHandEfficiency) {
		this.offHandEfficiency = offHandEfficiency;
	}

	protected void setCritChance(double critChance) {
		this.critChance = critChance;
	}

	protected void setAttackWidth(double attackWidth) {
		this.attackWidth = attackWidth;
	}

	protected void setAttackLength(int attackLength) {
		this.attackLength = attackLength;
	}

	protected void convertWeaponsArrayToMap(final EnumListType listType, final String[] list) {

		if (listType == null) {
			EnhancedCombat.LOG.log(Level.WARN, "List type could not be resolved. A packet might have been corrupted.");
			return;
		}

		if (list == null || list.length == 0) {
			EnhancedCombat.LOG.log(Level.WARN, "Received empty list from server. A packet might have been corrupted.");
			return;
		}

		switch (listType) {
		case MAINHAND: {
			Arrays.stream(EnumWhitelistType.VALUES).forEach(t -> mainhandWeapons.put(t, new ArrayList<>()));

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
								mainhandWeapons.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {

							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));

							if (item != null) {
								if (metadata == null)
									mainhandWeapons.get(type).add(new ItemStack(item, 1));
								else
									mainhandWeapons.get(type).add(new ItemStack(item, 1, metadata));
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
		case OFFHAND: {

			Arrays.stream(EnumWhitelistType.VALUES).forEach(t -> offhandWeapons.put(t, new ArrayList<>()));

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
								offhandWeapons.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {
							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
							if (item != null) {
								if (metadata == null)
									offhandWeapons.get(type).add(new ItemStack(item, 1));
								else
									offhandWeapons.get(type).add(new ItemStack(item, 1, metadata));
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
		case OFFHANDBLACKLIST: {

			Arrays.stream(EnumBlacklistType.VALUES).forEach(t -> offhandBlacklist.put(t, new ArrayList<>()));

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
								offhandBlacklist.get(type).add(EnumAction.valueOf(value));
							} catch (IllegalArgumentException ex) {
								EnhancedCombat.LOG.log(Level.WARN, String.format("Unknown action type: %s", value));
							}
						}
							break;
						case CLASS: {
							try {
								offhandBlacklist.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case ENTITYCLASS: {
							try {
								offhandBlacklist.get(type).add(Class.forName(value));
							} catch (ClassNotFoundException ignored) {
							}
						}
							break;
						case NAME: {
							Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
							if (item != null) {
								if (metadata == null)
									offhandBlacklist.get(type).add(new ItemStack(item, 1));
								else
									offhandBlacklist.get(type).add(new ItemStack(item, 1, metadata));
							}
						}
							break;
						case ENTITYNAME: {
							Class<?> cls = EntityList.getClass(new ResourceLocation(value));
							if (cls != null) {
								offhandBlacklist.get(type).add(cls);
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
		}
	}

	private void convertWeaponsArrayToMap(String[] mainhandWeapons, String[] offhandWeapons, String[] offhandBlacklist) {
		convertWeaponsArrayToMap(EnumListType.MAINHAND, mainhandWeapons);
		convertWeaponsArrayToMap(EnumListType.OFFHAND, offhandWeapons);
		convertWeaponsArrayToMap(EnumListType.OFFHANDBLACKLIST, offhandBlacklist);
	}

	/**
	 * Initialize variables in postInit event. Load the client settings until a
	 * connection to a server is made and settings get synced.
	 */
	public static void init() {
		INSTANCE.convertWeaponsArrayToMap(ModConfig.settings.mainhandWeapons, ModConfig.settings.offhandWeapons,
				ModConfig.settings.offhandBlacklist);
	}

	public static enum EnumListType {
		MAINHAND, OFFHAND, OFFHANDBLACKLIST;

		public static EnumListType getListForString(String name) {
			switch (name) {

			case "MAINHAND":
				return EnumListType.MAINHAND;
			case "OFFHAND":
				return EnumListType.OFFHAND;
			case "OFFHANDBLACKLIST":
				return EnumListType.OFFHANDBLACKLIST;
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

}
