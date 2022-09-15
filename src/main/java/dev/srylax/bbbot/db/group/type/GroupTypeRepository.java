package dev.srylax.bbbot.db.group.type;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupTypeRepository extends ReactiveMongoRepository<GroupType,String> {
    Mono<Boolean> existsByName(String name);
}
