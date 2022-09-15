package dev.srylax.bbbot.db.group.type;

import com.mongodb.lang.NonNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Document("groupType")
public class GroupType {
    @Id
    private String id;

    @NonNull
    @lombok.NonNull
    @Indexed(unique = true, background = true)
    private String name;

    public static GroupType of(String name) {
        return new GroupType(name);
    }
}
