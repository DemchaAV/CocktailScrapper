package org.cocktail_scrapper.links;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {
    final static String URL = "https://www.diffordsguide.com/sitemap/cocktail.xml";
    static String path = "C:\\Users\\Demch\\OneDrive\\Рабочий стол\\Cockatails\\";

    public static List<String> load() throws IOException {
        var doc = Jsoup.connect(URL).get();
        Elements allElements = doc.getAllElements();

        // Находим все элементы <url>
        Elements urls = doc.select("url");

        // Проходим по каждому элементу <url>
        List<String> urlsString = new ArrayList<>();
        for (Element url : urls) {
            // Извлекаем значение <loc>
            String loc = url.select("loc").text();

            if (loc.contains("/cocktails/recipe/")) {
                // Извлекаем дополнительные данные, если нужно
                urlsString.add(loc);
            }

        }
        System.out.println("Urls list has been downloaded successfully!");
        writeListToFile(urlsString, path + "urlLinks.txt");
        System.out.println("===============");
        return urlsString;
    }

    private static void writeListToFile(List<String> urlsString, String path) {
        try (BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)))) {
            for (String url : urlsString) {
                write.write(url);
                write.newLine();
            }
            System.out.printf("Urls list has been wrote %s successfully!\n", path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}