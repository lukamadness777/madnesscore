package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Servicio de plataforma que resuelve como se adjunta el {@link LivingEntitySlotComponent} a una
 * entidad. El codigo common no sabe (ni le importa) si por debajo hay un campo inyectado por
 * mixin (Fabric) o un Data Attachment (NeoForge); solo pide "dame (o crea) el componente de esta
 * entidad".
 * <ul>
 *     <li>Fabric: implementado con un mixin sobre {@code LivingEntity} que inyecta un campo y
 *     lo expone via una interfaz "duck" ({@code SlotComponentHolder}).</li>
 *     <li>NeoForge: implementado con un {@code AttachmentType<LivingEntitySlotComponent>}
 *     registrado via {@code DeferredRegister}, que ademas persiste el componente automaticamente
 *     al guardar/cargar la entidad.</li>
 * </ul>
 */
public interface ISlotAttachment {

    /**
     * @return el componente de slots de la entidad, creandolo si todavia no existe.
     */
    LivingEntitySlotComponent getOrCreate(LivingEntity entity);

    /**
     * @return el componente de slots de la entidad solo si ya fue creado, sin forzar su creacion.
     */
    Optional<LivingEntitySlotComponent> getIfPresent(LivingEntity entity);
}