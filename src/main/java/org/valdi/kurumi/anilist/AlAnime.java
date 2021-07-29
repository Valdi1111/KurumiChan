package org.valdi.kurumi.anilist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AlAnime {
    @Expose @SerializedName("media")
    private Media media;
    @Expose @SerializedName("status")
    private Status status;

    public Media getMedia() {
        return media;
    }

    public Status getStatus() {
        return status;
    }

    public static class Media {
        @Expose @SerializedName("id")
        private int id;

        public int getId() {
            return id;
        }
    }

    public enum Status {
        CURRENT,
        PLANNING,
        COMPLETED,
        DROPPED,
        PAUSED,
        REPEATING;
    }

    @Override
    public String toString() {
        return "AlAnime{" +
                "id=" + media.id +
                ", status=" + status +
                '}';
    }
}
