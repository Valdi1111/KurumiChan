package org.valdi.kurumi.anilist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AlAnimeList {
    @Expose @SerializedName("status")
    private AlAnime.Status status;
    @Expose @SerializedName("entries")
    private List<AlAnime> entries;

    public AlAnime.Status getStatus() {
        return status;
    }

    public List<AlAnime> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "AlAnimeList{" +
                "status=" + status +
                ", entries=" + entries +
                '}';
    }
}
