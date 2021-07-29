package org.valdi.kurumi.myanimelist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MalAnime {
    @Expose @SerializedName("node")
    private Node node;
    @Expose @SerializedName("list_status")
    private ListStatus listStatus;

    public Node getNode() {
        return node;
    }

    public ListStatus getListStatus() {
        return listStatus;
    }

    public static class Node {
        @Expose @SerializedName("id")
        private int id;
        @Expose @SerializedName("title")
        private String title;
        @Expose @SerializedName("main_picture")
        private Picture mainPicture;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Picture getMainPicture() {
            return mainPicture;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", mainPicture=" + mainPicture +
                    '}';
        }
    }

    public static class Picture {
        @Expose @SerializedName("medium")
        private String medium;
        @Expose @SerializedName("large")
        private String large;

        public String getMedium() {
            return medium;
        }

        public String getLarge() {
            return large;
        }
    }

    public static class ListStatus {
        @Expose @SerializedName("status")
        private Status status;
        @Expose @SerializedName("score")
        private int score;
        @Expose @SerializedName("num_watched_episodes")
        private int watchedEpisodes;
        @Expose @SerializedName("is_rewatching")
        private boolean rewatching;
        @Expose @SerializedName("updated_at")
        private String updatedAt;

        public Status getStatus() {
            return status;
        }

        public int getScore() {
            return score;
        }

        public int getWatchedEpisodes() {
            return watchedEpisodes;
        }

        public boolean isRewatching() {
            return rewatching;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public String toString() {
            return "ListStatus{" +
                    "status='" + status + '\'' +
                    ", score=" + score +
                    ", watchedEpisodes=" + watchedEpisodes +
                    ", rewatching=" + rewatching +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }
    }

    public enum Status {
        watching,
        completed,
        on_hold,
        dropped,
        plan_to_watch;
    }

    @Override
    public String toString() {
        return "Anime{" +
                "id=" + node.id +
                ", title='" + node.title + '\'' +
                ", medium_picture=" + node.mainPicture.medium +
                ", large_picture=" + node.mainPicture.large +
                ", status='" + listStatus.status + '\'' +
                ", score=" + listStatus.score +
                ", watched_episodes=" + listStatus.watchedEpisodes +
                ", rewatching=" + listStatus.rewatching +
                ", updated_at='" + listStatus.updatedAt + '\'' +
                '}';
    }

    public String toPrint() {
        return "\n{" +
                "\n   id: " + node.id +
                "\n   title: '" + node.title + '\'' +
                "\n   medium_picture: " + node.mainPicture.medium +
                "\n   large_picture: " + node.mainPicture.large +
                "\n   status: '" + listStatus.status + '\'' +
                "\n   score: " + listStatus.score +
                "\n   watched_episodes: " + listStatus.watchedEpisodes +
                "\n   rewatching: " + listStatus.rewatching +
                "\n   updated_at: '" + listStatus.updatedAt + '\'' +
                "\n}";
    }
}
