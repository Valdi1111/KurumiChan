package org.valdi.kurumi.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.valdi.kurumi.AnimeUser;
import org.valdi.kurumi.Constants;
import org.valdi.kurumi.KurumiBot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands;
    private final KurumiBot animeBot;

    public CommandManager(KurumiBot animeBot) {
        this.commands = new HashMap<>();
        this.animeBot = animeBot;
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent e) {
        Member member = e.getMember();
        if (member == null) {
            return;
        }

        Message message = e.getMessage();
        MessageChannel channel = e.getChannel();
        String command = message.getContentRaw();
        if (!command.startsWith(Constants.COMMAND_PREFIX)) {
            // TODO handle mention
            return;
        }

        command = command.trim();
        command = command.substring(Constants.COMMAND_PREFIX.length());
        command = command.trim();
        if (command.isEmpty() || command.isBlank()) {
            return;
        }

        animeBot.getLogger().info("Processing " + command);
        List<String> split = Arrays.asList(command.split(" "));
        String cmd = split.get(0).toLowerCase();
        if(!commands.containsKey(cmd)) {
            return;
        }

        long userId = member.getIdLong();
        if (!animeBot.getAnimeUsers().containsKey(userId)) {
            animeBot.getAnimeUsers().add(new AnimeUser(userId));
        }

        AnimeUser user = animeBot.getAnimeUsers().get(userId);
        commands.get(cmd).executeCommand(channel, message, member, user, split.subList(1, split.size()));
    }

    public void registerCommand(Command command) {
        commands.put(command.getName(), command);
        for(String alias : command.getAliases()) {
            commands.put(alias, command);
        }
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}
