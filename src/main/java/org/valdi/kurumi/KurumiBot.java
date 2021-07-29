package org.valdi.kurumi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valdi.kurumi.anilist.AniListApi;
import org.valdi.kurumi.anilist.AniListConstants;
import org.valdi.kurumi.anilist.auth.AniListAuthenticator;
import org.valdi.kurumi.auth.AuthToken;
import org.valdi.kurumi.auth.ResponseServer;
import org.valdi.kurumi.auth.TokenRetrieveException;
import org.valdi.kurumi.discord.CommandManager;
import org.valdi.kurumi.discord.DiscordConfig;
import org.valdi.kurumi.discord.commands.AnilistCommand;
import org.valdi.kurumi.discord.commands.InfoCommand;
import org.valdi.kurumi.discord.commands.MyAnimeListCommand;
import org.valdi.kurumi.discord.commands.PrintList;
import org.valdi.kurumi.myanimelist.MyAnimeListApi;
import org.valdi.kurumi.myanimelist.auth.MyAnimeListAuthenticator;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Scanner;

public class KurumiBot {
    private static final Logger logger = LogManager.getLogger("Main");

    private final AuthCredentials authCredentials;
    private final Gson gson;

    private JDA jda;
    private CommandManager cmdManager;

    private AnimeUsersScheduler scheduler;
    private ResponseServer server;
    private MyAnimeListApi malApi;
    private AniListApi alApi;

    private DiscordConfig discordConfig;
    private AnimeUsers animeUsers;

    private File jar;
    private File discordConfigFile;
    private File usersFile;

    private boolean running = false;

    public KurumiBot(String... args) throws Exception {
        Main.setInstance(this);

        Dotenv dotenv = Dotenv.configure().load();
        this.authCredentials = new AuthCredentials()
                .setAwClientId(dotenv.get("AW_CLIENT_ID"))
                .setAwApiKey(dotenv.get("AW_API_KEY"))
                .setDiscordToken(dotenv.get("DISCORD_TOKEN"))
                .setMalClientId(dotenv.get("MAL_CLIENT_ID"))
                .setMalClientSecret(dotenv.get("MAL_CLIENT_SECRET"))
                .setAlClientId(dotenv.get("AL_CLIENT_ID"))
                .setAlClientSecret(dotenv.get("AL_CLIENT_SECRET"));

        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping()
                .create();

        this.start();

        Scanner scanner = new Scanner(System.in);
        String command = "";
        while (!command.equalsIgnoreCase("/stop")
                && !command.equalsIgnoreCase("/close")
                && running) {
            command = scanner.nextLine();
            ((MessageChannel) jda.getGuildChannelById(discordConfig.getChannel())).sendMessage(command).queue();
        }

        this.stop();
    }

    public synchronized void start() throws Exception {
        this.running = true;

        this.loadUsers();
        this.loadDiscordConfig();
        this.startDiscordBot();

        this.startResponseServer();
        this.startMalApi();
        this.startAlApi();
        this.startScheduler();

        /*this.startWebserver();*/
        // TODO load modules
    }

    public synchronized void stop() throws Exception {
        this.running = false;

        /*this.stopWebserver();*/
        // TODO unload modules

        this.stopScheduler();
        this.stopMalApi();
        this.stopAlApi();
        this.stopResponseServer();

        this.stopDiscordBot();
        this.saveDiscordConfig();
        this.saveUsers();
    }

    private void startDiscordBot() throws InterruptedException, LoginException {
        logger.info("Starting Discord API...");
        JDABuilder builder = JDABuilder.createDefault(authCredentials.getDiscordToken());
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.watching("Anime World"));

