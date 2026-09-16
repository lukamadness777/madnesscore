package dev.lukamadness.madnesscore.common.client.gui.options.control;

import dev.lukamadness.madnesscore.common.client.util.Dim2i;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOptionList extends AbstractScrollable {
    protected final List<ControlElement> controls = new ArrayList<>();

    protected AbstractOptionList(Dim2i dim) {
        super(dim);
    }

    public List<ControlElement> getControls() {
        return this.controls;
    }
}
