package dev.srylax.bbbot.commands.group;

import dev.srylax.bbbot.assets.TEXTS;

public class GroupTypeNotFoundException extends RuntimeException {
    public GroupTypeNotFoundException(String type) {
        super(TEXTS.get("GroupTypeNotFound",type));
    }
}
