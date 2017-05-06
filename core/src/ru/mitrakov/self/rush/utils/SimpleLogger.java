package ru.mitrakov.self.rush.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.mitrakov.self.rush.FileReader;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 01.05.2017
 *
 * THIS IS A STUB! USE NORMAL LOGGERS IN THE FUTURE
 */
public class SimpleLogger {
    private final static Model.IFileReader fileReader = new FileReader();
    private final static Format sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    public static synchronized void log(String s) {
        String w = String.format("%s: %s", sdf.format(new Date()), s);
        System.out.println(w);
        fileReader.append("logger.txt", w);
    }
}
