package dev.lukamadness.madnesscore.common.content.tailoring.recipe;

import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeItem;

import java.util.List;

public final class FormalSuitDyeBlend {
    private FormalSuitDyeBlend() {
    }

    public static int blend(int existingRgb, List<DyeItem> dyes) {
        int r = FastColor.ARGB32.red(existingRgb);
        int g = FastColor.ARGB32.green(existingRgb);
        int b = FastColor.ARGB32.blue(existingRgb);
        int maxSum = Math.max(r, Math.max(g, b));
        int count = 1;

        for (DyeItem dye : dyes) {
            int rgb = dye.getDyeColor().getTextureDiffuseColor();
            int dr = FastColor.ARGB32.red(rgb);
            int dg = FastColor.ARGB32.green(rgb);
            int db = FastColor.ARGB32.blue(rgb);
            maxSum += Math.max(dr, Math.max(dg, db));
            r += dr;
            g += dg;
            b += db;
            count++;
        }

        int avgR = r / count;
        int avgG = g / count;
        int avgB = b / count;
        float brightnessFactor = (float) maxSum / count;
        float maxOfAvg = Math.max(avgR, Math.max(avgG, avgB));
        avgR = (int) (avgR * brightnessFactor / maxOfAvg);
        avgG = (int) (avgG * brightnessFactor / maxOfAvg);
        avgB = (int) (avgB * brightnessFactor / maxOfAvg);

        return FastColor.ARGB32.color(0, avgR, avgG, avgB);
    }
}
