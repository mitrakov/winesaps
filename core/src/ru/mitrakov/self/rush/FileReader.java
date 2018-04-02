package ru.mitrakov.self.rush;

import java.io.*;

import com.badlogic.gdx.Gdx;

import ru.mitrakov.self.rush.model.Model;

/**
 * File Reader is used for IO operations on external storage
 * Class is designed to meet the Loose Coupling Principle
 * This class is intended to have a single instance
 * @author mitrakov
 */
public class FileReader implements Model.IFileReader {
    @Override
    public void write(String filename, String s) {
        Gdx.files.local(filename).writeString(s, false);
    }

    @Override
    public void append(String filename, String s) {
        Gdx.files.external(filename).writeString(s + "\n", true);
    }

    @Override
    public String read(String filename) {
        try {
            return Gdx.files.local(filename).readString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public byte[] readAsByteArray(String filename) {
        try {
            return Gdx.files.internal(filename).readBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public Object deserialize(String filename) {
        try { // since API Level 19 may be replaced with try-with-resources
            ObjectInputStream s = new ObjectInputStream(Gdx.files.local(filename).read());
            Object res = s.readObject();
            s.close(); // found by FindBugs
            return res;
        } catch (RuntimeException e) { // recommended to split Catch clause by FindBugs
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void serialize(String filename, Object obj) {
        try { // since API Level 19 may be replaced with try-with-resources
            ObjectOutputStream s = new ObjectOutputStream(Gdx.files.local(filename).write(false));
            s.writeObject(obj);
            s.close(); // found by FindBugs
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
