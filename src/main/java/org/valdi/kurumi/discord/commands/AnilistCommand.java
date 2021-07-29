package org.valdi.kurumi.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.valdi.kurumi.AnimeUser;
import org.valdi.kurumi.KurumiBot;
import org.valdi.kurumi.discord.Command;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class AnilistCommand extends Command {

    public AnilistCommand(KurumiBot animeBot) {
        super(animeBot, "anilist", Arrays.asList("al"));
    }

    @Override
    protected boolean onCommand(MessageChannel channel, Message message, Member member, AnimeUser user, List<String> args) {
        if(args.size() < 1) {
            MessageBuilder builder = new MessageBuilder();
            builder.append("Devi inserire il tuo username di AniList!");
            builder.append("\n");
            builder.append("Usage: a!anilist [username]", MessageBuilder.Formatting.BOLD);

            channel.sendMessage(builder.build()).queue();
            return false;
        }

        user.setAnilistUser(args.get(0));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        embed.setThumbnail(member.getUser().getEffectiveAvatarUrl());
        embed.setTitle("AniList: " + user.getAnilistUser(), user.getAnilistUserLink());
        embed.setDescription("Lo username di AniList è stato aggiornato!");
        embed.setColor(Color.CYAN);

        channel.sendMessage(embed.build()).queue();
        animeBot.saveUsers();
        user.queryAnilist();
        return true;
    }
}
