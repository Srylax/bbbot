package dev.srylax.bbbot.db.request.group;

import dev.srylax.bbbot.db.group.type.GroupType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupRequestRepository extends ReactiveMongoRepository<GroupRequest,String> {

}
