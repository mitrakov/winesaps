package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Created by mitrakov on 08.05.2017
 */
@SuppressWarnings("WeakerAccess")
public final class FriendItem implements Serializable {
    public final Model.Character character;
    public final String name;

    public FriendItem(Model.Character character, String name) {
        assert character != null && name != null;
        this.character = character;
        this.name = name;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "FriendItem{" + "character=" + character + ", name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendItem that = (FriendItem) o;

        return character == that.character && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = character.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
