package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.resources.ResourceLocation;

public final class ModFluidProperties {
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private ResourceLocation overlayTexture;
    private int tintColor = 0xFFFFFFFF;
    private int luminosity = 0;
    private int density = 1000;
    private int viscosity = 1000;
    private boolean lighterThanAir = false;

    private ModFluidProperties(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    public static ModFluidProperties of(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new ModFluidProperties(stillTexture, flowingTexture);
    }

    public ModFluidProperties overlay(ResourceLocation overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public ModFluidProperties tintColor(int argb) {
        this.tintColor = argb;
        return this;
    }

    public ModFluidProperties luminosity(int luminosity) {
        this.luminosity = luminosity;
        return this;
    }

    public ModFluidProperties density(int density) {
        this.density = density;
        return this;
    }

    public ModFluidProperties viscosity(int viscosity) {
        this.viscosity = viscosity;
        return this;
    }

    public ModFluidProperties lighterThanAir(boolean lighterThanAir) {
        this.lighterThanAir = lighterThanAir;
        return this;
    }

    public ResourceLocation stillTexture() {
        return stillTexture;
    }

    public ResourceLocation flowingTexture() {
        return flowingTexture;
    }

    public ResourceLocation overlayTexture() {
        return overlayTexture;
    }

    public int tintColor() {
        return tintColor;
    }

    public int luminosity() {
        return luminosity;
    }

    public int density() {
        return density;
    }

    public int viscosity() {
        return viscosity;
    }

    public boolean lighterThanAir() {
        return lighterThanAir;
    }
}
