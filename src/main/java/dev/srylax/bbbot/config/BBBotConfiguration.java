package dev.srylax.bbbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class BBBotConfiguration {

    @Value("${BOT_TOKEN}")
    private String token;


    @Value("${GUILD_ID}")
    private Long guildId;

    @Bean
    public CommandLineRunner registerCommands(GatewayDiscordClient client) throws IOException {
        ObjectMapper mapper = JacksonResources.create().getObjectMapper();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

        List<ApplicationCommandRequest> requests = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = mapper.readValue(resource.getURL(),ApplicationCommandRequest.class);
            requests.add(request);
        }

        return args -> client.getRestClient().getApplicationService().bulkOverwriteGuildApplicationCommand(
                client.getRestClient().getApplicationId().blockOptional().orElseThrow(),
                guildId,
                requests
        ).subscribe();
    }


    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        ReactorResources resources = ReactorResources.builder().blockingTaskScheduler(Schedulers.boundedElastic()).build();
        return DiscordClientBuilder.create(token).setReactorResources(resources)
                .build()
                .gateway().setInitialPresence(ignore ->
                        ClientPresence.online(
                                ClientActivity.listening(
                                        "BBB Wertebaum"
                                ))
                )
                .login()
                .blockOptional()
                .orElseThrow();
    }

}
