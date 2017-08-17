package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Created by mitrakov on 08.05.2017
 */
@SuppressWarnings("WeakerAccess")
public final class FriendItem implements Serializable {
    public final Model.Character character;
    public final String name;
    public final int status; // Server API 1.2.0+ supports statuses

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
