package ru.mitrakov.self.rush.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.mitrakov.self.rush.FileReader;
import ru.mitrakov.self.rush.model.Model;

/**
 * Logger to log in StdOut and external storage. Please turn off this logger in production to avoid excessive GC!
 * THIS IS A STUB! USE NORMAL LOGGERS IN THE FUTURE
 */
public class SimpleLogger {
    private final static Model.IFileReader fileReader = new FileReader();
    private final static Format sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private final static boolean LOG_TO_FILE = false; // don't forget about WRITE_EXTERNAL_STORAGE permission
    private final static boolean LOG_TO_SOUT = false;

    /**
     * Puts the message given by the object "s" to the log, prepending it with the "prefix"
     * If the logger is turned off, it does nothing (and does not generate garbage for GC)
     * @param prefix - prefix
     * @param s - any object (may be NULL)
     */
    public static synchronized void log(String prefix, Object s) {
        if (LOG_TO_FILE) {
            String w = String.format("%s: %s%s", sdf.format(new Date()), prefix, s);
            fileReader.append("logger.txt", w);
        }
        if (LOG_TO_SOUT) {
            System.out.print(prefix);
            System.out.println(s);
        }
    }
}
