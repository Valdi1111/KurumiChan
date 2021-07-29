package org.valdi.kurumi;

import java.util.Collection;
import java.util.HashMap;

public class AnimeUsers extends HashMap<Long, AnimeUser> {

    public AnimeUsers() {
        super();
    }

    /**
     * Add a user to the list.
     * @param user the user to be added
     * @return the previous value associated with key, or null if there was no mapping for key.
     * (A null return can also indicate that the map previously associated null with key.)
     */
    public AnimeUser add(AnimeUser user) {
        return this.put(user.getUserId(), user);
    }

    /**
     * Add a list of users to the list.
     * @param users collection containing elements to be added to this list
     */
    public void addAll(Collection<AnimeUser> users) {
        users.forEach(this::add);
    }

}
