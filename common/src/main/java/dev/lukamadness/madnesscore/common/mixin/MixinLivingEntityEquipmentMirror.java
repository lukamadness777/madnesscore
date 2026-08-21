package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.slots.VanillaEquipmentMirror;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hace que vainilla "vea" en un {@link EquipmentSlot} de armadura real (cabeza/pecho/piernas/pies)
 * el item que en verdad esta puesto en un slot de trinkets anclado a ese grupo (ver
 * {@link VanillaEquipmentMirror}) - pero SOLO cuando el slot vainilla real esta vacio, y sin
 * escribir nunca nada en la entidad. Reemplaza al viejo mecanismo de
 * {@code SlotTicker#mirrorToVanillaEquipment}, que copiaba el stack via
 * {@code entity.setItemSlot(...)} y por eso duplicaba el item (quedaba existiendo a la vez en el
 * inventario de trinkets y en el array de equipo real).
 * <p>
 * Con este mixin no hay copia: {@code getItemBySlot} simplemente devuelve, de prestado, la MISMA
 * referencia de {@link ItemStack} que ya vive en el inventario de trinkets. Como practicamente
 * todo el codigo vainilla que calcula "efectos de equipo" (respiracion de agua del casco de
 * tortuga, reduccion de dano de los encantamientos de Proteccion, Espinas, Paso Helado, Pies
 * Ligeros, Afinidad Acuatica, Respiracion, Paso de las Almas, renderizado de armadura, etc.) lee
 * el equipo a traves de este metodo (directamente o via {@code getArmorSlots()}/{@code getAllSlots()},
 * que lo llaman internamente por cada slot), alcanza con espejar este unico punto para que MC
 * trate el item de trinkets exactamente igual que si estuviera puesto de verdad - sin recrear cada
 * efecto ni cada encantamiento a mano.
 * <p>
 * Alcance deliberado: el espejo solo actua si la entidad es un {@link Player} (ver el
 * {@code instanceof} abajo). Ver el javadoc de {@link VanillaEquipmentMirror} para el motivo (
 * riesgo de duplicacion via death-drop de mobs) y que falta verificar antes de extenderlo.
 * <p>
 * NOTA (no se puede compilar en este entorno): confirmar en el IDE que la firma exacta en
 * mappings oficiales 1.21.1 es {@code ItemStack getItemBySlot(EquipmentSlot)} (deberia serlo, es
 * un metodo muy estable de {@code LivingEntity} usado por practicamente todo el ecosistema de
 * mods). {@code cancellable = true} + {@code cir.setReturnValue(...)} es la forma correcta de
 * reemplazar el valor de retorno de un {@code @Inject(at = "RETURN")} en Mixin 0.8.x.
 */
@Mixin(Player.class)
public abstract class MixinLivingEntityEquipmentMirror {

    @Inject(method = "getItemBySlot", at = @At("RETURN"), cancellable = true)
    private void madnesscore$mirrorTrinketArmor(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            // Ya hay algo puesto de verdad en el slot vainilla real - eso siempre gana, nunca se
            // pisa ni se compara con lo que haya en trinkets.
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;

        ItemStack mirrored = VanillaEquipmentMirror.getMirroredStack(self, slot);
        if (!mirrored.isEmpty()) {
            cir.setReturnValue(mirrored);
        }
    }
}