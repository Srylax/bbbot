package dev.srylax.bbbot.commands.request.group;

import dev.srylax.bbbot.commands.ReactiveEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestGroupButton extends ReactiveEventListener {
    public RequestGroupButton(GatewayDiscordClient client) {
        super(client);
    }

    @Override
    public Publisher<?> onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getCustomId().equals("approveRequest") || !event.getCustomId().equals("denyRequest")) return Mono.empty();
        return Mono.empty();

    }
}
