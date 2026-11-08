package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** A clipped, scrollable container for the 1.21.4 settings form widgets. */
public final class ScrollableForm extends AbstractContainerWidget {
    private final LinearLayout form;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<Integer> baseX = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();

    public ScrollableForm(LinearLayout form) {
        super(0, 0, 1, 1, Component.literal("Settings"));
        this.form = form;
        refreshFormLayout();
    }

    public void setBounds(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setSize(width, height);
        refreshFormLayout();
        refreshScrollAmount();
        positionWidgets();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    protected int contentHeight() {
        return form.getHeight();
    }

    @Override
    protected double scrollRate() {
        return 20.0;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        positionWidgets();
        graphics.enableScissor(getX(), getY(), getRight(), getBottom());
        try {
            for (AbstractWidget widget : widgets) {
                widget.render(graphics, mouseX, mouseY, delta);
            }
        } finally {
            graphics.disableScissor();
        }
        renderScrollbar(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        positionWidgets();
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        positionWidgets();
        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        positionWidgets();
        return handled;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (getFocused() instanceof AbstractWidget focusedWidget) {
            focusedWidget.updateNarration(output);
        } else {
            defaultButtonNarrationText(output);
        }
    }

    private void refreshFormLayout() {
        form.setX(0);
        form.setY(0);
        form.arrangeElements();
        widgets.clear();
        baseX.clear();
        baseY.clear();
        form.visitWidgets(widget -> {
            widgets.add(widget);
            baseX.add(widget.getX());
            baseY.add(widget.getY());
        });
    }

    private void positionWidgets() {
        int formX = getX() + Math.max(0, (getWidth() - form.getWidth()) / 2);
        int formY = getY() - (int) scrollAmount();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget widget = widgets.get(i);
            widget.setX(formX + baseX.get(i));
            widget.setY(formY + baseY.get(i));
        }
    }
}