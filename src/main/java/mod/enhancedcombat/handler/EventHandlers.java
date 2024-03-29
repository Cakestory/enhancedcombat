package mod.enhancedcombat.handler;

import mod.enhancedcombat.capability.CapabilityOffhandCooldown;
import mod.enhancedcombat.combat.IOffHandAttack;
import mod.enhancedcombat.combat.ISecondHurtTimer;
import mod.enhancedcombat.network.PacketHandler;
import mod.enhancedcombat.network.PacketSendEnergy;
import mod.enhancedcombat.util.Helpers;
import mod.enhancedcombat.util.ModConfig;
import mod.enhancedcombat.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@SuppressWarnings("rawtypes")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class EventHandlers {
	public static final EventHandlers INSTANCE = new EventHandlers();

	public int energyToGive;
	public boolean giveEnergy;
	public int offhandCooldown;

	@CapabilityInject(IOffHandAttack.class)
	public static final Capability<IOffHandAttack> OFFHAND_CAP = Helpers.getNull();
	@CapabilityInject(ISecondHurtTimer.class)
	public static final Capability<ISecondHurtTimer> SECONDHURTTIMER_CAP = Helpers.getNull();
	@CapabilityInject(CapabilityOffhandCooldown.class)
	public static final Capability<CapabilityOffhandCooldown> TUTO_CAP = Helpers.getNull();

	private EventHandlers() {
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

		if (!event.player.world.isRemote) {
			EntityPlayerMP playerMP = (EntityPlayerMP) event.player;

			Helpers.sendSettingsToClient(playerMP);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Reference.MOD_ID)) {
			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		}
	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		if (event.getTarget() == null) {
			return;
		}
		giveEnergy = false;
		if (event.getTarget().hurtResistantTime <= 10) {
			if (ModConfig.settings.moreSwipe) {
				((EntityPlayer) event.getEntityLiving()).spawnSweepParticles();
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		ISecondHurtTimer sht = event.getEntityLiving().getCapability(SECONDHURTTIMER_CAP, null);
		if (sht != null && sht.getHurtTimerBCM() > 0) {
			sht.tick();
		}

		if (event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			IOffHandAttack oha = event.getEntityLiving().getCapability(OFFHAND_CAP, null);
			CapabilityOffhandCooldown cof = player.getCapability(TUTO_CAP, null);
			Helpers.execNullable(oha, IOffHandAttack::tick);

			if (cof != null) {
				cof.tick();
				if (this.offhandCooldown > 0) {
					cof.setOffhandCooldown(this.offhandCooldown);
					if (!player.world.isRemote) {
						cof.sync();
					}
					this.offhandCooldown = 0;
				}
			}

			if (this.giveEnergy) {
				if (player.ticksSinceLastSwing == 0) {
					player.ticksSinceLastSwing = this.energyToGive;
					this.giveEnergy = false;
					PacketHandler.instance.sendToServer(new PacketSendEnergy(this.energyToGive));
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent<Entity> event) {

		if (event.getGenericType() != Entity.class) {
			return;
		}
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(Reference.MOD_ID, "TUTO_CAP"),
					new CapabilityOffhandCooldown((EntityPlayer) event.getObject()));
		}

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "IOffHandAttack"),
				new ICapabilitySerializable<NBTBase>() {
					IOffHandAttack inst = EventHandlers.OFFHAND_CAP.getDefaultInstance();

					@Override
					public boolean hasCapability(Capability capability, EnumFacing facing) {
						return capability == EventHandlers.OFFHAND_CAP;
					}

					@Override
					public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
						return capability == EventHandlers.OFFHAND_CAP ? EventHandlers.OFFHAND_CAP.cast(this.inst)
								: null;
					}

					@Override
					public NBTPrimitive serializeNBT() {
						return (NBTPrimitive) EventHandlers.OFFHAND_CAP.getStorage().writeNBT(EventHandlers.OFFHAND_CAP,
								this.inst, null);
					}

					@Override
					public void deserializeNBT(NBTBase nbt) {
						EventHandlers.OFFHAND_CAP.getStorage().readNBT(EventHandlers.OFFHAND_CAP, this.inst, null, nbt);
					}
				});

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "ISecondHurtTimer"),
				new ICapabilitySerializable<NBTBase>() {
					ISecondHurtTimer inst = EventHandlers.SECONDHURTTIMER_CAP.getDefaultInstance();

					@Override
					public boolean hasCapability(Capability capability, EnumFacing facing) {
						return capability == EventHandlers.SECONDHURTTIMER_CAP;
					}

					@Override
					public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
						return capability == EventHandlers.SECONDHURTTIMER_CAP
								? EventHandlers.SECONDHURTTIMER_CAP.cast(this.inst)
								: null;
					}

					@Override
					public NBTPrimitive serializeNBT() {
						return (NBTPrimitive) EventHandlers.SECONDHURTTIMER_CAP.getStorage()
								.writeNBT(EventHandlers.SECONDHURTTIMER_CAP, this.inst, null);
					}

					@Override
					public void deserializeNBT(NBTBase nbt) {
						EventHandlers.SECONDHURTTIMER_CAP.getStorage().readNBT(EventHandlers.SECONDHURTTIMER_CAP,
								this.inst, null, nbt);
					}
				});
	}
}