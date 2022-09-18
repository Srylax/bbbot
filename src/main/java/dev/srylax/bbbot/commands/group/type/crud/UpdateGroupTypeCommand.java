package dev.srylax.bbbot.commands.group.type.crud;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.commands.ReactiveEventListener;
import dev.srylax.bbbot.commands.group.type.GroupTypeCommand;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.util.Color;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class UpdateGroupTypeCommand extends GroupTypeCommand {

    public UpdateGroupTypeCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository) {
        super(client,groupTypeRepository,"update");
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals(commandGroup) || event.getOption(COMMAND_SUP_GROUP).isEmpty())
            return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption(COMMAND_SUP_GROUP).get();
        String id = getRequiredValue(commandOption, "id").asString();
        String name = getRequiredValue(commandOption, "name").asString();

        return event.deferReply().withEphemeral(true)
                .then(groupTypeRepository.findById(id))
                .doOnNext(g->g.setName(name))
                .flatMap(groupTypeRepository::save)
                .map(g -> g.toEmbed()
                        .withColor(Color.GREEN)
                        .withTitle(TEXTS.get("GroupType")))
                .onErrorReturn(EmbedCreateSpec.create()
                        .withColor(Color.RED)
                        .withTitle(TEXTS.get("GroupTypeNotFound", id)))
                .flatMap(e -> event.createFollowup(InteractionFollowupCreateSpec.create()
                        .withEmbeds(e)));
    }
}
