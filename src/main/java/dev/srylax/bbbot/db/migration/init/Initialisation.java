package dev.srylax.bbbot.db.migration.init;


import dev.srylax.bbbot.db.group.type.GroupType;
import dev.srylax.bbbot.db.group.type.GroupTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class Initialisation {

    private final Logger logger = LoggerFactory.getLogger(Initialisation.class);

    @Bean
    public CommandLineRunner loadGroupTypes(GroupTypeRepository repository) {
        return args -> {
            Flux<GroupType> groupTypes = Flux.just(
                    GroupType.of("ABU"),
                    GroupType.of("BM"),
                    GroupType.of("PROJECT")
            );
            groupTypes.flatMap(repository::save)
                    .onErrorContinue((ex,obj)->{})
                            .subscribe(e->logger.info("Added: {}",e.getName()));
        };
    }
}
