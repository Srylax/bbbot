package dev.srylax.bbbot.db.request.group;

import com.mongodb.lang.NonNull;
import dev.srylax.bbbot.assets.TEXTS;
import dev.srylax.bbbot.db.Embedable;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Document("groupRequest")
public class GroupRequest implements Embedable {

    @Id
    private String id;

    @NonNull
    @lombok.NonNull
    private String groupName;

    @NonNull
    @lombok.NonNull
    private String groupType;

    @NonNull
    @lombok.NonNull
    private String description;

    @NonNull
    @lombok.NonNull
    private Long userId;

    public EmbedCreateSpec toEmbed() {
        return EmbedCreateSpec.create()
                .withFields(
                        EmbedCreateFields.Field.of("ID",id,true),
                        EmbedCreateFields.Field.of(TEXTS.get("User"), String.valueOf(userId),false),
                        EmbedCreateFields.Field.of(TEXTS.get("GroupName"),groupName,true),
                        EmbedCreateFields.Field.of(TEXTS.get("GroupType"),groupType,true),
                        EmbedCreateFields.Field.of(TEXTS.get("Description"), description,false)
                );
    }
}
