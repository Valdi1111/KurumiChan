package org.valdi.kurumi.discord.commands;

import com.google.gson.stream.JsonReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.valdi.kurumi.*;
import org.valdi.kurumi.discord.Command;
import org.valdi.kurumi.myanimelist.MalAnime;
import org.valdi.kurumi.myanimelist.MyAnimeListConstants;
//import org.valdi.kurumi.webserver.listeners.EventEpisodeListener;
//import org.valdi.kurumi.webserver.responses.EventEpisodeResponse;
import org.valdi.kurumi.KurumiBot;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class PrintList extends Command {
    public PrintList(KurumiBot animeBot) {
        super(animeBot, "print");
    }

    @Override
    protected boolean onCommand(MessageChannel channel, Message message, Member member, AnimeUser user, List<String> args) {
        if (args.size() == 0) {
            return false;
        }

        if (args.get(0).equalsIgnoreCase("mal")) {
            if (!user.hasMalUser()) {
                return false;
            }

            //user.queryMal();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
            //TODO mal avatar
            //embed.setThumbnail(animeBot.getMalApi().getUser(user.getMalUser()).getPictureURL());
            embed.setThumbnail(member.getUser().getEffectiveAvatarUrl());
            embed.setTitle("User's myanimelist", String.format(MyAnimeListConstants.PROFILE_LINK, user.getMalUser()));
            embed.setDescription(user.getMalUser() + " watching list");
            embed.setColor(Color.CYAN);

            String descFormat = "Progress: %s/%s\nVoto: %s\n[link](%s)";
            List<MalAnime> al = user.getMalAnimeCache();
            al.stream()
                    .filter(a -> a.getListStatus().getStatus() == MalAnime.Status.watching)
                    .forEach(a -> {
                        String title = a.getNode().getTitle();
                        String desc = String.format(descFormat,
                                a.getListStatus().getWatchedEpisodes(),
                                "?", //a.getNode().getEpisodes(),
                                a.getListStatus().getScore(),
                                String.format(MyAnimeListConstants.ANIME_LINK, a.getNode().getId()));
                        embed.addField(title, desc, true);
                    });

            channel.sendMessage(embed.build()).queue();
        } else {
            InputStream is = animeBot.getResourceAsStream(args.get(0));
            InputStreamReader isr = new InputStreamReader(is);
            try (JsonReader reader = new JsonReader(isr)) {
                //EventEpisodeResponse ep = animeBot.getGson().fromJson(reader, EventEpisodeResponse.class);
                //EventEpisodeListener.processEp(ep, animeBot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
