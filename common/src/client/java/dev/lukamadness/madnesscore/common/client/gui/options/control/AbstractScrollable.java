package dev.lukamadness.madnesscore.common.client.gui.options.control;

import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractParentWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.ScrollbarWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;

public abstract class AbstractScrollable extends AbstractParentWidget {
    protected ScrollbarWidget scrollbar;

    protected AbstractScrollable(Dim2i dim) {
        super(dim);
    }

    public int getScrollAmount() {
        return this.scrollbar.getScrollAmount();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollbar.scroll((int) (-verticalAmount * 10));
        return true;
    }
}
