package dev.srylax.bbbot.db.group;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group,String> {

    Mono<Boolean> existsByName(String name);

    Mono<Void> deleteByName(String name);

    Flux<Group> findByNameLikeIgnoreCase(String name);
}
