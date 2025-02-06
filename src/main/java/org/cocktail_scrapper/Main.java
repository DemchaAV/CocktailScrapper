package org.cocktail_scrapper;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.extractors.CocktailScraper;
import org.cocktail_scrapper.links.XmlParser;
import org.cocktail_scrapper.threads.ThreadReader;
import org.cocktail_scrapper.threads.ThreadWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    static final String URL = "https://www.diffordsguide.com/cocktails/recipe/3282/";
    static String formattedURL = null;
    static String likesPath = "C:\\Users\\Demch\\OneDrive\\BarMixer\\resource\\urls\\urls_cocktails_top_100.txt";
    static List<CocktailData> cocktails = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        List<String> urls;
        try {
            urls = Files.readAllLines(Path.of(likesPath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BlockingQueue<CocktailData> cocktailDataBlockingQueue = new LinkedBlockingQueue<>(10);

        int rows = urls.size();

        String rootPath = "C:\\Users\\Demch\\OneDrive\\BarMixer\\resource";
        String cocktailFilename = "cocktails_top_100.csv";

//        String testUrl = "https://www.diffordsguide.com/cocktails/recipe/2908/aperol-spritz-aperitivo-spritz";
//        var cocktail = CocktailScraper.loader(testUrl).extract();
//        System.out.println(cocktail);
//        String cocktailNameTest = "Aperol Spritz / Aperitivo Spritz";
//        System.out.println(cocktailNameTest.replace("/", ""));

        runMultiThreads(cocktailDataBlockingQueue, 1, List.of("https://www.diffordsguide.com/cocktails/recipe/1797/appletini-sour-apple-martini"), rootPath, cocktailFilename);

    }

    private static void runMultiThreads(BlockingQueue<CocktailData> cocktailDataBlockingQueue, int rows, List<String> urls, String rootPath, String cocktailFileName) {
        Logger.setPATH(rootPath);
        Thread threadReader = new ThreadReader(cocktailDataBlockingQueue, rows, urls);
        threadReader.setName("ThreadReader");
        Thread threadWriter = new ThreadWriter(cocktailDataBlockingQueue, rootPath, cocktailFileName);
        threadWriter.setName("Writer Thread");


        threadReader.start();
        threadWriter.start();
    }

    private static void extract(int rows, List<String> urls, BlockingQueue<CocktailData> blockingQueue) throws InterruptedException {
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
                System.out.println("Urls = null");
                return;
            }
        }

        int length = (int) Math.floor(Math.log10(rows)) + 1;
        String format = "% " + length + "d%% Current cocktail: %s";

        System.out.println("Start time is: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss  dd-MMM-yyyy")));
        approximatelyTime(rows);

        try {
            for (int i = 1; i <= rows; i++) {
                CocktailScraper extractor = null;
                int numberAgents = (int) (Math.random() * userAgents.length);

                try {
                    formattedURL = urls.get(i);
                    extractor = CocktailScraper.loader(formattedURL, userAgents[numberAgents]);
                } catch (Exception e) {
                    continue;
                }

                CocktailData cocktail = extractor.extract();
//                cocktails.add(cocktail);
                blockingQueue.put(cocktail);
                System.out.print("\r");

                // Calculate progress percentage with floating-point arithmetic, then truncate
                int percentage = (int) ((double) i / rows * 100);
                int sleepTime = 2 + (int) (Math.random() * 3);

                // Print as a neat three-digit value followed by a percent symbol
                System.out.printf(format, percentage, cocktail.name());
                TimeUnit.SECONDS.sleep(sleepTime);
            }
            System.out.println("\nComplete!!");
        } catch (Exception e) {
            System.err.println("Current link with data error is " + formattedURL);
            throw new RuntimeException(e);
        } finally {
            System.out.println("Finally block -> last link: " + formattedURL);
            blockingQueue.put(null);

        }
    }

    private static void extract(int rows) throws InterruptedException {
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

    private MysqlDataSource getConnection() {
        var dataSource = new MysqlDataSource();

        dataSource.setServerName("localhost");
        dataSource.setPort(3306);
        dataSource.setUser(System.getenv("MYSQL_USER"));
        dataSource.setPassword(System.getenv("MYSQL_PASS"));
        dataSource.setDatabaseName("cocktail_db");
        return dataSource;
    }
}