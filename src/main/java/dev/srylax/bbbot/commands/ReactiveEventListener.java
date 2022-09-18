package dev.srylax.bbbot.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NoArgsConstructor
public abstract class ReactiveEventListener extends ReactiveEventAdapter {
    public ReactiveEventListener(GatewayDiscordClient client) {
        client.on(this).subscribe();
    }

    protected ApplicationCommandInteractionOptionValue getRequiredValue(ApplicationCommandInteractionOption interaction, String name) {
        return interaction
                .getOption(name)
                .orElseThrow(IllegalStateException::new)
                .getValue()
                .orElseThrow(IllegalStateException::new);
    }
}
