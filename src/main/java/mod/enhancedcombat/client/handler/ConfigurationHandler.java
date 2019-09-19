package mod.enhancedcombat.client.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mod.enhancedcombat.util.ModConfig;

@SideOnly(Side.CLIENT)
public class ConfigurationHandler extends SynchedSettings {

	public static boolean isMainhandItemAttackUsable(final ItemStack itemstack) {

		return (itemstack.isEmpty() && INSTANCE.getSyncedBoolean("isFistWeapon") || INSTANCE.getSyncedWhitelist(EnumSettingsType.LIST_MAINHAND).get(EnumWhitelistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(itemstack.getItem()))
				|| INSTANCE.getSyncedWhitelist(EnumSettingsType.LIST_MAINHAND).get(EnumWhitelistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(itemstack, (ItemStack) n)));
	}

	public static boolean isOffhandItemAttackUsable(final ItemStack offhandItemStack, final ItemStack mainhandItemStack) {

		if (INSTANCE.getSyncedBlacklist(EnumSettingsType.LIST_OFFHANDBLACKLIST).get(EnumBlacklistType.ACTION).stream().anyMatch(a -> a == mainhandItemStack.getItemUseAction())) {
			return false;
		}
		if (INSTANCE.getSyncedBlacklist(EnumSettingsType.LIST_OFFHANDBLACKLIST).get(EnumBlacklistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(mainhandItemStack.getItem()))) {
			return false;
		}

		if (INSTANCE.getSyncedBlacklist(EnumSettingsType.LIST_OFFHANDBLACKLIST).get(EnumBlacklistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(mainhandItemStack, (ItemStack) n))) {
			return false;
		}

		return INSTANCE.getSyncedWhitelist(EnumSettingsType.LIST_OFFHAND).get(EnumWhitelistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(offhandItemStack.getItem())) || INSTANCE.getSyncedWhitelist(EnumSettingsType.LIST_OFFHAND).get(EnumWhitelistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(offhandItemStack, (ItemStack) n));
	}

	public static boolean isEntityAttackableMainhand(final Entity entity) {

		return (ModConfig.settings.playerFriendly && entity instanceof EntityPlayer) ? false : true;
	}

	public static boolean isEntityAttackableOffhand(final Entity entity) {

		if (ModConfig.settings.playerFriendly && entity instanceof EntityPlayer)
			return false;

		return INSTANCE.getSyncedBlacklist(EnumSettingsType.LIST_OFFHANDBLACKLIST).get(EnumBlacklistType.ENTITYCLASS).stream().noneMatch(c -> ((Class<?>) c).isInstance(entity)) && INSTANCE.getSyncedBlacklist(EnumSettingsType.LIST_OFFHANDBLACKLIST).get(EnumBlacklistType.ENTITYNAME).stream().noneMatch(c -> ((Class<?>) c).isInstance(entity));
	}
}