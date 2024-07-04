package sahan.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author ksoff
 */
public class CommonLogger {

    public static Logger logger = Logger.getLogger(CommonLogger.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("log/log.log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
