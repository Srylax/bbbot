package dev.srylax.bbbot.commands.clearall;

import dev.srylax.bbbot.assets.TEXTS;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Component
public class ClearAllCommand extends ReactiveEventAdapter {

    public ClearAllCommand(GatewayDiscordClient client) {
        client.on(this).subscribe();
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        Flux<Void> deleteMessages = event
                .getInteraction()
                .getChannel()
                .flatMapMany(e -> e.getMessagesBefore(Snowflake.of(Instant.now())))
                .flatMap(Message::delete);
        return event.deferReply().withEphemeral(true)
                .thenMany(deleteMessages)
                .then(event.createFollowup(TEXTS.get("MessagesDeleted")));
    }
}
