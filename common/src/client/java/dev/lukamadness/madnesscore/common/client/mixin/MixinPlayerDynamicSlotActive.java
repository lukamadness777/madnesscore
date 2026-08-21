package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotUiState;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sobreescribe {@code Slot#isActive()} (client-only) para instancias de {@link PlayerDynamicSlot},
 * equivalente a {@code dev.emi.trinkets.SurvivalTrinketSlot#isEnabled()} en Trinkets.
 * <p>
 * IMPORTANTE: el mixin va sobre {@code Slot} (la clase que DECLARA {@code isActive()}), no sobre
 * {@link PlayerDynamicSlot}. Un intento anterior apuntaba a {@code PlayerDynamicSlot} directamente
 * y falló en el arranque con
 * {@code InvalidInjectionException: could not find any targets matching 'isActive' in ...
 * PlayerDynamicSlot} - {@link PlayerDynamicSlot} nunca sobreescribe {@code isActive()} (lo hereda
 * tal cual de {@code Slot}), así que ese método no existe en el bytecode propio de la clase, que es
 * donde Mixin busca el target por defecto. Confirmado parseando el constant-pool del jar de
 * Minecraft 1.21.1 (net/minecraft/world/inventory/Slot.class) que el nombre real en mappings
 * oficiales es, efectivamente, {@code isActive} - el nombre no era el problema, el target sí.
 * <p>
 * FALTABA en el port: {@link PlayerDynamicSlot} vive en el sourceSet "main" (común a ambos lados,
 * a propósito, ver el comentario en esa clase) y por eso nunca decide por sí mismo si está
 * "enfocado" - pero como consecuencia no había NINGÚN lugar donde vainilla se enterara de que un
 * slot colapsado (no-ancla, grupo no abierto) debía tratarse como inactivo. {@code Slot#isActive()}
 * es el método que {@code AbstractContainerScreen} usa tanto para decidir si un slot entra en el
 * loop de {@code renderSlot} como para decidir si puede llegar a ser {@code hoveredSlot} - sin este
 * mixin, un {@link PlayerDynamicSlot} colapsado se comporta como activo siempre, así que:
 * <p>
 * (a) se puede hoverear/clickear directamente sin pasar primero por el slot ancla (el guard de
 * {@code MixinAbstractContainerScreen#madnesscore$isHovering} sólo actúa una vez que YA hay un
 * grupo activo, no antes), y
 * <p>
 * (b) si un ítem termina metido ahí (ej: quick-move), {@code MixinAbstractContainerScreen} cancela
 * su render por completo (no anchor + grupo cerrado) y el ítem queda invisible aunque siga
 * equipado - el bug reportado como "la armadura no se renderiza".
 * <p>
 * Con este mixin, vainilla directamente saca esos slots del loop de hover/render (igual que hace
 * Trinkets), y {@code MixinAbstractContainerScreen} pasa a ser un guard redundante/defensivo en
 * vez de la única barrera.
 */
@Mixin(Slot.class)
public abstract class MixinPlayerDynamicSlotActive {

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void madnesscore$isActive(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerDynamicSlot self)) {
            return;
        }

        // El slot ancla (offset 0, en la posición vainilla del grupo) siempre está activo -
        // MIENTRAS su tipo tenga al menos un item que pueda llegar a entrar (ver
        // SlotsApi#hasReadyItems). Antes de este fix se mostraba activo siempre, sin importar si
        // el datapack le habia dado algun tag de items (ej: "head/face", "legs/belt" - definidos
        // pero sin ningun item asignado todavia) - quedaba un slot vacio, clickeable, imposible de
        // usar, permanentemente visible en el inventario. Con el reordenamiento agregado en
        // MixinInventoryMenu (los tipos "listos" van primero), el ancla de un grupo es siempre uno
        // de esos si existe alguno - asi que si el ancla NO esta lista, es porque NINGUN tipo del
        // grupo lo esta, y el grupo entero queda oculto (no hay forma de abrirlo).
        if (self.madnesscore$isAnchor()) {
            cir.setReturnValue(SlotsApi.hasReadyItems(self.madnesscore$getType()));
            return;
        }

        SlotGroup group = self.madnesscore$getGroup();
        boolean groupOpen = SlotUiState.activeGroup == group;
        if (groupOpen && SlotUiState.activeType != null) {
            groupOpen = SlotUiState.activeType == self.madnesscore$getType();
        }
        cir.setReturnValue(groupOpen && SlotsApi.hasReadyItems(self.madnesscore$getType()));
    }
}