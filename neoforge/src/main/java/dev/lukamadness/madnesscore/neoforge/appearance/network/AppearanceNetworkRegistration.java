package dev.lukamadness.madnesscore.neoforge.appearance.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.customization.network.AppearanceClientHandler;
import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.network.AppearanceServerHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID)
public final class AppearanceNetworkRegistration {
    private AppearanceNetworkRegistration() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                AppearanceConfigPayload.TYPE,
                AppearanceConfigPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(

                        (payload, context) -> context.enqueueWork(() ->
                                AppearanceClientHandler.handleReceived(payload)),

                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof ServerPlayer sender) {
                                AppearanceServerHandler.handleReceived(sender, payload);
                            }
                        })
                )
        );
    }
}
