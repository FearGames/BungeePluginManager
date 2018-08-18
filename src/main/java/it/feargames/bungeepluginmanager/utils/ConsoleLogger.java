package it.feargames.bungeepluginmanager.utils;

import lombok.experimental.UtilityClass;

import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class ConsoleLogger {

    private static Logger logger;
    private static boolean debugEnabled = false;

    public static void setLogger(Logger logger) {
        ConsoleLogger.logger = logger;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        ConsoleLogger.debugEnabled = debugEnabled;
    }

    public static void info(String message) {
        if (logger == null) {
            System.out.println(message);
            return;
        }
        logger.info(message);
    }

    public static void warning(String message) {
        if (logger == null) {
            System.out.println(message);
            return;
        }
        logger.warning(message);
    }

    public static void error(String message) {
        if (logger == null) {
            System.out.println(message);
            return;
        }
        logger.severe(message);
    }

    public static void exception(String message, Throwable throwable) {
        if (logger == null) {
            System.out.println(message);
            throwable.printStackTrace();
            return;
        }
        logger.log(Level.SEVERE, message, throwable);
    }

    public static void debug(String message) {
        if (debugEnabled) {
            info("[Debug] " + message);
        }
    }
}
