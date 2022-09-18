package dev.srylax.bbbot.commands.group.request;

import dev.srylax.bbbot.commands.ReactiveEventListener;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import dev.srylax.bbbot.db.request.group.GroupRequestRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class GroupRequestCommand extends ReactiveEventListener {
    protected final String commandGroup;

    protected static final String COMMAND_SUP_GROUP = "group-request";
    protected final GroupRequestRepository groupRequestRepository;

    public GroupRequestCommand(GatewayDiscordClient client, GroupRequestRepository groupRequestRepository, String commandGroup) {
        super(client);
        this.commandGroup = commandGroup;
        this.groupRequestRepository = groupRequestRepository;
    }
    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals(commandGroup) || event.getOption(COMMAND_SUP_GROUP).isEmpty())
            return Mono.empty();

        String search = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        Flux<ApplicationCommandOptionChoiceData> options = groupRequestRepository.findByGroupNameLikeIgnoreCase(search)
                .map(e -> ApplicationCommandOptionChoiceData.builder()
                        .name(e.getGroupName())
                        .value(e.getId())
                        .build());
        return options.collectList()
                .flatMap(event::respondWithSuggestions);
    }
}
