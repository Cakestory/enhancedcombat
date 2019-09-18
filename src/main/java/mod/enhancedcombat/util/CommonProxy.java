package mod.enhancedcombat.util;

import mod.enhancedcombat.capability.StorageOffHandAttack;
import mod.enhancedcombat.capability.StorageSecondHurtTimer;
import mod.enhancedcombat.combat.DefaultImplOffHandAttack;
import mod.enhancedcombat.combat.DefaultImplSecondHurtTimer;
import mod.enhancedcombat.combat.IOffHandAttack;
import mod.enhancedcombat.combat.ISecondHurtTimer;
import mod.enhancedcombat.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CommonProxy {
	public void preInit() {
		PacketHandler.registerMessages(Reference.MOD_ID);
		CapabilityManager.INSTANCE.register(IOffHandAttack.class, new StorageOffHandAttack(), DefaultImplOffHandAttack::new);
		CapabilityManager.INSTANCE.register(ISecondHurtTimer.class, new StorageSecondHurtTimer(), DefaultImplSecondHurtTimer::new);
	}

	public void init() {

	}

	public void spawnSweep(EntityPlayer player) {
	}

	public void postInit() {

	}
}