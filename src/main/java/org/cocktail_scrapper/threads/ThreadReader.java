package org.cocktail_scrapper.threads;

import org.cocktail_scrapper.Logger;
import org.cocktail_scrapper.SaveData;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.extractors.CocktailScraper;

import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ThreadReader extends Thread {
    BlockingQueue<CocktailData> cocktailDataBlockingQueue;
    int rows;
    List<String> urls;
    public static boolean doneStatus = false;

    public ThreadReader(BlockingQueue<CocktailData> cocktailDataBlockingQueue, int rows, List<String> urls) {
        this.cocktailDataBlockingQueue = cocktailDataBlockingQueue;
        this.rows = rows;
        this.urls = urls;
    }

    @Override
    public void run() {
        try {
            CocktailScraper.extract(rows, urls, cocktailDataBlockingQueue);
        } catch (InterruptedException e) {
            Logger.logWriter(e.getMessage() + " Thread Reader");
        } finally {
            doneStatus = true;
            Thread.currentThread().interrupt();
        }
    }
}
