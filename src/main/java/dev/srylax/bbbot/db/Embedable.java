package dev.srylax.bbbot.db;

import discord4j.core.spec.EmbedCreateSpec;

public interface Embedable {
    EmbedCreateSpec toEmbed();
}
