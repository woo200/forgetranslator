package com.tekcno.translate;

import com.mojang.blaze3d.platform.InputConstants;
import com.tekcno.translate.config.TranslatorConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.*;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("translator")
public class ModTranslator
{
    public static DeepLTranslator deepLTranslator;
    public static boolean translationsEnabled = false;
    public static String selectedLang = "EN-US";
    public static TranslatorConfig translatorConfig;

    public static final KeyMapping TOGGLE_TRANSLATIONS = new KeyMapping("key.translator.ttoggle", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "categories.mod.translator");

    Logger logger = LogManager.getLogger("translator");

    public ModTranslator() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        String configPath = Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/config/translator.config";
        try {
            translatorConfig = new TranslatorConfig(configPath);
            translatorConfig.setDefaults("api_key", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:fx");
            translatorConfig.saveConfig();
        } catch (IOException e) {
            logger.fatal("Error loading translator config. Cannot load mod.");
            return;
        }
        MinecraftForge.EVENT_BUS.register(this);
        ModTranslator.deepLTranslator = new DeepLTranslator(translatorConfig.getConfigString("api_key"));
        ClientRegistry.registerKeyBinding(TOGGLE_TRANSLATIONS);
    }

    public static BaseComponent prefixed(BaseComponent message)
    {
        return (BaseComponent) (new TextComponent("[Translator] ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(message);
    }

    public record DoTranslation(String message) implements Runnable {
        @Override
        public void run() {
            DeepLTranslator.Translation translation = ModTranslator.deepLTranslator.translate(message);

            BaseComponent tooltip;
            if (translation != null) {
                tooltip = new TextComponent(translation.getText());
                tooltip.setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponent(message + " [" + translation.getDetected_source_language() + "]")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setlang " + translation.getDetected_source_language())));
            } else { // Translation error
                tooltip = new TextComponent(message);
                tooltip.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Error Translating Message"))));
            }
            Minecraft.getInstance().player.sendMessage(tooltip, UUID.randomUUID());
        }
    }

    public record SendTranslation(String message) implements Runnable {
        @Override
        public void run() {
            DeepLTranslator.Translation translation = ModTranslator.deepLTranslator.translate(message, selectedLang);
            if (translation != null)
                Minecraft.getInstance().player.chat(translation.getText());
            else
                Minecraft.getInstance().player.sendMessage(new TextComponent("ERROR TRANSLATING"), UUID.randomUUID());
        }
    }

    /**
     * Yes, I know this mod is complete spaghetti code. I will clean up the code in the next release.
     * Pardon me, but I made this mod in math class at school. I rushed the whole thing and made it in about an hour.
     */
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onKeyPressed(final InputEvent.KeyInputEvent event) {
            if (!TOGGLE_TRANSLATIONS.isDown())
                return;

            if (translatorConfig.getConfigString("api_key").equals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:fx")) {
                Minecraft.getInstance().player.sendMessage(prefixed(
                        (BaseComponent) new TextComponent("Click this message to set your API key")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setkey API_KEY_HERE")))), UUID.randomUUID());
                return;
            }

            translationsEnabled = !translationsEnabled;
            Minecraft.getInstance().player.sendMessage(prefixed(
                    (BaseComponent) new TextComponent(translationsEnabled ? "Enabled" : "Disabled")
                            .setStyle(Style.EMPTY.withColor(translationsEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))), UUID.randomUUID());

        }
        @SubscribeEvent
        public static void onChatIncoming(final ClientChatReceivedEvent event) {
            if (!translationsEnabled)
                return;

            new Thread(new DoTranslation(event.getMessage().getString())).start(); // Translate incoming messages
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onChatFromPlayer(final ClientChatEvent event) {
            String[] args = event.getMessage().split(" ");
            if (event.getMessage().split(" ")[0].equals("/setlang")) { // Set language to translate to (DEFAULT EN-US)
                if (args.length > 1) {
                    selectedLang = args[1];
                    Minecraft.getInstance().player.sendMessage(prefixed(new TextComponent("Set language to: " + selectedLang)), UUID.randomUUID());
                }
                event.setCanceled(true);
                return;
            }
            if (event.getMessage().split(" ")[0].equals("/setkey")) { // Set API key
                if (args.length > 1) {
                    String apiKey = args[1];
                    translatorConfig.setConfigString("api_key", apiKey);
                    deepLTranslator.setApi_key(apiKey);
                    try {
                        translatorConfig.saveConfig();
                    } catch (IOException e) {
                        Minecraft.getInstance().player.sendMessage(prefixed((BaseComponent) new TextComponent("Unable to save API key to config. The API key has been TEMPORARILY updated.").withStyle(Style.EMPTY.withColor(ChatFormatting.RED))), UUID.randomUUID());
                        return;
                    }
                    Minecraft.getInstance().player.sendMessage(prefixed(new TextComponent("API key has been updated.")), UUID.randomUUID());
                }
                event.setCanceled(true);
                return;
            }
            if (translationsEnabled) { // Translate outgoing messages if translations are enabled
                if (event.getMessage().startsWith("/"))
                    return;
                new Thread(new SendTranslation(event.getMessage())).start();
                event.setCanceled(true);
            }
        }
    }
}
