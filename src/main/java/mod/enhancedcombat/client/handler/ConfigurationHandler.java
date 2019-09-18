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

		return (itemstack.isEmpty() && INSTANCE.isFistWeapon()) || INSTANCE.getMainhandWeapons().get(EnumWhitelistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(itemstack.getItem()))
				|| INSTANCE.getMainhandWeapons().get(EnumWhitelistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(itemstack, (ItemStack) n));
	}

	public static boolean isOffhandItemAttackUsable(final ItemStack offhandItemStack, final ItemStack mainhandItemStack) {

		if (INSTANCE.getOffhandBlacklist().get(EnumBlacklistType.ACTION).stream().anyMatch(a -> a == mainhandItemStack.getItemUseAction())) {
			return false;
		}
		if (INSTANCE.getOffhandBlacklist().get(EnumBlacklistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(mainhandItemStack.getItem()))) {
			return false;
		}

		if (INSTANCE.getOffhandBlacklist().get(EnumBlacklistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(mainhandItemStack, (ItemStack) n))) {
			return false;
		}

		return INSTANCE.getOffhandWeapons().get(EnumWhitelistType.CLASS).stream().anyMatch(c -> ((Class<?>) c).isInstance(offhandItemStack.getItem())) || INSTANCE.getOffhandWeapons().get(EnumWhitelistType.NAME).stream().anyMatch(n -> ItemStack.areItemsEqual(offhandItemStack, (ItemStack) n));
	}

	public static boolean isEntityAttackableMainhand(final Entity entity) {

		return (ModConfig.settings.playerFriendly && entity instanceof EntityPlayer) ? false : true;
	}

	public static boolean isEntityAttackableOffhand(final Entity entity) {

		if (ModConfig.settings.playerFriendly && entity instanceof EntityPlayer)
			return false;

		return INSTANCE.getOffhandBlacklist().get(EnumBlacklistType.ENTITYCLASS).stream().noneMatch(c -> ((Class<?>) c).isInstance(entity)) && INSTANCE.getOffhandBlacklist().get(EnumBlacklistType.ENTITYNAME).stream().noneMatch(c -> ((Class<?>) c).isInstance(entity));
	}
}