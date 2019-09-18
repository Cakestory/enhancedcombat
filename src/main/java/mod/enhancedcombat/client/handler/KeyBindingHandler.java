package mod.enhancedcombat.client.handler;

import mod.enhancedcombat.client.ClientProxy;
import mod.enhancedcombat.util.ModConfig;
import mod.enhancedcombat.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindingHandler {

	public static final KeyBindingHandler INSTANCE = new KeyBindingHandler();

	private KeyBindingHandler() {

	}

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void onEvent(KeyInputEvent event) {

		KeyBinding[] keyBindings = ClientProxy.keyBindings;
		Minecraft mc = Minecraft.getMinecraft();

		if (keyBindings[0].isPressed()) {

			if (ModConfig.settings.playerFriendly) {
				ModConfig.settings.playerFriendly = false;

				if (mc.player != null) {
					TextComponentTranslation text = new TextComponentTranslation("text.enhancedcombat.playerfriendly.off");
					text.getStyle().setColor(TextFormatting.DARK_RED);
					mc.player.sendStatusMessage(text, true);
				}
			} else {
				ModConfig.settings.playerFriendly = true;

				if (mc.player != null) {
					TextComponentTranslation text = new TextComponentTranslation("text.enhancedcombat.playerfriendly.on");
					text.getStyle().setColor(TextFormatting.DARK_GREEN);
					mc.player.sendStatusMessage(text, true);
				}
			}

			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		}
	}

}
