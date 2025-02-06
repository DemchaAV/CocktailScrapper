package org.cocktail_scrapper.extractors;

import org.cocktail_scrapper.Logger;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.links.XmlParser;
import org.cocktail_scrapper.threads.ThreadReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CocktailScraper {
    public static String URL;
    Document doc;
    Document fragment;

    private CocktailScraper(Document doc, Document fragment, String url) {
        this.doc = doc;
        this.fragment = fragment;
        this.URL = url;
    }

    public static CocktailScraper loader(String ulr, String userAgent, Proxy proxy) {
        String property = ".cell.long-form.long-form--small.long-form--inline-paragraph.pad-bottom";
        Document doc;
        Document fragment = null;
        var connection = Jsoup.connect(ulr);
        if (userAgent != null) {
            connection.userAgent(userAgent);
        }
        if (proxy != null) {
            connection.proxy(proxy);
        }
        try {
            doc = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements elements = doc.select(property);
        if (!elements.isEmpty()) {
            // Создаём новый документ из найденных элементов
            fragment = createNewDocument(elements);

            // Выводим новый документ
        } else {
            System.out.println("No elements found!");
        }
        return new CocktailScraper(doc, fragment, ulr);
    }

    public static CocktailScraper loader(String url) {
        return loader(url, null, null);
    }

    public static CocktailScraper loader(String url, String userAgent) {
        return loader(url, userAgent, null);
    }

    private static Document createNewDocument(Elements elements) {
        // Создаём новый документ и добавляем найденные элементы
        Document newDoc = Document.createShell("");
        for (Element element : elements) {
            newDoc.body().appendChild(element.clone()); // Клонируем, чтобы избежать удаления из исходного документа
        }
        return newDoc;
    }

    public static void saveDocumentToTextFile(Document doc, File file) {

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8))) {

            // Записываем HTML-содержимое документа
            writer.write(doc.html());

            System.out.println("Файл успешно сохранен: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Ошибка при записи файла: " + e.getMessage());
        }
    }

    public static void extract(int rows, List<String> urls, BlockingQueue<CocktailData> blockingQueue) throws InterruptedException {
        String[] userAgents = {
                // Google Chrome (Windows)
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
                // Mozilla Firefox (Windows)
                "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:116.0) Gecko/20100101 Firefox/116.0",
                // Safari (macOS)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Safari/605.1.15",
                // Microsoft Edge (Windows)
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 Edg/115.0.0.0",
                // Opera (Windows)
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 OPR/101.0.0.0"
        };

        if (urls == null) {

            try {
                //load urls from internet
                urls = XmlParser.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (urls == null) {
                Logger.logWriter("Urls = null");
                return;
            }
        }

        int length = (int) Math.floor(Math.log10(rows)) + 1;
        String format = "% " + length + "d%% Current cocktail: %s";
        var currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss  dd-MMM-yyyy"));
        System.out.println("Start time is: " + currentTime);
        approximatelyTime(rows);

        String currentUrl = null;
        try {
            for (int i = 0; i < rows; i++) {
                currentUrl = urls.get(i);
                int numberAgents = (int) (Math.random() * userAgents.length);
                CocktailData cocktail;
                try {
                    cocktail = CocktailScraper.loader(currentUrl, userAgents[numberAgents]).extract();
                } catch (Exception e) {
                    Logger.logWriter("%s was failed on extraction", currentUrl);
                    continue;
                }

                blockingQueue.put(cocktail);
                System.out.print("\r");

                // Calculate progress percentage with floating-point arithmetic, then truncate
                int percentage = (int) ((double) i / rows * 100);
                int sleepTime = 2 + (int) (Math.random() * 3);

                // Print as a neat three-digit value followed by a percent symbol
                System.out.printf(format, percentage, cocktail.name());
                TimeUnit.SECONDS.sleep(sleepTime);
            }
            ThreadReader.doneStatus =true;
            System.out.println("\n" + currentTime + " Complete!!");
        } catch (Exception e) {
            System.err.println("Current link with data error is " + currentUrl);
            throw new RuntimeException(e);
        } finally {
            System.out.println("Finally block -> last link: " + currentUrl);
            ThreadReader.doneStatus =true;
            Thread.currentThread().interrupt();

        }
    }

    public static void extract(int rows) throws InterruptedException {
        extract(rows, null, null);
    }

    static void approximatelyTime(int rows) {
        String minTime = formatTime(2 * rows);
        String maxTime = formatTime(5 * rows);


        System.out.println("Minimum time to finish is: " + minTime);
        System.out.println("Max time to finish is: " + maxTime);
    }

    private static String formatTime(long totalSeconds) {
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;

        // Build a user-friendly time string
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" hours ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minutes ");
        }
        // Display seconds if there are any, or if everything else is zero
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append(" seconds");
        }
        return sb.toString().trim();
    }

    public CocktailData extract() {

        CocktailData cocktail = new CocktailData.Builder(doc, fragment, URL)
                .getName()
                .getIngredients()
                .getAllergens()
                .getGlassWear()
                .getStrength()
                .getTaste()
                .getInstruction()
                .getHistory()
                .getNutrition()
                .getUnitsOfAlc()
                .getAlcPercent()
                .getGramsAlc()
                .getGarnish()
                .getImage()
                .build();
        return cocktail;
    }

}