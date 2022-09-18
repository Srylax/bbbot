package dev.srylax.bbbot.commands.group;

import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.commands.ReactiveEventListener;
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
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
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
public class GroupAddCommand extends ReactiveEventListener {
    private final GroupTypeRepository groupTypeRepository;
    private final GroupRepository groupRepository;

    public GroupAddCommand(GatewayDiscordClient client, GroupTypeRepository groupTypeRepository, GroupRepository groupRepository) {
        super(client);
        this.groupTypeRepository = groupTypeRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("add").isEmpty()) return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption("add").get();
        String name = getRequiredValue(commandOption,"name").asString();
        String type = getRequiredValue(commandOption,"type").asString();

        Mono<Role> createRole = event.getInteraction().getGuild()
                .flatMap(g -> g.createRole(RoleCreateSpec.create().withName(name)));

        Mono<PermissionOverwrite> everyoneRolePermission = event.getInteraction().getGuild()
                .flatMap(Guild::getEveryoneRole)
                .map(r -> PermissionOverwrite.forRole(
                        r.getId(),
                        PermissionSet.of(),
                        PermissionSet.of(Permission.VIEW_CHANNEL)
                ));

        Function<Role, PermissionOverwrite> toPermissionOverwrite = r ->
                PermissionOverwrite.forRole(r.getId(),
                        PermissionSet.of(
                                Permission.VIEW_CHANNEL,
                                Permission.READ_MESSAGE_HISTORY,
                                Permission.SEND_MESSAGES,
                                Permission.ADD_REACTIONS,
                                Permission.CONNECT,
                                Permission.SPEAK),
                        PermissionSet.of()
                );


        Function<List<PermissionOverwrite>, Mono<Category>> createCategory = p -> event.getInteraction().getGuild()
                .flatMap(g ->
                        g.createCategory(name)
                                .withPermissionOverwrites(p)
                );

        Function<Category, Flux<Channel>> createChannels = c -> event.getInteraction().getGuild()
                .flatMapMany(g ->
                        Flux.concat(
                                g.createTextChannel(name)
                                        .withParentId(c.getId()),
                                g.createVoiceChannel(name)
                                        .withParentId(c.getId())
                        ));


        return event.deferReply().withEphemeral(true)
                .then(groupTypeRepository.findById(type))
                .flatMap(t->groupRepository.save(new Group(name,t)))
                .switchIfEmpty(Mono.error(new GroupTypeNotFoundException(type)))
                .then(createRole)
                .map(toPermissionOverwrite)
                .mergeWith(everyoneRolePermission).collectList()
                .flatMap(createCategory)
                .flatMapMany(createChannels)
                .then(event.createFollowup(TEXTS.get("GroupCreated", name)))
                .onErrorResume(e -> event.createFollowup(TEXTS.get("CouldNotCompleteAction", e.getMessage())));
    }

    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(@NotNull ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("add").isEmpty()) return Mono.empty();

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