        this.jda = builder
                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_EMOJIS)
                .build();
        this.jda.awaitReady();

        this.jda.setEventManager(new AnnotatedEventManager());
        this.cmdManager = new CommandManager(this);
        cmdManager.registerCommand(new InfoCommand(this));
        cmdManager.registerCommand(new AnilistCommand(this));
        cmdManager.registerCommand(new MyAnimeListCommand(this));
        cmdManager.registerCommand(new PrintList(this));

        this.jda.addEventListener(cmdManager);
    }

    private void stopDiscordBot() {
        logger.info("Shutting down Discord API...");
        this.jda.shutdown();
        this.jda = null;
    }

    private void startScheduler() {
        logger.info("Starting users Scheduler...");
        this.scheduler = new AnimeUsersScheduler(this);
        this.scheduler.start();
    }

    public void stopScheduler() {
        logger.info("Shutting down users Scheduler...");
        this.scheduler.close();
        this.scheduler = null;
    }

    private void startResponseServer() throws IOException {
        logger.info("Starting Response server...");
        this.server = new ResponseServer(5050);
        this.server.start();
    }

    private void stopResponseServer() {
        logger.info("Shutting Response server...");
        this.server.close();
        this.server = null;
    }

    private void startMalApi() throws IOException, TokenRetrieveException {
        logger.info("Starting MyAnimeList API...");
        MyAnimeListAuthenticator auth = new MyAnimeListAuthenticator(
                server,
                authCredentials.getMalClientId(),
                authCredentials.getMalClientSecret())
                .setURLCallback(System.out::println);
        this.malApi = new MyAnimeListApi(auth);

        File tokenFile = new File(this.getParentFolder(), ".myanimelist-token");
        if (tokenFile.exists()) {
            try (FileReader fr = new FileReader(tokenFile);
                 JsonReader reader = new JsonReader(fr)) {
                AuthToken malToken = gson.fromJson(reader, AuthToken.class);
                this.malApi.start(malToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        tokenFile.createNewFile();
        this.malApi.start();

        AuthToken malToken = malApi.getToken();
        try (FileWriter fw = new FileWriter(tokenFile);
             JsonWriter writer = new JsonWriter(fw)) {
            gson.toJson(malToken, AuthToken.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMalApi() {
        logger.info("Shutting down MyAnimeList API...");
        this.malApi.close();
        this.malApi = null;
    }

    private void startAlApi() throws IOException, TokenRetrieveException {
        logger.info("Starting AniList API...");
        AniListAuthenticator auth = new AniListAuthenticator(
                server,
                authCredentials.getAlClientId(),
                authCredentials.getAlClientSecret(),
                Constants.RESPONSE_REDIRECT_URL + AniListConstants.CONTEXT)
                .setURLCallback(System.out::println);
        this.alApi = new AniListApi(auth);

        File tokenFile = new File(this.getParentFolder(), ".anilist-token");
        if (tokenFile.exists()) {
            try (FileReader fr = new FileReader(tokenFile);
                 JsonReader reader = new JsonReader(fr)) {
                AuthToken alToken = gson.fromJson(reader, AuthToken.class);
                this.alApi.start(alToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        tokenFile.createNewFile();
        this.alApi.start();

        AuthToken alToken = alApi.getToken();
        try (FileWriter fw = new FileWriter(tokenFile);
             JsonWriter writer = new JsonWriter(fw)) {
            gson.toJson(alToken, AuthToken.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAlApi() {
        logger.info("Shutting down AniList API...");
        this.alApi.close();
        this.alApi = null;
    }

    public void loadDiscordConfig() throws IOException {
        this.discordConfigFile = new File(this.getParentFolder(), "discord-config.json");
        this.discordConfig = new DiscordConfig();
        boolean newFile = this.discordConfigFile.createNewFile();
        if (newFile) {
            this.saveDiscordConfig();
            return;
        }

        try (FileReader fr = new FileReader(discordConfigFile);
             JsonReader reader = new JsonReader(fr)) {
            this.discordConfig = gson.fromJson(reader, DiscordConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDiscordConfig() {
        try (FileWriter fw = new FileWriter(discordConfigFile);
             JsonWriter writer = new JsonWriter(fw)) {
            gson.toJson(this.discordConfig, DiscordConfig.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUsers() throws IOException {
        this.usersFile = new File(this.getParentFolder(), "users.json");
        this.animeUsers = new AnimeUsers();
        boolean newFile = this.usersFile.createNewFile();
        if (newFile) {
            this.saveUsers();
            return;
        }

        try (FileReader fr = new FileReader(usersFile);
             JsonReader reader = new JsonReader(fr)) {
            Type listType = new TypeToken<Collection<AnimeUser>>() {
            }.getType();
            Collection<AnimeUser> usersList = gson.fromJson(reader, listType);
            this.animeUsers.addAll(usersList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUsers() {
        try (FileWriter fw = new FileWriter(usersFile);
             JsonWriter writer = new JsonWriter(fw)) {
            Type listType = new TypeToken<Collection<AnimeUser>>() {
            }.getType();
            gson.toJson(this.animeUsers.values(), listType, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public AuthCredentials getAuthCredentials() {
        return authCredentials;
    }

    public Gson getGson() {
        return gson;
    }

    public JDA getJda() {
        return jda;
    }

    public CommandManager getCommandManager() {
        return cmdManager;
    }

    public AnimeUsersScheduler getScheduler() {
        return scheduler;
    }

    public MyAnimeListApi getMalApi() {
        return malApi;
    }

    public AniListApi getAlApi() {
        return alApi;
    }

    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    public AnimeUsers getAnimeUsers() {
        return animeUsers;
    }

    public File getJarFile() {
        if (this.jar == null) {
            try {
                URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
                this.jar = new File(url.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return this.jar;
    }

    public File getParentFolder() {
        return this.getJarFile().getParentFile();
    }

    public InputStream getResourceAsStream(String resource) {
        return Main.class.getClassLoader().getResourceAsStream(resource);
    }

}
