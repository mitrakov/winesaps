package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Data class representing a single friend of a user
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class FriendItem implements Serializable {
    /** Character */
    public final Model.Character character;
    /** Name */
    public final String name;
    /**
     * Status (online/offline)
     * @since Server API 1.2.0
     */
    public final int status;

    /**
     * Creates a new friend item
     * @param character friend's character
     * @param name friend's name
     * @param status friend's status (online/offline, since Server API 1.2.0)
     */
    public FriendItem(Model.Character character, String name, int status) {
        assert character != null && name != null;
        this.character = character;
        this.name = name;
        this.status = status;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "FriendItem{" + "character=" + character + ", name='" + name + '\'' + ", status=" + status + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendItem that = (FriendItem) o;

        return status == that.status && character == that.character && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = character.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + status;
        return result;
    }
}
