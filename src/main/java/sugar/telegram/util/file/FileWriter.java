package sugar.telegram.util.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar.sugar.dateTimeFormater.DateTimeFormat;
import sugar.telegram.loger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Slf4j
public class FileWriter {
    private final boolean append = true;
    public void fileWriter(Logger logger, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        long fileSize = file.length();
        long maxSize = 262144000L;

        if (fileSize >= maxSize) {
            try {
                Files.write(file.toPath(), new byte[0]);
                log.info("Файл logger.txt - очищен");

            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        try (BufferedWriter writer = new BufferedWriter((new java.io.FileWriter(file, append)))) {

            writer.write("{ChatId=" + logger.getChatId() + ";\n" +
                    "Message=" + logger.getMessage() + ";\n" + "Time=" +
                    DateTimeFormat.dateTimeToString(logger.getDateTime()) + "};\n\n");

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
