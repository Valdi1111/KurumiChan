package org.valdi.kurumi;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AnimeUsersScheduler extends Thread {
    private final KurumiBot animeBot;
    private final ScheduledExecutorService scheduler;

    public AnimeUsersScheduler(KurumiBot animeBot) {
        super("Users Scheduler");

        this.animeBot = animeBot;

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("Users Thread #%d").build();
        this.scheduler = Executors.newScheduledThreadPool(5, factory);
    }

    @Override
    public void run() {
        super.run();

        List<AnimeUser> users = new ArrayList<>(animeBot.getAnimeUsers().values());
        AtomicInteger index = new AtomicInteger();

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("Users Scheduler").build();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(factory);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            try {
                if (index.get() == users.size()) {
                    executor.shutdownNow();
                    return;
                }

                AnimeUser user = users.get(index.get());
                user.createUpdater(this);
                index.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3L, 5L, TimeUnit.SECONDS);
    }

    public void close() {
        this.scheduler.shutdownNow();
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
