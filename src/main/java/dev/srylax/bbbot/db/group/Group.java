package dev.srylax.bbbot.db.group;

import com.mongodb.lang.NonNull;
import dev.srylax.bbbot.db.group.type.GroupType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@RequiredArgsConstructor
@Document("group")
public class Group {
    @Id
    private String id;

    @Indexed(unique = true)
    @NonNull
    @lombok.NonNull
    private String name;

    @NonNull
    @lombok.NonNull
    private GroupType groupType;
}
