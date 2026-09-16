package dev.lukamadness.madnesscore.common.client.gui.widgets;

import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyBoundButtonWidget extends FlatButtonWidget {
    private final int shortcutKey;
    private final Component underlinedLabel;

    public KeyBoundButtonWidget(Dim2i dim, Component label, Runnable action, boolean drawBackground, boolean leftAlign, int shortcutKey) {
        super(dim, label, action, drawBackground, leftAlign);
        this.shortcutKey = shortcutKey;
        this.underlinedLabel = buildUnderlinedLabel(label, shortcutKey);
    }

    private static Component buildUnderlinedLabel(Component label, int shortcutKey) {
        var text = label.getString();
        var keyChar = (char) shortcutKey;
        var index = indexOfIgnoreCase(text, keyChar);

        if (index >= 0) {
            return Component.empty()
                    .append(Component.literal(text.substring(0, index)))
                    .append(Component.literal(text.substring(index, index + 1)).withStyle(ChatFormatting.UNDERLINE))
                    .append(Component.literal(text.substring(index + 1)));
        }

        return Component.empty()
                .append(label)
                .append(Component.literal(" ["))
                .append(Component.literal(String.valueOf(keyChar)).withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("]"));
    }

    private static int indexOfIgnoreCase(String text, char target) {
        var lowerTarget = Character.toLowerCase(target);
        for (int i = 0; i < text.length(); i++) {
            if (Character.toLowerCase(text.charAt(i)) == lowerTarget) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected Component getRenderedLabel() {
        return this.isEnabled() && Screen.hasAltDown() ? this.underlinedLabel : super.getRenderedLabel();
    }

    public boolean tryActivateShortcut(int keyCode, int modifiers) {
        if (this.isEnabled() && this.isVisible() && (modifiers & GLFW.GLFW_MOD_ALT) != 0 && keyCode == this.shortcutKey) {
            this.doAction();
            return true;
        }
        return false;
    }
}
