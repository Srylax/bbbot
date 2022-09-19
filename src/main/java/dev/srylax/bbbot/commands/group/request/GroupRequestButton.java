package dev.srylax.bbbot.commands.group.request;

import dev.srylax.bbbot.commands.ReactiveEventListener;
Adimport dev.srylax.bbbot.db.request.group.GroupRequestRepository;
import dev.srylax.bbbot.db.request.group.GroupRequestRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GroupRequestButton extends ReactiveEventListener {

    private final GroupRequestRepository groupRequestRepository;
    public GroupRequestButton(GatewayDiscordClient client, GroupRequestRepository groupRequestRepository) {
        super(client);
        this.groupRequestRepository = groupRequestRepository;
    }

    @Override
    public Publisher<?> onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getCustomId().startsWith("request-group")) return Mono.empty();
        String id = event.getMessage().stream()
                .map(Message::getEmbeds)
                .flatMap(List::stream)
                .map(Embed::getFields)
                .flatMap(List::stream)
                .filter(f->f.getName().equals("ID"))
                .map(Embed.Field::getValue)
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        return event.deferReply().withEphemeral(true)
                .then(groupRequestRepository.findById(id))
                .cache()
                .doOnNext()
                ;
    }
}
