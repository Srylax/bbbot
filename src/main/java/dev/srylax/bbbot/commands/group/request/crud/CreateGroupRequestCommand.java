package dev.srylax.bbbot.commands.group.request.crud;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.commands.ReactiveEventListener;
import dev.srylax.bbbot.commands.group.request.GroupRequestCommand;
import dev.srylax.bbbot.db.request.group.GroupRequest;
import dev.srylax.bbbot.db.request.group.GroupRequestRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.rest.util.Color;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class CreateGroupRequestCommand extends GroupRequestCommand {

    public CreateGroupRequestCommand(GatewayDiscordClient client, GroupRequestRepository groupRequestRepository) {
        super(client,groupRequestRepository,"create");
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals(commandGroup) || event.getOption(COMMAND_SUP_GROUP).isEmpty())
            return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption(COMMAND_SUP_GROUP).get();
        String name = getRequiredValue(commandOption, "name").asString();
        String type = getRequiredValue(commandOption, "type").asString();
        String description = getRequiredValue(commandOption, "description").asString();
        Long userId = event.getInteraction().getUser().getId().asLong();

        GroupRequest request = new GroupRequest(name, type, description, userId);

        return event.deferReply().withEphemeral(true)
                .then(groupRequestRepository.save(request))
                .map(e -> e.toEmbed()
                        .withTitle(TEXTS.get("GroupRequestCreated"))
                        .withColor(Color.GREEN))
                .flatMap(e ->
                        event.createFollowup(InteractionFollowupCreateSpec.create()
                                .withEmbeds(e)));
    }
}
