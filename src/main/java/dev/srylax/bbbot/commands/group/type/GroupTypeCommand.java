package dev.srylax.bbbot.commands.group.type;

import dev.srylax.bbbot.commands.ReactiveEventListener;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class GroupTypeCommand extends ReactiveEventListener {
    protected final String commandGroup;

    protected static final String COMMAND_SUP_GROUP = "group-type";
    protected final GroupTypeRepository groupTypeRepository;

    public GroupTypeCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository, String commandGroup) {
        super(client);
        this.commandGroup = commandGroup;
        this.groupTypeRepository = groupTypeRepository;
    }

    @Override
    public Publisher<?> onChatInputAutoCompleteInteraction(ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals(commandGroup) || event.getOption(COMMAND_SUP_GROUP).isEmpty())
            return Mono.empty();

        String search = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        Flux<ApplicationCommandOptionChoiceData> options = groupTypeRepository.findByNameLikeIgnoreCase(search)
                .map(e -> ApplicationCommandOptionChoiceData.builder()
                        .name(e.getName())
                        .value(e.getId())
                        .build());
        return options.collectList()
                .flatMap(event::respondWithSuggestions);
    }
}
