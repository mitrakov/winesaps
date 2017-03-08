package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 07.03.2017
 */

class FileReader implements Model.IFileReader {
    @Override
    public void write(String filename, String s) {
        Gdx.files.local(filename).writeString(s, false);
    }

    @Override
    public String read(String filename) {
        try {
            return Gdx.files.local(filename).readString();
        } catch (Exception e) {
            return null;
        }
    }
}
