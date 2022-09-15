package dev.srylax.bbbot.commands.group;

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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.RoleCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
public class GroupAddCommand extends ReactiveEventAdapter {
    private final GroupTypeRepository groupTypeRepository;
    private final GroupRepository groupRepository;

    public GroupAddCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository, GroupRepository groupRepository) {
        this.groupTypeRepository = groupTypeRepository;
        this.groupRepository = groupRepository;
        client.on(this).subscribe();
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("add").isEmpty()) return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption("add").get();
        String name = commandOption.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(IllegalStateException::new);
        String type = commandOption.getOption("type")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(IllegalStateException::new);


        Flux<Channel> createGroup = event.getInteraction()
                .getGuild()
                .flatMapMany(g ->
                        Flux.concat(
                                        g.createRole(RoleCreateSpec.create().withName(name))
                                                .map(r ->
                                                        PermissionOverwrite.forRole(r.getId(),
                                                                PermissionSet.of(
                                                                        Permission.VIEW_CHANNEL,
                                                                        Permission.READ_MESSAGE_HISTORY,
                                                                        Permission.SEND_MESSAGES,
                                                                        Permission.ADD_REACTIONS,
                                                                        Permission.CONNECT,
                                                                        Permission.SPEAK), PermissionSet.of()
                                                        )),
                                        g.getEveryoneRole()
                                                .map(r ->
                                                        PermissionOverwrite.forRole(r.getId(),
                                                                PermissionSet.of(), PermissionSet.of(
                                                                        Permission.VIEW_CHANNEL))
                                                )
                                ).collectList()
                                .flatMapMany(r ->
                                        g.createCategory(name)
                                                .withPermissionOverwrites(r)
                                                .flatMapMany(cat ->
                                                        Flux.<Channel>concat(
                                                                g.createTextChannel(name)
                                                                        .withParentId(cat.getId()),
                                                                g.createVoiceChannel(name)
                                                                        .withParentId(cat.getId())
                                                        )
                                                )
                                )
                );


        return event.deferReply().withEphemeral(true)
                .then(
                        groupTypeRepository.findById(type)
                                .flatMap(t -> groupRepository.save(new Group(name, t)))
                                .and(event.createFollowup(TEXTS.get("GroupCreated", name)))
                                .thenMany(createGroup)
                                .then(event.createFollowup(TEXTS.get("ChannelsCreated", name)))
                )
                .onErrorResume(e -> event.createFollowup(TEXTS.get("GroupAlreadyExists", name)).withEphemeral(true));
    }

    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(@NotNull ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("add").isEmpty()) return Mono.empty();

        Flux<ApplicationCommandOptionChoiceData> options = groupTypeRepository.findAll()
                .map(e -> ApplicationCommandOptionChoiceData.builder()
                        .name(e.getName())
                        .value(e.getId())
                        .build());
        return options.collectList()
                .flatMap(event::respondWithSuggestions);
    }
}
