package org.valdi.kurumi.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.valdi.kurumi.AnimeUser;
import org.valdi.kurumi.discord.Command;
import org.valdi.kurumi.KurumiBot;

import java.awt.*;
import java.util.List;

public class InfoCommand extends Command {

    public InfoCommand(KurumiBot animeBot) {
        super(animeBot, "info");
    }

    @Override
    protected boolean onCommand(MessageChannel channel, Message message, Member member, AnimeUser user, List<String> args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        embed.setThumbnail(member.getUser().getEffectiveAvatarUrl());
        embed.setTitle("User's profile");
        embed.setDescription("Aggiungendo il tuo username di MyAnimeList/AniList, " +
                "A-chan potrà inviarti una notifica ogni volta che esce un anime presente nella tua plan-to-watch!");
        embed.addField("MyAnimeList", user.hasMalUser() ? user.getMalUserLink() : "Aggiungi con a!mal [username]", true);
        embed.addField("AniList", user.hasAnilistUser() ? user.getAnilistUserLink() : "Aggiungi con a!anilist [username]", true);
        embed.setColor(Color.CYAN);

        channel.sendMessage(embed.build()).queue();
        return true;
    }
}
