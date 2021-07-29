package org.valdi.kurumi;

public class Main {
    private static KurumiBot instance = null;

    public static void main(String... args) throws Exception {
        new KurumiBot(args);
    }

    public static KurumiBot getInstance() {
        return instance;
    }

    public static void setInstance(KurumiBot bot) {
        if(instance != null) {
            throw new RuntimeException("AnimeBot instance is already initialized!");
        }

        instance = bot;
    }
}
