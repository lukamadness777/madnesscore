package dev.lukamadness.madnesscore.neoforge.slots;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SlotAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MadnessCoreCommon.MOD_ID);

    private static final IAttachmentSerializer<CompoundTag, LivingEntitySlotComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public LivingEntitySlotComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    LivingEntitySlotComponent component = new LivingEntitySlotComponent((LivingEntity) holder);
                    component.readFromNbt(tag, provider);
                    return component;
                }

                @Override
                public CompoundTag write(LivingEntitySlotComponent attachment, HolderLookup.Provider provider) {
                    CompoundTag tag = new CompoundTag();
                    attachment.writeToNbt(tag, provider);
                    return tag;
                }
            };

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LivingEntitySlotComponent>> SLOT_COMPONENT =
            ATTACHMENT_TYPES.register("slot_component", () ->
                    AttachmentType.builder((IAttachmentHolder holder) -> new LivingEntitySlotComponent((LivingEntity) holder))
                            .serialize(SERIALIZER)
                            .build());

    private SlotAttachments() {
    }

    public static void registerToBus(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
