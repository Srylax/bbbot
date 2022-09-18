package dev.srylax.bbbot.commands.request.group;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.commands.ReactiveEventListener;
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
public class CreateGroupRequestCommand extends ReactiveEventListener {
    private static final String COMMAND_SUP_GROUP = "group-request";
    private static final String COMMAND_GROUP = "create";

    private final GroupRequestRepository groupRequestRepository;

    public CreateGroupRequestCommand(GatewayDiscordClient client, GroupRequestRepository groupRequestRepository) {
        super(client);
        this.groupRequestRepository = groupRequestRepository;
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals(COMMAND_GROUP) || event.getOption(COMMAND_SUP_GROUP).isEmpty())
            return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption(COMMAND_SUP_GROUP).get();
        String name = getRequiredValue(commandOption, "name").asString();
        String type = getRequiredValue(commandOption, "type").asString();
        String description = getRequiredValue(commandOption, "description").asString();
        Long userId = event.getInteraction().getUser().getId().asLong();

        GroupRequest request = new GroupRequest(name, type, description, userId);

        return groupRequestRepository.save(request)
                .map(e -> e.toEmbed()
                        .withTitle(TEXTS.get("GroupRequestCreated"))
                        .withColor(Color.GREEN))
                .map(e ->
                        event.createFollowup(InteractionFollowupCreateSpec.create()
                                .withEmbeds(e)));
    }
}
