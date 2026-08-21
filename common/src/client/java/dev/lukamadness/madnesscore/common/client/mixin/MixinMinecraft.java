package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        MadnessCoreCommon.LOG.info("This line is printed by the Madness Core common mixin!");
        MadnessCoreCommon.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
}
