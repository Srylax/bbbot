package dev.srylax.bbbot.commands.group.request;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.db.group.Group;
import dev.srylax.bbbot.db.group.GroupRepository;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.*;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.InteractionData;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Component
public class GroupRequestCommand extends ReactiveEventAdapter {
    private final GroupTypeRepository groupTypeRepository;

    public GroupRequestCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository) {
        this.groupTypeRepository = groupTypeRepository;
        client.on(this).subscribe();
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("request")) return Mono.empty();

        String name = event.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(IllegalStateException::new);
        String type = event.getOption("type")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(IllegalStateException::new);
        String description = event.getOption("description")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(IllegalStateException::new);


        EmbedCreateSpec groupRequestEmbed = EmbedCreateSpec.create()
                .withColor(Color.YELLOW)
                .withTitle("New Group Request from " + event.getInteraction().getUser().getTag())
                .withFields(
                        EmbedCreateFields.Field.of(TEXTS.get("GroupName"),name,true),
                        EmbedCreateFields.Field.of(TEXTS.get("GroupType"),type,true),
                        EmbedCreateFields.Field.of(TEXTS.get("GroupType"),event.getInteraction().getUser().getMention(),false),
                        EmbedCreateFields.Field.of(TEXTS.get("Description"),description,false)
                );

        MessageCreateSpec groupRequestMessage = MessageCreateSpec.create()
                .withEmbeds(groupRequestEmbed)
                .withComponents(ActionRow.of(
                        Button.danger("approveRequest",TEXTS.get("Approve")),
                        Button.success("denyRequest",TEXTS.get("Deny")
                        )));




        return event.reply(TEXTS.get("GroupRequestCreated")).withEphemeral(true)
                .then(event.getInteraction().getGuild()
                        .flatMap(Guild::getSystemChannel)
                        .flatMap(c->c.createMessage(groupRequestMessage)));
    }

    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(@NotNull ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("request")) return Mono.empty();

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
