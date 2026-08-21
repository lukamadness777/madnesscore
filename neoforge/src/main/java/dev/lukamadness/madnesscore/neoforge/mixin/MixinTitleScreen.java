package dev.lukamadness.madnesscore.neoforge.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        MadnessCoreCommon.LOG.info("This line is printed by the Madness Core mixin from NeoForge!");
        MadnessCoreCommon.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
}
