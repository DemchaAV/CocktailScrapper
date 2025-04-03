package org.cocktail_scrapper.extractors.threads;

import org.cocktail_scrapper.Logger;
import org.cocktail_scrapper.SaveData;
import org.cocktail_scrapper.cocktail.Cocktail;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.cocktail.CocktailSerializer;
import org.cocktail_scrapper.extractors.CocktailScraper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class ThreadWriter extends Thread {
    BlockingQueue<CocktailData> cocktailDataBlockingQueue;
    String rootPath;
    String cocktailFileName;
    ThreadReader threadReader;
    private String stopKeyQueue;

    public ThreadWriter(BlockingQueue<CocktailData> cocktailDataBlockingQueue, String rootPath, String cocktailFileName, ThreadReader threadReader, String stopKeyQueue) {
        this.cocktailDataBlockingQueue = cocktailDataBlockingQueue;
        this.rootPath = rootPath.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        this.cocktailFileName = cocktailFileName;
        this.threadReader = threadReader;
        this.stopKeyQueue = stopKeyQueue;
    }

    @Override
    public void run() {
        CocktailData cocktailData;
        BufferedWriter writer = null;
        boolean isList = false;
        String recipesFolder = File.separator + "recipes" + File.separator;
        String imgFolder = File.separator + "img" + File.separator;

        try {
            isList = isListStarts(rootPath + recipesFolder + cocktailFileName);
            // Создаём файл для записи
            File file = getFile(rootPath + recipesFolder + cocktailFileName);
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), // Используем "true" для дозаписи в файл
                            StandardCharsets.UTF_8));
            if (!isList) {
                writer.write("[");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean startProcessing = false;
        try {
            while ((cocktailData = cocktailDataBlockingQueue.poll(10, TimeUnit.SECONDS)) != null && !cocktailData.name().equals(stopKeyQueue)) {
                if (cocktailData.name().equals(stopKeyQueue)) {
                    break;
                } else if (startProcessing) {
                    try {
                        writer.write(", ");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    startProcessing = true;

                }
                try {
                    // Записываем каждую строку сразу
//                    writer.write(CocktailSerializer.toCsvFormat(cocktailData));
//                    writer.write(CocktailSerializer.toJson(cocktailData));
                    // Экспортируем изображение
                    writeImagesFile(cocktailData, imgFolder);
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
                writer.write("]");
                writer.close();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                Logger.logWriter(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void writeImagesFile(CocktailData cocktailData, String imgFolder) throws IOException {
        String nameCocktail = Cocktail.sanitizeFileName(cocktailData.name());
        byte img[] = cocktailData.img();
        if (img != null || img.length != 0) {
            SaveData.imgExporter(cocktailData.img(), nameCocktail, "webp", rootPath + imgFolder);
        } else {
            Logger.logWriter("No image downloaded img == NUll Name cocktail is %s", cocktailData.name());
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

    private boolean isListStarts(String rootPath) throws IOException {
        Path path = Path.of(rootPath);
        if (!Files.exists(path)) {
            return false;
        }
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(0, content.length() - 1);
            if (!content.equals("[")) {
                content += ", ";
            }
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            return true;
        }
        return false;
    }

}


