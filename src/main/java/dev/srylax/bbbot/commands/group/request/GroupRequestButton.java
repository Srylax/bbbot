package dev.srylax.bbbot.commands.group.request;

import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GroupRequestButton extends ReactiveEventAdapter {
    @Override
    public Publisher<?> onButtonInteraction(ButtonInteractionEvent event) {
        return Mono.empty();
//        if (!event.getCustomId().equals("approveRequest") || !event.getCustomId().equals("denyRequest")) return Mono.empty();

    }
}
