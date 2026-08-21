package dev.lukamadness.madnesscore.common.mixin.accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expone la lista interna de {@link AbstractContainerMenu} que vainilla usa para detectar
 * cambios de contenido entre ticks (comparada en {@code broadcastChanges()}). Necesario SOLO
 * para el caso de remoción: {@code addSlot(Slot)} ya hace crecer esta lista con solo agregar un
 * slot nuevo normalmente, pero vainilla no expone ningún "removeSlot", así que para poder sacar
 * los slots dinámicos de la cola (antes de re-agregarlos) sin desincronizar los índices hay que
 * recortarla a mano en el mismo punto.
 * <p>
 * Portado de dev.emi.trinkets.mixin.accessor.ScreenHandlerAccessor (Yarn: trackedStacks /
 * trackedSlots).
 * <p>
 * CONFIRMADO contra mappings oficiales 1.21.x (ver javadoc de AbstractContainerMenu): el campo
 * se llama "lastSlots" y su tipo es {@code NonNullList<ItemStack>} (NO {@code List<ItemStack>}
 * genérico) — el tipo de retorno del @Accessor tiene que coincidir exactamente con el del campo,
 * de ahí el error "Could not locate @Accessor target" si se declara como List.
 */
@Mixin(AbstractContainerMenu.class)
public interface ContainerMenuAccessor {

    @Accessor("lastSlots")
    NonNullList<ItemStack> madnesscore$getLastSlots();
}
