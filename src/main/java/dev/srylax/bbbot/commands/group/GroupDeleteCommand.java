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

import java.util.function.Function;


@Component
public class GroupDeleteCommand extends ReactiveEventListener {
    private final GroupRepository groupRepository;

    public GroupDeleteCommand(GatewayDiscordClient client, GroupRepository groupRepository) {
        super(client);
        this.groupRepository = groupRepository;
    }

    @Override
    public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("delete").isEmpty()) return Mono.empty();

        ApplicationCommandInteractionOption commandOption = event.getOption("delete").get();
        String name = getRequiredValue(commandOption,"name").asString();



        Flux<Void> deleteCategory = event.getInteraction().getGuild()
                .flatMapMany(Guild::getChannels)
                .filter(Category.class::isInstance)
                .cast(Category.class)
                .filter(g->g.getName().equalsIgnoreCase(name))
                .flatMap(c->
                        c.getChannels().flatMap(Channel::delete)
                        .mergeWith(c.delete()));

        Flux<Void> deleteRole = event.getInteraction().getGuild()
                .flatMapMany(Guild::getRoles)
                .filter(r->r.getName().equalsIgnoreCase(name))
                .flatMap(Role::delete);

        return event.deferReply().withEphemeral(true)
                .then(groupRepository.deleteByName(name))
                .thenMany(deleteCategory)
                .thenMany(deleteRole)
                .then(event.createFollowup(TEXTS.get("GroupDeleteSuccess",name)));
    }

    @Override
    public @NotNull Publisher<?> onChatInputAutoCompleteInteraction(@NotNull ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("group") || event.getOption("delete").isEmpty()) return Mono.empty();

        String search = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        Flux<ApplicationCommandOptionChoiceData> options = groupRepository.findByNameLikeIgnoreCase(search)
                .map(e -> ApplicationCommandOptionChoiceData.builder()
                        .name(e.getName())
                        .value(e.getName())
                        .build());
        return options.collectList()
                .flatMap(event::respondWithSuggestions);
    }
}
