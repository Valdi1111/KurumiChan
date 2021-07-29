package org.valdi.kurumi.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.valdi.kurumi.AnimeUser;
import org.valdi.kurumi.KurumiBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Command {
    protected final Map<String, Command> subcommands;
    protected final KurumiBot animeBot;
    protected final String name;
    protected final List<String> aliases;
    protected Command command;

    public Command(KurumiBot animeBot, String name) {
        this(animeBot, name, new ArrayList<>());
    }

    public Command(KurumiBot animeBot, String name, List<String> aliases) {
        this.animeBot = animeBot;
        this.subcommands = new HashMap<>();
        this.name = name.toLowerCase();
        this.aliases = aliases;
    }

    public Command(KurumiBot animeBot, Command command, String name) {
        this(animeBot, command, name, new ArrayList<>());
    }

    public Command(KurumiBot animeBot, Command command, String name, List<String> aliases) {
        this.animeBot = animeBot;
        this.subcommands = new HashMap<>();
        this.name = name.toLowerCase();
        this.aliases = aliases;
        this.command = command;

        command.registerSubcommand(this);
    }

    public boolean executeCommand(MessageChannel channel, Message message, Member member, AnimeUser user, List<String> split) {
        List<String> args = new ArrayList<>();
        for (String arg : split) {
            if(arg.isEmpty() || arg.isBlank()) {
                continue;
            }

            args.add(arg);
        }

        if(args.size() > 1 && subcommands.containsKey(args.get(0).toLowerCase())) {
            return subcommands.get(args.get(0).toLowerCase()).onCommand(channel, message, member, user, args.subList(1, args.size()));
        }

        return this.onCommand(channel, message, member, user, args);
    }

    protected abstract boolean onCommand(MessageChannel channel, Message message, Member member, AnimeUser user, List<String> args);

    public void registerSubcommand(Command command) {
        subcommands.put(command.getName(), command);
        for(String alias : command.getAliases()) {
            subcommands.put(alias, command);
        }
    }

    public Map<String, Command> getSubcommands() {
        return subcommands;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getName() {
        return name;
    }
}
