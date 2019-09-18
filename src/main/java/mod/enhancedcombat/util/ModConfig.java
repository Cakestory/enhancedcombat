package mod.enhancedcombat.util;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = Reference.MOD_ID)
@Config.LangKey(Reference.MOD_ID + ".config.title")
public class ModConfig {

	public static final Settings settings = new Settings();

	public static class Settings {

		@Config.Name("Mainhand Weapons")
		@Config.Comment({
				"A list of weapons that are affected by this mod when equipped in the mainhand slot.",
				"All other weapons will still function normally, but will not be affected by this mod´s features (e.g. longer/wider attack range)",
				"Formatting:",
				"CLASS:package.className - Affect mainhand item that is an instance of this class. (The full class name inlcuding path is required!)",
				"NAME:modId:itemName:optionalMetadata -  Affected mainhand item of this item type and metadata. (The name is identical to /give command.)" })

		public String[] mainhandWeapons = new String[] { "CLASS:net.minecraft.item.ItemSword", "CLASS:net.minecraft.item.ItemAxe", "CLASS:slimeknights.tconstruct.library.tools.SwordCore", "CLASS:slimeknights.tconstruct.library.tools.AoeToolCore" };

		@Config.Name("Offhand Weapons")
		@Config.Comment({
				"A list of weapons that are affected by this mod when equipped in the offhand slot. (Should be identical to mainhand list unless you want a weapon to be mainhand or offhand only.)",
				"Formatting:",
				"CLASS:package.className - Affect offhand item that is an instance of this class. (The full class name inlcuding path is required!)",
				"NAME:modId:itemName:optionalMetadata - Affected offhand item of this item type and metadata. (The name is identical to /give command.)" })

		public String[] offhandWeapons = new String[] { "CLASS:net.minecraft.item.ItemSword", "CLASS:net.minecraft.item.ItemAxe", "CLASS:slimeknights.tconstruct.library.tools.SwordCore", "CLASS:slimeknights.tconstruct.library.tools.AoeToolCore" };

		@Config.Name("Offhand Blacklist")
		@Config.Comment({
				"This list by default contains actions and entities that should prevent accidental offhand attacks when trying to interact. You should not remove any entries unless you know what you are doing!",
				"Formatting:",
				"CLASS:package.className - Equipped mainhand item that is an instance of this class will prevent offhand attacks. (The full class name inlcuding path is required!)",
				"NAME:modId:itemName:optionalMetadata - Equipped mainhand item of this item type and metadata prevent offhand attacks. (The name is identical to /give command.)",
				"ACTION:type - Equipped mainhand item that perform this action prevent offhand attacks. (For Modders see: net.minecraft.item.EnumAction)",
				"ENTITYCLASS:package.className - Aiming at an entity that is an instance this class will prevent offhand attacks. (By default: pets, armorstands, horses and villagers) ",
				"ENTITYNAME:name - Aiming at this type of entity will prevent offhand attacks. (The name is identical to /summon command.)" })

		public String[] offhandBlacklist = new String[] {
				"ACTION:BOW",
				"ACTION:EAT",
				"ACTION:DRINK",
				"ACTION:BLOCK",
				"ENTITYCLASS:net.minecraft.entity.IEntityOwnable",
				"ENTITYCLASS:net.minecraft.entity.item.EntityArmorStand",
				"ENTITYCLASS:net.minecraft.entity.passive.EntityVillager",
				"ENTITYCLASS:net.minecraft.entity.passive.AbstractHorse",
				"NAME:minecraft:ender_pearl",
				"NAME:minecraft:ender_eye",
				"NAME:minecraft:snowball",
				"NAME:minecraft:fire_charge",
				"NAME:minecraft:flint_and_steel",
				"NAME:minecraft:egg",
				"NAME:minecraft:writable_book",
				"NAME:minecraft:name_tag",
				"NAME:minecraft:lead" };

		@Config.Name("Fist as Weapon")
		@Config.Comment({ "Shall mainhand melee attacks with a bare hand (no item) be affected by the mod?" })
		public boolean isFistWeapon = false;

		@Config.Name("Require Full Energy")
		@Config.Comment({ "Attacks with a weapons require full energy. - MAY NOT WORK FOR MODDED WEAPONS! (e.g. Tinker�s Construct)" })
		public boolean requireFullEnergy = false;

		@Config.Name("Melee Sprint")
		@Config.Comment({ "Enables sprinting while performing melee attacks." })
		public boolean moreSprint = true;

		@Config.Name("Refound On Miss")
		@Config.Comment({ "Melee energy is not consumed when missing a hit." })
		public boolean refoundEnergy = true;

		@Config.Name("Critical Hit On Ground")
		@Config.Comment({ "If set to true critical hits are ONLY granted when the player is on the ground. Setting this to false will use vanilla behaviour and critical hits will require the player to be airborne. - MAY NOT WORK FOR MODDED WEAPONS! (e.g. Tinker�s Construct)" })
		public boolean critHitOnGround = true;

		@Config.Name("Offhand Multiplier")
		@Config.Comment({ "Defines the strength of offhand attacks compared to mainhand. By default (0.5) attacks will do 50% damage. Set to 0 to disable offhand attacks." })
		@RangeDouble(min = 0, max = 1.0)
		public double offHandEfficiency = 0.5D;

		@Config.Name("Critical Hit Chance")
		@Config.Comment({
				"Defines the chance to land a critical hit. Set to 1 to make all melee attacks land a critical hit. Set to 0 to restore vanilla behaviour (basically every airborne hit is critical). The 'Critical Hit On Ground' setting will then be ignored. - MAY NOT WORK FOR MODDED WEAPONS! (e.g. Tinker�s Construct)" })
		@RangeDouble(min = 0.0, max = 1.0)
		public double critChance = 0.2D;

		@Config.Name("Attack Width Multiplier")
		@Config.Comment({
				"Multiplies the melee attack width by the provided value. Vanilla multiplier is 1. Values below 1 can cause even more missed attacks then vanilla settings. Values above 2 hardly require aiming and could lead to attacking the wrong entity. Set to 0 to disable wider attacks." })
		@RangeDouble(min = 0, max = 5.0)
		public double attackWidth = 1.25D;

		@Config.Name("Additional Attack Length")
		@Config.Comment({ "Increases the melee attack range by the provided number of blocks. Setting this to 0 will default to vanilla range." })
		@RangeInt(min = 0, max = 16)
		public int attackLength = 1;

		@Config.Name("Hit Sound")
		@Config.Comment({ "Play a hit soundeffect when hitting a mob or player." })
		public boolean hitSound = true;

		@Config.Name("Critical Hit Sound")
		@Config.Comment({ "Play a critical hit soundeffect when performing a crit attack." })
		public boolean critSound = true;

		@Config.Name("More Swipes")
		@Config.Comment({ "All valid weapon items play the swipe animation" })
		public boolean moreSwipe = true;

		@Config.Name("Player Friendly")
		@Config.Comment({ "Disable attacks on any player. Meant to avoid accidently hitting your fellow players. Can be toggled with an optional keybind. Default: P" })
		public boolean playerFriendly = false;

	}
}
