package dev.lukamadness.madnesscore.common.client.customization.model;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SkinRegion {
    public enum BodyPart {
        HEAD("madnesscore.bodypart.head"),
        BODY("madnesscore.bodypart.body"),
        RIGHT_ARM("madnesscore.bodypart.right_arm"),
        LEFT_ARM("madnesscore.bodypart.left_arm"),
        RIGHT_LEG("madnesscore.bodypart.right_leg"),
        LEFT_LEG("madnesscore.bodypart.left_leg");

        private final String translationKey;
        BodyPart(String translationKey) { this.translationKey = translationKey; }
        public Component label() { return Component.translatable(translationKey); }
    }

    public enum Layer {
        BASE("madnesscore.layer.base"), OVERLAY("madnesscore.layer.overlay");

        private final String translationKey;
        Layer(String translationKey) { this.translationKey = translationKey; }
        public Component label() { return Component.translatable(translationKey); }
    }

    public enum Face {
        TOP("madnesscore.face.top"), BOTTOM("madnesscore.face.bottom"), RIGHT("madnesscore.face.right"),
        FRONT("madnesscore.face.front"), LEFT("madnesscore.face.left"), BACK("madnesscore.face.back"),
        PAD_LEFT("madnesscore.face.pad_left"), PAD_RIGHT("madnesscore.face.pad_right");

        private final String translationKey;
        Face(String translationKey) { this.translationKey = translationKey; }
        public Component label() { return Component.translatable(translationKey); }
    }

    public final BodyPart bodyPart;
    public final Layer layer;
    public final Face face;
    public final int u, v, width, height;

    private SkinRegion(BodyPart bodyPart, Layer layer, Face face, int u, int v, int width, int height) {
        this.bodyPart = bodyPart;
        this.layer = layer;
        this.face = face;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    public Component label() {
        return Component.translatable("madnesscore.config.region.label", bodyPart.label(), layer.label(), face.label());
    }

    public String key() {
        return bodyPart.name() + "_" + layer.name() + "_" + face.name();
    }

    private record BoxDef(BodyPart part, int depth, int width, int height,
                          int baseU, int baseV, int overlayU, int overlayV) {}

    private static final List<BoxDef> BOXES = List.of(
            new BoxDef(BodyPart.HEAD,      8, 8, 8,  0,  0, 32,  0),
            new BoxDef(BodyPart.BODY,      4, 8, 12, 16, 16, 16, 32),
            new BoxDef(BodyPart.RIGHT_ARM, 4, 4, 12, 40, 16, 40, 32),
            new BoxDef(BodyPart.LEFT_ARM,  4, 4, 12, 32, 48, 48, 48),
            new BoxDef(BodyPart.RIGHT_LEG, 4, 4, 12,  0, 16,  0, 32),
            new BoxDef(BodyPart.LEFT_LEG,  4, 4, 12, 16, 48,  0, 48)
    );

    private static final List<SkinRegion> ALL;

    static {
        List<SkinRegion> all = new ArrayList<>();
        for (BoxDef box : BOXES) {
            addFaces(all, box.part(), Layer.BASE, box.baseU(), box.baseV(), box.depth(), box.width(), box.height());
            addFaces(all, box.part(), Layer.OVERLAY, box.overlayU(), box.overlayV(), box.depth(), box.width(), box.height());
        }
        ALL = Collections.unmodifiableList(all);
    }

    private static void addFaces(List<SkinRegion> out, BodyPart part, Layer layer,
                                 int u, int v, int d, int w, int h) {
        out.add(new SkinRegion(part, layer, Face.PAD_LEFT,  u,             v,     d, d));
        out.add(new SkinRegion(part, layer, Face.TOP,       u + d,         v,     w, d));
        out.add(new SkinRegion(part, layer, Face.BOTTOM,    u + d + w,     v,     w, d));
        out.add(new SkinRegion(part, layer, Face.PAD_RIGHT, u + d + 2 * w, v,     d, d));
        out.add(new SkinRegion(part, layer, Face.RIGHT,     u,             v + d, d, h));
        out.add(new SkinRegion(part, layer, Face.FRONT,     u + d,         v + d, w, h));
        out.add(new SkinRegion(part, layer, Face.LEFT,      u + d + w,     v + d, d, h));
        out.add(new SkinRegion(part, layer, Face.BACK,      u + d + w + d, v + d, w, h));
    }

    public static List<SkinRegion> all() {
        return ALL;
    }

    public static SkinRegion of(BodyPart part, Layer layer, Face face) {
        for (SkinRegion region : ALL) {
            if (region.bodyPart == part && region.layer == layer && region.face == face) return region;
        }
        throw new IllegalArgumentException("No region for " + part + "/" + layer + "/" + face);
    }

    public static SkinRegion byKey(String key) {
        for (SkinRegion region : ALL) {
            if (region.key().equals(key)) return region;
        }
        return null;
    }

    public static SkinRegion defaultRegion() {
        return of(BodyPart.HEAD, Layer.BASE, Face.FRONT);
    }
}
