package dev.lukamadness.madnesscore.common.slots;

import dev.lukamadness.madnesscore.common.api.slots.SlotComponent;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * Espejo de SOLO LECTURA entre un slot de trinkets anclado a un grupo de armadura (ver
 * {@link #VANILLA_ARMOR_SLOT_IDS}) y el {@link EquipmentSlot} vainilla real de la entidad.
 * <p>
 * Reemplaza al viejo {@code SlotTicker#mirrorToVanillaEquipment}, que copiaba el stack hacia
 * {@code entity.setItemSlot(...)} y causaba un bug de duplicacion (el item quedaba existiendo a
 * la vez como dos referencias independientes: una en el inventario de trinkets y otra en el
 * array de equipo real). Esta clase NUNCA escribe nada en la entidad ni en ningun inventario -
 * solo expone, para quien la consulte, CUAL es la {@link ItemStack} (la misma instancia que ya
 * vive en el {@link SlotInventory} de trinkets, no una copia) que deberia "verse" como puesta en
 * un {@link EquipmentSlot} vainilla cuando ese slot esta vacio de verdad.
 * <p>
 * El unico punto que consulta esto es {@code MixinLivingEntityEquipmentMirror#getItemBySlot}
 * (common/mixin), inyectado en {@code LivingEntity#getItemBySlot(EquipmentSlot)}. Como
 * practicamente todo el codigo vainilla relevante para "efectos de equipo" (respiracion de agua
 * del casco de tortuga, reduccion de dano por Proteccion/Proteccion contra fuego/explosiones/
 * proyectiles, Espinas, Paso Helado, Pies Ligeros, Afinidad Acuatica, Respiracion, Paso de las
 * Almas, etc.) termina leyendo el item equipado a traves de {@code getItemBySlot}/
 * {@code getArmorSlots()}/{@code getAllSlots()} en vez de acceder al array interno directamente,
 * espejar SOLO ese metodo alcanza para que vainilla trate el item de trinkets exactamente igual
 * que si estuviera puesto de verdad - sin necesidad de "recrear" cada efecto ni cada encantamiento
 * a mano.
 * <p>
 * NOTA (alcance deliberado - ver mensaje al usuario): el mixin que consulta esta clase solo actua
 * sobre {@link Player} (chequeo por {@code instanceof} en el propio mixin), no sobre
 * {@link LivingEntity} en general. Para mobs, varios caminos vainilla de "soltar equipo al morir"
 * (ej. {@code Mob#dropCustomDeathLoot}/{@code LivingEntity#dropEquipment}) tambien leen a traves
 * de {@code getItemBySlot}/{@code getAllSlots()} y, a diferencia de los usos de solo lectura de
 * arriba, terminan SPAWNEANDO en el mundo la MISMA referencia que devuelvan - lo que reintroduciria
 * un bug de duplicacion (ahora via loot de muerte) si un mob con un item de trinkets "espejable"
 * muere y el mod, por separado, tambien lo dropea via su propio {@code SlotDeathHandler}. No se
 * pudo confirmar en este entorno (sin compilador ni jar fuente) si el drop-on-death de Player
 * pasa por el mismo camino, asi que probar especificamente: morir con un casco real puesto en
 * "hat" y confirmar que no aparecen dos copias en el piso, e igual con Mending (ver si repara el
 * item de trinkets sin duplicarlo). Si en el futuro se quiere extender a mobs, hay que auditar
 * antes esos caminos de death-drop.
 */
public final class VanillaEquipmentMirror {

    /**
     * Mismo mapeo slot_id -> EquipmentSlot que usaba el viejo mecanismo (ver
     * {@code MixinInventoryMenu}: 5=cabeza, 6=pecho, 7=piernas, 8=pies).
     */
    private static final Map<Integer, EquipmentSlot> VANILLA_ARMOR_SLOT_IDS = Map.of(
            5, EquipmentSlot.HEAD,
            6, EquipmentSlot.CHEST,
            7, EquipmentSlot.LEGS,
            8, EquipmentSlot.FEET
    );

    private VanillaEquipmentMirror() {
    }

    /**
     * @return el stack de trinkets (referencia real, no copia) que deberia verse como equipado en
     * {@code equipmentSlot}, o {@link ItemStack#EMPTY} si no corresponde espejar nada. Solo
     * considera {@link SlotType} con {@code mirror_vanilla_equipment: true} en su JSON (ver
     * {@link SlotType#mirrorsVanillaEquipment()}) cuyo grupo este anclado (via {@code slot_id}) a
     * {@code equipmentSlot}, y solo si el item puesto ahi es realmente una {@link ArmorItem} para
     * ESE EquipmentSlot puntual - un accesorio no-armadura en el mismo grupo (ej. "head/face") no
     * se espeja jamas.
     */
    public static ItemStack getMirroredStack(LivingEntity entity, EquipmentSlot equipmentSlot) {
        // Solo las 4 armor slots tienen sentido; salida rapida para mano/mano secundaria, que son
        //, por lejos, las mas consultadas por tick.
        if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return ItemStack.EMPTY;
        }

        Optional<SlotComponent> componentOpt = SlotsApi.getSlotComponent(entity);
        if (componentOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        SlotComponent component = componentOpt.get();

        for (Map.Entry<String, SlotGroup> groupEntry : component.getGroups().entrySet()) {
            SlotGroup group = groupEntry.getValue();
            if (VANILLA_ARMOR_SLOT_IDS.get(group.getSlotId()) != equipmentSlot) {
                continue;
            }

            Map<String, SlotInventory> groupInventory = component.getInventory().get(groupEntry.getKey());
            if (groupInventory == null) {
                continue;
            }

            for (SlotInventory inv : groupInventory.values()) {
                if (!inv.getSlotType().mirrorsVanillaEquipment()) {
                    continue;
                }
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem
                            && armorItem.getEquipmentSlot() == equipmentSlot) {
                        return stack;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
