package dev.lukamadness.madnesscore.common.client.slots.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public final class SlotHoverManager {
    private static final ResourceLocation MORE_SLOTS =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/slots/more_slots.png");

    private static WeakReference<SlotHoverScreen> currentScreen;
    public static Rect2i currentBounds = new Rect2i(0, 0, 0, 0);
    public static Rect2i typeBounds = new Rect2i(0, 0, 0, 0);
    public static SlotGroup group = null;

    private SlotHoverManager() {
    }

    public static void init(SlotHoverScreen screen) {
        currentScreen = screen == null ? null : new WeakReference<>(screen);
        group = null;
        currentBounds = new Rect2i(0, 0, 0, 0);
    }

    public static void close() {
        init(null);
    }

    public static void removeSelections() {
        SlotUiState.clear();
    }

    public static void update(float mouseX, float mouseY) {
        SlotHoverScreen screen = getCurrentScreen();
        if (screen == null) {
            return;
        }

        PlayerSlotMenu menu = screen.madnesscore$getMenu();
        Slot hoveredSlot = screen.madnesscore$getHoveredSlot();
        int x = screen.madnesscore$getX();
        int y = screen.madnesscore$getY();

        if (group != null) {
            if (SlotUiState.activeType != null) {
                if (!typeBounds.contains(Math.round(mouseX) - x, Math.round(mouseY) - y)) {
                    SlotUiState.activeType = null;
                } else if (hoveredSlot != null) {
                    if (!(hoveredSlot instanceof dev.lukamadness.madnesscore.common.slots.gui.DynamicSlot ds
                            && ds.madnesscore$getType() == SlotUiState.activeType)) {
                        SlotUiState.activeType = null;
                    }
                }
            }
            if (SlotUiState.activeType == null) {
                if (!currentBounds.contains(Math.round(mouseX) - x, Math.round(mouseY) - y)) {
                    SlotUiState.activeGroup = null;
                    group = null;
                } else {
                    if (hoveredSlot instanceof dev.lukamadness.madnesscore.common.slots.gui.DynamicSlot ds) {
                        int i = menu.madnesscore$getSlotTypes(group).indexOf(ds.madnesscore$getType());
                        if (i >= 0) {
                            Point slotHeight = menu.madnesscore$getSlotHeight(group, i);
                            if (slotHeight != null) {
                                Rect2i r = screen.madnesscore$getGroupRect(group);
                                int height = slotHeight.y();
                                if (height > 1) {
                                    SlotUiState.activeType = ds.madnesscore$getType();
                                    typeBounds = new Rect2i(r.getX() + slotHeight.x() - 2,
                                            r.getY() - (height - 1) / 2 * 18 - 3, 23, height * 18 + 5);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (group == null) {
            boolean recipeBookOpen = screen.madnesscore$isRecipeBookOpen();
            for (SlotGroup g : SlotsApi
                    .getEntitySlots(Minecraft.getInstance().player).values()) {
                Rect2i r = screen.madnesscore$getGroupRect(g);

                if (r.getX() < 0 && recipeBookOpen) {
                    continue;
                }
                if (r.contains(Math.round(mouseX) - x, Math.round(mouseY) - y)) {
                    SlotUiState.activeGroup = g;
                    break;
                }
            }
        }

        if (group != SlotUiState.activeGroup) {
            group = SlotUiState.activeGroup;

            if (group != null) {
                int slotsWidth = menu.madnesscore$getSlotWidth(group) + 1;
                if (group.getSlotId() == -1) {
                    slotsWidth -= 1;
                }
                Rect2i r = screen.madnesscore$getGroupRect(group);
                currentBounds = new Rect2i(0, 0, 0, 0);

                if (r != null) {
                    int l = (slotsWidth - 1) / 2 * 18;

                    if (slotsWidth > 1) {
                        currentBounds = new Rect2i(r.getX() - l - 3, r.getY() - 3, slotsWidth * 18 + 5, 23);
                    } else {
                        currentBounds = r;
                    }

                    if (hoveredSlot instanceof dev.lukamadness.madnesscore.common.slots.gui.DynamicSlot ds) {
                        int i = menu.madnesscore$getSlotTypes(group).indexOf(ds.madnesscore$getType());
                        if (i >= 0) {
                            Point slotHeight = menu.madnesscore$getSlotHeight(group, i);
                            if (slotHeight != null) {
                                int height = slotHeight.y();
                                if (height > 1) {
                                    SlotUiState.activeType = ds.madnesscore$getType();
                                    typeBounds = new Rect2i(r.getX() + slotHeight.x() - 2,
                                            r.getY() - (height - 1) / 2 * 18 - 3, 23, height * 18 + 5);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void drawGroup(GuiGraphics graphics, SlotGroup group, SlotType type) {
        SlotHoverScreen screen = getCurrentScreen();
        if (screen == null) {
            return;
        }

        PlayerSlotMenu menu = screen.madnesscore$getMenu();
        GlStateManager._enableDepthTest();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);

        Rect2i r = screen.madnesscore$getGroupRect(group);
        int slotsWidth = menu.madnesscore$getSlotWidth(group) + 1;
        List<Point> slotHeights = menu.madnesscore$getSlotHeights(group);
        List<SlotType> slotTypes = menu.madnesscore$getSlotTypes(group);
        if (group.getSlotId() == -1) {
            slotsWidth -= 1;
        }
        int x = r.getX() - 4 - (slotsWidth - 1) / 2 * 18;
        int y = r.getY() - 4;
        if (slotsWidth > 1 || type != null) {
            blit(graphics, x, y, 0, 0, 4, 26);

            for (int i = 0; i < slotsWidth; i++) {
                blit(graphics, x + i * 18 + 4, y, 4, 0, 18, 26);
            }

            blit(graphics, x + slotsWidth * 18 + 4, y, 22, 0, 4, 26);
            for (int s = 0; s < slotHeights.size() && s < slotTypes.size(); s++) {
                if (slotTypes.get(s) != type) {
                    continue;
                }
                Point slotHeight = slotHeights.get(s);
                int height = slotHeight.y();
                if (height > 1) {
                    int top = (height - 1) / 2;
                    int bottom = height / 2;
                    int slotX = slotHeight.x() - 4 + r.getX();
                    if (height > 2) {
                        blit(graphics, slotX, y - top * 18, 0, 0, 26, 4);
                    }
                    for (int i = 1; i < top + 1; i++) {
                        blit(graphics, slotX, y - i * 18 + 4, 0, 4, 26, 18);
                    }
                    for (int i = 1; i < bottom + 1; i++) {
                        blit(graphics, slotX, y + i * 18 + 4, 0, 4, 26, 18);
                    }
                    blit(graphics, slotX, y + 18 + bottom * 18 + 4, 0, 22, 26, 4);
                }
            }

            for (int s = 0; s < slotHeights.size(); s++) {
                Point slotHeight = slotHeights.get(s);
                int height = slotHeight.y();
                if (slotTypes.get(s) != type) {
                    height = 1;
                }
                int slotX = slotHeight.x() + r.getX() + 1;
                int top = (height - 1) / 2;
                int bottom = height / 2;
                blit(graphics, slotX, y - top * 18 + 1, 4, 1, 16, 3);
                blit(graphics, slotX, y + (bottom + 1) * 18 + 4, 4, 22, 16, 3);
            }

            if (group.getSlotId() != -1) {
                blit(graphics, r.getX() + 1, y + 1, 4, 1, 16, 3);
                blit(graphics, r.getX() + 1, y + 22, 4, 22, 16, 3);
            }
        } else {
            blit(graphics, x + 4, y + 4, 4, 4, 18, 18);
        }

        graphics.pose().popPose();
        GlStateManager._disableDepthTest();
    }

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(MORE_SLOTS, x, y, u, v, width, height, 256, 256);
    }

    public static void drawActiveGroup(GuiGraphics graphics) {
        if (SlotUiState.activeGroup != null) {
            drawGroup(graphics, SlotUiState.activeGroup, SlotUiState.activeType);
        }
    }

    public static void drawExtraGroups(GuiGraphics graphics) {
        SlotHoverScreen screen = getCurrentScreen();
        if (screen == null) {
            return;
        }

        PlayerSlotMenu menu = screen.madnesscore$getMenu();
        int x = screen.madnesscore$getX();
        int y = screen.madnesscore$getY();
        int groupCount = menu.madnesscore$getGroupCount();
        if (groupCount <= 0 || screen.madnesscore$isRecipeBookOpen()) {
            return;
        }

        int width = groupCount / 4;
        int height = groupCount % 4;
        if (height == 0) {
            height = 4;
            width--;
        }

        blit(graphics, x + 3, y, 7, 26, 1, 7);
        for (int i = 0; i < width; i++) {
            blit(graphics, x - 15 - 18 * i, y, 7, 26, 18, 7);
            blit(graphics, x - 15 - 18 * i, y + 79, 7, 51, 18, 7);
        }
        blit(graphics, x - 15 - 18 * width, y, 7, 26, 18, 7);
        blit(graphics, x - 15 - 18 * width, y + 7 + 18 * height, 7, 51, 18, 7);
        blit(graphics, x - 22 - 18 * width, y, 0, 26, 7, 7);
        blit(graphics, x - 22 - 18 * width, y + 7 + 18 * height, 0, 51, 7, 7);
        for (int i = 0; i < height; i++) {
            blit(graphics, x - 22 - 18 * width, y + 7 + 18 * i, 0, 34, 7, 18);
        }
        if (width > 0) {
            for (int i = height; i < 4; i++) {
                blit(graphics, x - 4 - 18 * width, y + 7 + 18 * i, 0, 34, 7, 18);
            }
        }
        if (width > 0 && height < 4) {
            blit(graphics, x - 4 - 18 * width, y + 79, 0, 51, 7, 7);
            blit(graphics, x - 4 - 18 * width, y + 7 + 18 * height, 0, 58, 7, 7);
        }
        if (width > 0 || height == 4) {
            blit(graphics, x, y + 79, 0, 58, 3, 7);
        }
    }

    public static boolean isClickInsideBounds(double mouseX, double mouseY) {
        SlotHoverScreen screen = getCurrentScreen();
        if (screen == null || Minecraft.getInstance().screen != screen) {
            return false;
        }
        int x = screen.madnesscore$getX();
        int y = screen.madnesscore$getY();
        int mx = (int) (Math.round(mouseX) - x);
        int my = (int) (Math.round(mouseY) - y);
        if (currentBounds.contains(mx, my)) {
            return true;
        }

        int groupCount = screen.madnesscore$getMenu().madnesscore$getGroupCount();
        if (groupCount <= 0 || screen.madnesscore$isRecipeBookOpen()) {
            return false;
        }
        int width = groupCount / 4;
        int height = groupCount % 4;
        if (width > 0) {
            if (new Rect2i(-4 - 18 * width, 0, 7 + 18 * width, 86).contains(mx, my)) {
                return true;
            }
        }
        if (height > 0) {
            if (new Rect2i(-22 - 18 * width, 0, 25, 14 + 18 * height).contains(mx, my)) {
                return true;
            }
        }
        return false;
    }

    private static SlotHoverScreen getCurrentScreen() {
        if (currentScreen == null) {
            return null;
        }
        return currentScreen.get();
    }
}
