package org.cocktail_scrapper.threads;

import org.cocktail_scrapper.Logger;
import org.cocktail_scrapper.SaveData;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.extractors.CocktailScraper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

public class ThreadWriter extends Thread {
    BlockingQueue<CocktailData> cocktailDataBlockingQueue;
    String rootPath;
    String cocktailFileName;

    public ThreadWriter(BlockingQueue<CocktailData> cocktailDataBlockingQueue, String rootPath, String cocktailFileName) {
        this.cocktailDataBlockingQueue = cocktailDataBlockingQueue;
        this.rootPath = rootPath.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        this.cocktailFileName = cocktailFileName;
    }

    @Override
    public void run() {
        CocktailData cocktailData;
        BufferedWriter writer = null;
        String recipesFolder = File.separator + "recipes" + File.separator;
        String imgFolder = File.separator + "img" + File.separator;
        try {
            // Создаём файл для записи
            File file = getFile(rootPath + recipesFolder + cocktailFileName);
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), // Используем "true" для дозаписи в файл
                            StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            while ((cocktailData = cocktailDataBlockingQueue.take()) != null ||!ThreadReader.doneStatus) {
                try {
                    // Записываем каждую строку сразу
                    writer.write(cocktailData.toSvgFormat());
                    writer.newLine();
                    // Экспортируем изображение
                    String nameCocktail = cocktailData.name().toLowerCase().replace(" ", "_");
                    SaveData.imgExporter(cocktailData.img(), nameCocktail, "webp", rootPath + imgFolder);
                } catch (IOException e) {
                    Logger.logWriter("Error writing data for cocktail: " + cocktailData.name() + ". Skipping.");
                    Logger.logWriter("Exception %s Problem Url %s",e.getMessage(), CocktailScraper.URL);
                    // Если ошибка при записи, продолжаем с следующей строки
                }
            }
        } catch (InterruptedException e) {
            Logger.logWriter("Exception %s Problem Url %s",e.getMessage(), CocktailScraper.URL);
            Thread.currentThread().interrupt(); // Корректная обработка прерывания потока
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public File getFile(String path) throws IOException {
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

    private void logWriter(String message) {
        // Логирование ошибок или сообщений
        System.err.println(message);
    }
}


