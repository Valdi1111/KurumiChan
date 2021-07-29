package org.valdi.kurumi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.entities.User;
import org.valdi.kurumi.anilist.AlAnime;
import org.valdi.kurumi.anilist.AniListConstants;
import org.valdi.kurumi.myanimelist.MalAnime;
import org.valdi.kurumi.myanimelist.MyAnimeListConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AnimeUser {
    private final KurumiBot animeBot;
    @Expose @SerializedName("discord-id")
    private long discordUser;
    @Expose @SerializedName("myanimelist-username")
    private String malUser;
    @Expose @SerializedName("anilist-username")
    private String alUser;

    private ScheduledFuture<?> updater;
    private List<MalAnime> malAnimeCache;
    private List<AlAnime> alAnimeCache;

    public AnimeUser() {
        this.animeBot = Main.getInstance();
    }

    public AnimeUser(long discordUser) {
        this.animeBot = Main.getInstance();
        this.discordUser = discordUser;
        this.createUpdater(animeBot.getScheduler());
    }

    public long getUserId() {
        return discordUser;
    }

    public User getDiscordUser() {
        return animeBot.getJda().getUserById(discordUser);
    }

    public String getDiscordName() {
        User user = getDiscordUser();
        return user == null ? null : user.getName();
    }

    public boolean hasMalUser() {
        return malUser != null && !malUser.isBlank();
    }

    public String getMalUser() {
        return malUser;
    }

    public String getMalUserLink() {
        return String.format(MyAnimeListConstants.PROFILE_LINK, this.getMalUser());
    }

    public boolean hasAnilistUser() {
        return alUser != null && !alUser.isBlank();
    }

    public String getAnilistUser() {
        return alUser;
    }

    public String getAnilistUserLink() {
        return String.format(AniListConstants.PROFILE_LINK, this.getAnilistUser());
    }

    public AnimeUser setMalUser(String malUser) {
        this.malUser = malUser;
        return this;
    }

    public AnimeUser setAnilistUser(String alUser) {
        this.alUser = alUser;
        return this;
    }

    public ScheduledFuture<?> getUpdater() {
        return updater;
    }

    public void setUpdater(ScheduledFuture<?> updater) {
        if (this.updater != null) {
            throw new RuntimeException("Updater already exists!");
        }

        this.updater = updater;
    }

    public void createUpdater(AnimeUsersScheduler scheduler) {
        if (updater != null) {
            animeBot.getLogger().error("User {} updater already exists!", this.getDiscordUser());
            return;
        }

        ScheduledFuture<?> updater = scheduler.getScheduler().scheduleAtFixedRate(() -> {
            this.queryMal();
            this.queryAnilist();
        }, 0L, 6L, TimeUnit.HOURS);

        this.setUpdater(updater);
    }

    public List<MalAnime> getMalAnimeCache() {
        return malAnimeCache;
    }

    public List<AlAnime> getAlAnimeCache() {
        return alAnimeCache;
    }

    public void queryMal() {
        if(!this.hasMalUser()) {
            animeBot.getLogger().info("Skipping MyAnimeList query for {}...", this.getDiscordName());
            return;
        }

        animeBot.getLogger().info("Starting MyAnimeList query for {}...", malUser);
        try {
            this.malAnimeCache = animeBot.getMalApi().queryAnimeList(malUser);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void queryAnilist() {
        if(!this.hasAnilistUser()) {
            animeBot.getLogger().info("Skipping Anilist query for {}...", this.getDiscordName());
            return;
        }

        animeBot.getLogger().info("Starting Anilist query for {}...", alUser);
        try {
            this.alAnimeCache = animeBot.getAlApi().queryAnimeList(alUser);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
