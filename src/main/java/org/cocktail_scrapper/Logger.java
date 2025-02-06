package org.cocktail_scrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static String PATH = "C:\\Users\\Demch\\OneDrive\\Рабочий стол\\Cockatails\\logImageUrl.txt";

    public static void logWriter(String patternFormat, Object... args) {
        String timeFrame = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm  dd.MM.yyyy")) + "]  ";
        BufferedWriter writer = null;
        File file;
        try {
            file = getFile(PATH);
        } catch (IOException e) {
            System.err.println("Logger problem with file " + PATH);
            throw new RuntimeException(e);
        }
        try {
            // Открываем файл в режиме добавления
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true),
                            StandardCharsets.UTF_8));
            writer.write(timeFrame + patternFormat.formatted(args));
            writer.newLine();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void logWriter(String message) {
        logWriter("%s", message);

    }

    private static File getFile(String path) throws IOException {
        File file = new File(path);

        // Получаем родительскую директорию
        File parentDir = file.getParentFile();

        // Если родительская директория не существует, создаём её
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // Метод mkdirs() создаёт все необходимые промежуточные директории
        }

        // Если файл не существует, создаём его
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public static void setPATH(String PATH) {
        Logger.PATH = PATH + File.separator + "log.txt";
    }
}
