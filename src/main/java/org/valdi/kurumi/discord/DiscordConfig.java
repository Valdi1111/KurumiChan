package org.valdi.kurumi.discord;

import com.google.gson.annotations.Expose;

public class DiscordConfig {
    @Expose
    private long channel = 0L;
    @Expose
    private long followEmoji = 0L;
    @Expose
    private long unfollowEmoji = 0L;

    public long getChannel() {
        return channel;
    }

    public long getFollowEmoji() {
        return followEmoji;
    }

    public long getUnfollowEmoji() {
        return unfollowEmoji;
    }
}
