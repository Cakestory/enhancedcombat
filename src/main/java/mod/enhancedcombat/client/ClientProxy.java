package mod.enhancedcombat.client;

import org.lwjgl.input.Keyboard;

import mod.enhancedcombat.client.handler.EventHandlersClient;
import mod.enhancedcombat.client.handler.KeyBindingHandler;
import mod.enhancedcombat.client.handler.SynchedSettings;
import mod.enhancedcombat.client.particle.EntitySweepAttack2FX;
import mod.enhancedcombat.network.PacketHandler;
import mod.enhancedcombat.util.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	public static KeyBinding[] keyBindings;

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(EventHandlersClient.INSTANCE);
		PacketHandler.registerClientMessages();
	}

	@Override
	public void init() {
		super.init();
		keyBindings = new KeyBinding[1];

		// Add keybindings here (
		keyBindings[0] = new KeyBinding("key.enhancedcombat.playerFriendly.desc", Keyboard.KEY_P, "key.enhancedcombat.category");

		for (int i = 0; i < keyBindings.length; ++i) {
			ClientRegistry.registerKeyBinding(keyBindings[i]);
		}

		MinecraftForge.EVENT_BUS.register(KeyBindingHandler.INSTANCE);
	}
	
	@Override
	public void postInit() {
		SynchedSettings.init();
	}

	@Override
	public void spawnSweep(EntityPlayer player) {
		double x = -MathHelper.sin(player.rotationYaw * 0.017453292F);
		double z = MathHelper.cos(player.rotationYaw * 0.017453292F);
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySweepAttack2FX(Minecraft.getMinecraft().getTextureManager(), player.world, player.posX + x, player.posY + player.height * 0.5D, player.posZ + z, 0.0D));
	}
}