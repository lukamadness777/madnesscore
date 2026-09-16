package dev.lukamadness.madnesscore.common.mixin.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V1460.class)
public class MixinV1460 {
    @Unique
    private static Schema madnesscore$schema;

    @Inject(method = "registerTypes", at = @At("HEAD"))
    private void madnesscore$captureSchema(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
            Map<String, Supplier<TypeTemplate>> blockEntityTypes, CallbackInfo ci) {
        madnesscore$schema = schema;
    }

    @Redirect(method = "registerTypes", at = @At(value = "INVOKE",
            target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V"))
    private void madnesscore$attachSlotFixer(Schema schema, boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> original) {
        if (type == References.PLAYER) {
            schema.registerType(recursive, type, () -> madnesscore$attachSlotSchema(original.get()));
        } else {
            schema.registerType(recursive, type, original);
        }
    }

    @Unique
    private static TypeTemplate madnesscore$attachSlotSchema(TypeTemplate original) {
        return DSL.allWithRemainder(
                DSL.optional(DSL.field("madnesscore:slots",
                        DSL.optional(DSL.compoundList(
                                DSL.optional(DSL.compoundList(
                                        DSL.optionalFields("Items",
                                                DSL.optionalFields("Items",
                                                        DSL.list(References.ITEM_STACK.in(madnesscore$schema))))
                                ))
                        ))
                )), original);
    }
}
