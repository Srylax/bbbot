package dev.srylax.bbbot.commands.request.group;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.commands.ReactiveEventListener;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import dev.srylax.bbbot.db.request.group.GroupRequest;
import dev.srylax.bbbot.db.request.group.GroupRequestRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.*;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.util.Color;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Component
public class RequestGroupCommand extends ReactiveEventListener {
    private final GroupTypeRepository groupTypeRepository;
    private final GroupRequestRepository groupRequestRepository;

    public RequestGroupCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository, GroupRequestRepository groupRequestRepository) {
        super(client);
        this.groupTypeRepository = groupTypeRepository;
        this.groupRequestRepository = groupRequestRepository;
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("request") || event.getOption("group").isEmpty()) return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption("group").get();

        String name = getRequiredValue(commandOption, "name").asString();
        String type = getRequiredValue(commandOption, "type").asString();
        String description = getRequiredValue(commandOption, "description").asString();


        Function<EmbedCreateSpec, MessageCreateSpec> groupRequestMessage = embed -> MessageCreateSpec.create()
                .withEmbeds(embed
                        .withColor(Color.YELLOW)
                        .withTitle(TEXTS.get("NewGroupRequest") + " " + TEXTS.get("from") + " " + event.getInteraction().getUser().getTag()))
                .withComponents(ActionRow.of(
                        Button.danger("approveRequest", TEXTS.get("Approve")),
                        Button.success("denyRequest", TEXTS.get("Deny")
                        )));


        GroupRequest groupRequest = new GroupRequest(name, type, description, event.getInteraction().getUser().getId().asLong());

        return event.reply(TEXTS.get("GroupRequestCreated")).withEphemeral(true)
                .then(groupRequestRepository.save(groupRequest))
                .map(GroupRequest::toEmbed)
                .map(groupRequestMessage)
                .flatMap(m -> event.getInteraction().getGuild()
                        .flatMap(Guild::getSystemChannel)
                        .flatMap(c -> c.createMessage(m)));
    }

    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(@NotNull ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("request") || event.getOption("group").isEmpty()) return Mono.empty();


        String search = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        Flux<ApplicationCommandOptionChoiceData> options = groupTypeRepository.findByNameLikeIgnoreCase(search)
                .map(e -> ApplicationCommandOptionChoiceData.builder()
                        .name(e.getName())
                        .value(e.getName())
                        .build());
        return options.collectList()
                .flatMap(event::respondWithSuggestions);
    }
}
