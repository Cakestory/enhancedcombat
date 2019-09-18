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

	private Map<WhitelistType, List<Object>> mainhandWeapons = new EnumMap<>(WhitelistType.class);
	protected Map<WhitelistType, List<Object>> offhandWeapons = new EnumMap<>(WhitelistType.class);
	protected Map<BlacklistType, List<Object>> offhandBlacklist = new EnumMap<>(BlacklistType.class);

	protected boolean isFistWeapon;
	protected boolean requireFullEnergy;
	protected boolean refoundEnergy;
	protected boolean moreSprint;
	protected boolean critHitOnGround;

	protected double offHandEfficiency;
	protected double critChance;
	protected double attackWidth;

	protected int attackLength;

	public Map<WhitelistType, List<Object>> getMainhandWeapons() {
		return mainhandWeapons;
	}

	public Map<WhitelistType, List<Object>> getOffhandWeapons() {
		return offhandWeapons;
	}

	public Map<BlacklistType, List<Object>> getOffhandBlacklist() {
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

	private void updateWeaponsLists(final String[] syncedMainhandWeapons, final String[] syncedOffhandWeapons, final String[] syncedOffhandBlacklist) {
		Arrays.stream(WhitelistType.VALUES).forEach(t -> mainhandWeapons.put(t, new ArrayList<>()));
		Arrays.stream(WhitelistType.VALUES).forEach(t -> offhandWeapons.put(t, new ArrayList<>()));
		Arrays.stream(BlacklistType.VALUES).forEach(t -> offhandBlacklist.put(t, new ArrayList<>()));

		Arrays.stream(syncedMainhandWeapons).forEach(s -> {
			int colonIndex = s.indexOf(':');
			if (colonIndex > 0) {
				String typeStr = s.substring(0, colonIndex);
				try {
					WhitelistType type = WhitelistType.valueOf(typeStr);
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

		Arrays.stream(syncedOffhandWeapons).forEach(s -> {
			int colonIndex = s.indexOf(':');
			if (colonIndex > 0) {
				String typeStr = s.substring(0, colonIndex);
				try {
					WhitelistType type = WhitelistType.valueOf(typeStr);
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

		Arrays.stream(syncedOffhandBlacklist).forEach(s -> {
			int colonIndex = s.indexOf(':');
			if (colonIndex > 0) {
				String typeStr = s.substring(0, colonIndex);
				try {
					BlacklistType type = BlacklistType.valueOf(typeStr);
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

	/**
	 * Initialize variables in postInit event. Load the client settings until a
	 * connection to a server is made and settings get synced.
	 */
	public static void init() {
		INSTANCE.updateWeaponsLists(ModConfig.settings.mainhandWeapons, ModConfig.settings.offhandWeapons, ModConfig.settings.offhandBlacklist);
	}

	/**
	 * Updates all synced settings variables of this class with values from the
	 * server.
	 */
	protected void syncSettingsWithServer() {
		
		// TODO: Start packet handling
		
		
		updateWeaponsLists(null, null, null); // Change parameters!!!!
	}

	protected enum WhitelistType {
		CLASS, NAME;

		public static final WhitelistType[] VALUES = values();
	}

	protected enum BlacklistType {
		ACTION, CLASS, NAME, ENTITYCLASS, ENTITYNAME;

		public static final BlacklistType[] VALUES = values();
	}

}
