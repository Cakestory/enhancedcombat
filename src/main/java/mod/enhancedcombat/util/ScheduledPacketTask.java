package mod.enhancedcombat.util;

import mod.enhancedcombat.EnhancedCombat;
import mod.enhancedcombat.handler.EventHandlers;
import mod.enhancedcombat.network.PacketOffhandCooldown;
import mod.enhancedcombat.util.Helpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

public class ScheduledPacketTask implements Runnable {
	
	private EntityPlayer player;
	private PacketOffhandCooldown message;

	public ScheduledPacketTask(EntityPlayer player, PacketOffhandCooldown message) {
		this.player = player;
		this.message = message;
	}

	@Override
	public void run() {
		if (this.player == null) {
			return;
		}

		if (ModConfig.settings.moreSwipe || this.player.getHeldItemOffhand().getItem() instanceof ItemSword) {
			EnhancedCombat.proxy.spawnSweep(this.player);
		}

		Helpers.execNullable(this.player.getCapability(EventHandlers.TUTO_CAP, null),
				stg -> stg.setOffhandCooldown(this.message.cooldown));
	}
}