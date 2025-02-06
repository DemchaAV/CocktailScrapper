package org.cocktail_scrapper.extractors.extraxtTop100;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Importer {
    public static List<CocktailDataLinksTop100>importDataLinks(){
        List<CocktailDataLinksTop100> cocktailsList = new ArrayList<>();

        Document doc;

        for (int i = 1; i < 100; i += 20) {
            String url = "https://www.diffordsguide.com/g/1127/worlds-top-100-cocktails/" + i + "-" + (i + 19);
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                System.err.println("Connection faild");
                System.err.println(e.fillInStackTrace());
                throw new RuntimeException(e);
            }
            // Извлечение заголовка (например, h1 с классом no-margin-top)
            Element title = doc.selectFirst("h1.no-margin-top");
//        System.out.println("Title: " + title.text());

            // Извлечение всех коктейлей и их описаний
            Elements cocktails = doc.select("p:has(a)");
            for (Element cocktail : cocktails) {
                // Извлечение названия коктейля
                Element link = cocktail.selectFirst("a strong");
                String cocktailName = link != null ? link.text() : "No name";

                // Извлечение ссылки на рецепт
                String href = cocktail.selectFirst("a").absUrl("href");

                // Извлечение описания коктейля
                String description = cocktail.text();

                // Вывод данных

                if (cocktailName.contains("2024's most popular cocktails")|| cocktailName.equals("No name")){
                    break;
                }
                cocktailsList.add(new CocktailDataLinksTop100(cocktailName, href, description));
            }
        }
        return cocktailsList;
    }
}
