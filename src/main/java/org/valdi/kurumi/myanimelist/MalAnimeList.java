package org.valdi.kurumi.myanimelist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MalAnimeList {
    @Expose @SerializedName("data")
    private List<MalAnime> data;
    @Expose @SerializedName("paging")
    private Paging paging;

    public List<MalAnime> getData() {
        return data;
    }

    public Paging getPaging() {
        return paging;
    }

    public static class Paging {
        @Expose @SerializedName("next")
        private String next;

        public String getNext() {
            return next;
        }
    }

    @Override
    public String toString() {
        return "AnimeList{" +
                "data=" + data +
                ", next=" + (paging != null ? paging.next : "") +
                '}';
    }

    public String toPrint() {
        StringBuilder builder = new StringBuilder("# Anime list:");
        data.forEach(d -> {
            builder.append(d.toPrint());
            builder.append(",");
            builder.append("\n");
        });
        builder.append("# Next list: ").append(paging != null ? paging.next : "nothing");
        return builder.toString();
    }
}
