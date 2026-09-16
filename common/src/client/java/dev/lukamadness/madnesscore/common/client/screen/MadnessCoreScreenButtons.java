package dev.lukamadness.madnesscore.common.client.screen;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.config.MadnessConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public final class MadnessCoreScreenButtons {
    public static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "icon.png");

    private static final int GAP = 4;
    private static final int SIZE = 20;

    private MadnessCoreScreenButtons() {}

    public static void onScreenInit(Screen screen, Consumer<AbstractWidget> widgetAdder) {
        if (!(screen instanceof TitleScreen) && !(screen instanceof PauseScreen)) {
            return;
        }

        if (hasButton(screen)) {
            return;
        }

        AbstractWidget anchor = findAnchor(screen);

        int y = anchor != null ? anchor.getY() : fallbackY(screen);
        int startX = anchor != null
                ? anchor.getX() - GAP - SIZE
                : screen.width / 2 - 100 - GAP - SIZE;

        int x = findFreeSpaceToTheLeft(screen, startX, y, SIZE, SIZE);

        widgetAdder.accept(new MadnessConfigButton(x, y));
    }

    private static AbstractWidget findAnchor(Screen screen) {
        return screen instanceof TitleScreen
                ? findWidgetByText(screen, "fml.menu.mods")
                : findWidgetByText(screen, "menu.options");
    }

    private static int fallbackY(Screen screen) {
        return (screen instanceof TitleScreen)
                ? screen.height / 4 + 48 + 48
                : screen.height / 4 + 72 + 12;
    }

    private static AbstractWidget findWidgetByText(Screen screen, String translationKey) {
        String target = I18n.get(translationKey);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget w && w.getMessage().getString().equals(target)) {
                return w;
            }
        }
        return null;
    }

    private static boolean hasButton(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof MadnessConfigButton) {
                return true;
            }
        }
        return false;
    }

    private static int findFreeSpaceToTheLeft(Screen screen, int x, int y, int width, int height) {
        boolean overlapping = true;
        while (overlapping) {
            overlapping = false;
            for (GuiEventListener child : screen.children()) {
                if (child instanceof AbstractWidget w && w.visible
                        && x < w.getX() + w.getWidth() && x + width > w.getX()
                        && y < w.getY() + w.getHeight() && y + height > w.getY()) {
                    x = w.getX() - GAP - width;
                    overlapping = true;
                }
            }
        }
        return x;
    }

    private static class MadnessConfigButton extends net.minecraft.client.gui.components.Button {
        MadnessConfigButton(int x, int y) {
            super(x, y, SIZE, SIZE, Component.translatable("gui.madnesscore.config"),
                    b -> Minecraft.getInstance().setScreen(new MadnessConfigScreen(Minecraft.getInstance().screen)),
                    DEFAULT_NARRATION);
            setTooltip(Tooltip.create(Component.translatable("gui.madnesscore.config")));
        }

        @Override
        public void renderString(net.minecraft.client.gui.GuiGraphics graphics, net.minecraft.client.gui.Font font, int color) {
            graphics.blit(ICON, getX() + 2, getY() + 2, 16, 16, 0.0f, 0.0f, 128, 128, 128, 128);
        }
    }
}
