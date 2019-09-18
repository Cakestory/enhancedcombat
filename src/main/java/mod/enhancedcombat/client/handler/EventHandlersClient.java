package mod.enhancedcombat.client.handler;

import mod.enhancedcombat.capability.CapabilityOffhandCooldown;
import mod.enhancedcombat.client.gui.GuiCrosshairsBC;
import mod.enhancedcombat.combat.IOffHandAttack;
import mod.enhancedcombat.combat.ISecondHurtTimer;
import mod.enhancedcombat.handler.EventHandlers;
import mod.enhancedcombat.network.PacketHandler;
import mod.enhancedcombat.network.PacketMainhandAttack;
import mod.enhancedcombat.network.PacketOffhandAttack;
import mod.enhancedcombat.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class EventHandlersClient extends SynchedSettings {

	public static final EventHandlersClient INSTANCE = new EventHandlersClient();

	private final GuiCrosshairsBC gc = new GuiCrosshairsBC();

	private EventHandlersClient() {
	}

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void onMouseEvent(MouseEvent event) {
		KeyBinding attack = Minecraft.getMinecraft().gameSettings.keyBindAttack;
		KeyBinding useItem = Minecraft.getMinecraft().gameSettings.keyBindUseItem;
		if (attack.getKeyCode() < 0 && event.getButton() == attack.getKeyCode() + 100 && event.isButtonstate()) {
			onMouseLeftClick(event);
		}
		if (offHandEfficiency > 0 && useItem.getKeyCode() < 0 && event.getButton() == useItem.getKeyCode() + 100 && event.isButtonstate()) {
			onMouseRightClick();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if (offHandEfficiency == 0) {
			return;
		}

		switch (event.getType()) {
		case CROSSHAIRS:
			boolean cancelled = event.isCanceled();
			event.setCanceled(true);
			if (!cancelled) {
				this.gc.renderAttackIndicator(0.5F, new ScaledResolution(Minecraft.getMinecraft()));
				MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(event, event.getType()));
			}
			break;
		default:
			break;
		}
	}

	public static void onMouseLeftClick(MouseEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;

		if (!player.getActiveItemStack().isEmpty()) {
			return;
		}

		boolean isLongerAttack = ConfigurationHandler.isMainhandItemAttackUsable(player.getHeldItemMainhand());

		RayTraceResult mov = getMouseOverExtended(isLongerAttack ? (float) (INSTANCE.getAttackLength() + 4) : 4.0F, false);
		if (mov != null && mov.entityHit != null) {
			if (mov.entityHit != player) {

				event.setCanceled(true);
				if ((INSTANCE.isRequireFullEnergy() && player.getCooledAttackStrength(0.5F) < 1.0F) || !shouldAttack(mov.entityHit, player, false)) {
					return;
				}

				player.isSwingInProgress = true;
				player.swingingHand = EnumHand.MAIN_HAND;

				player.attackTargetEntityWithCurrentItem(mov.entityHit);
				PacketHandler.instance.sendToServer(new PacketMainhandAttack(mov.entityHit.getEntityId()));

				return;
			}
		}

		if (INSTANCE.isRefoundEnergy()) {
			refoundEnergy(player);
		}
	}

	public static void onMouseRightClick() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		if (player != null && !player.isSpectator()) {
			if (!player.getActiveItemStack().isEmpty()) {
				return;
			}

			boolean isLongerAttack = ConfigurationHandler.isMainhandItemAttackUsable(player.getHeldItemOffhand());

			if (INSTANCE.isRequireFullEnergy() && Helpers.execNullable(player.getCapability(EventHandlers.TUTO_CAP, null), CapabilityOffhandCooldown::getOffhandCooldown, 1) > 0) {
				return;
			}
			ItemStack stackOffHand = player.getHeldItemOffhand();

			if (stackOffHand.isEmpty() || !ConfigurationHandler.isOffhandItemAttackUsable(stackOffHand, player.getHeldItemMainhand())) {
				return;
			}

			IOffHandAttack oha = player.getCapability(EventHandlers.OFFHAND_CAP, null);
			RayTraceResult mov = getMouseOverExtended(isLongerAttack ? (float) (INSTANCE.getAttackLength() + 4) : 4.0F, true);

			if (oha != null && (mov == null || mov.typeOfHit == RayTraceResult.Type.MISS || shouldAttack(mov.entityHit, player, true))) {
				oha.swingOffHand(player);
			}

			if (INSTANCE.isRefoundEnergy()) {
				EventHandlers.INSTANCE.offhandCooldown = Helpers.getOffhandCooldown(player);
			}

			if (mov != null && mov.entityHit != null) {

				ISecondHurtTimer sht = mov.entityHit.getCapability(EventHandlers.SECONDHURTTIMER_CAP, null);
				if (sht != null && sht.getHurtTimerBCM() <= 0) {
					if (shouldAttack(mov.entityHit, player, true)) {
						PacketHandler.instance.sendToServer(new PacketOffhandAttack(mov.entityHit.getEntityId()));
						return;
					}
				}
			}

			PacketHandler.instance.sendToServer(new PacketOffhandAttack());
		}
	}

	public static void refoundEnergy(EntityPlayer player) {
		EventHandlers.INSTANCE.giveEnergy = true;
		EventHandlers.INSTANCE.energyToGive = player.ticksSinceLastSwing;
	}

	private static boolean shouldAttack(Entity entHit, EntityPlayer player, boolean offhand) {
		if (entHit == null) {
			return false;
		}

		if (entHit instanceof EntityPlayerMP) {
			return Helpers.execNullable(entHit.getServer(), MinecraftServer::isPVPEnabled, false);
		}

		return !offhand ? ConfigurationHandler.isEntityAttackableMainhand(entHit) : ConfigurationHandler.isEntityAttackableOffhand(entHit);
	}

	public static RayTraceResult getMouseOverExtended(double dist, boolean offhand) {
		Minecraft mc = Minecraft.getMinecraft();
		Entity rvEntity = mc.getRenderViewEntity();
		if (rvEntity == null) {
			return null;
		}

		EntityPlayer player = mc.player;

		boolean isWiderAttack = !offhand ? ConfigurationHandler.isMainhandItemAttackUsable(player.getHeldItemMainhand()) : ConfigurationHandler.isOffhandItemAttackUsable(player.getHeldItemOffhand(), player.getHeldItemMainhand());

		AxisAlignedBB viewBB = new AxisAlignedBB(rvEntity.posX - 0.5D, rvEntity.posY - 0.0D, rvEntity.posZ - 0.5D, rvEntity.posX + 0.5D, rvEntity.posY + 1.5D, rvEntity.posZ + 0.5D);
		if (mc.world != null) {
			RayTraceResult traceResult = rvEntity.rayTrace(dist, 0.0F);
			final Vec3d pos = rvEntity.getPositionEyes(0.0F).addVector(0.0D, -Helpers.execNullable(rvEntity.getRidingEntity(), Entity::getMountedYOffset, 0.0D), 0.0D);
			final Vec3d lookVec = rvEntity.getLook(0.0F);
			final Vec3d lookTarget = pos.addVector(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
			final float growth = 1.0F;
			final List<Entity> list = mc.world.getEntitiesWithinAABBExcludingEntity(rvEntity, viewBB.expand(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist).grow(growth, growth, growth));
			final double calcdist = traceResult != null ? traceResult.hitVec.distanceTo(pos) : dist;

			double newDist = calcdist;
			Entity pointed = null;

			for (Entity entity : list) {
				if (entity.canBeCollidedWith()) {
					float borderSize = entity.getCollisionBorderSize();
					AxisAlignedBB aabb;

					if (isWiderAttack) {
						float w = (float) INSTANCE.getAttackWidth();
						aabb = new AxisAlignedBB(entity.posX - entity.width * w, entity.posY, entity.posZ - entity.width * w, entity.posX + entity.width * w, entity.posY + entity.height, entity.posZ + entity.width * w);
					} else {
						aabb = new AxisAlignedBB(entity.posX - entity.width / 2.0F, entity.posY, entity.posZ - entity.width / 2.0F, entity.posX + entity.width / 2.0F, entity.posY + entity.height, entity.posZ + entity.width / 2.0F);
					}

					aabb.grow(borderSize, borderSize, borderSize);
					RayTraceResult mop0 = aabb.calculateIntercept(pos, lookTarget);
					if (aabb.contains(pos) && entity != rvEntity.getRidingEntity()) {
						if (newDist >= -0.000001D) {
							pointed = entity;
							newDist = 0.0D;
						}
					} else if (mop0 != null) {
						double hitDist = pos.distanceTo(mop0.hitVec);
						if (hitDist < newDist || (newDist >= -0.000001D && newDist <= 0.000001D)) {
							pointed = entity;
							newDist = hitDist;
						}
					}
				}
			}

			if (pointed != null && (newDist < calcdist || traceResult == null)) {
				return new RayTraceResult(pointed);
			}

			return traceResult;
		}

		return null;
	}
}
