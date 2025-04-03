package org.cocktail_scrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.cocktail_scrapper.cocktail.Cocktail;
import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.cocktail.CocktailSerializer;
import org.cocktail_scrapper.cocktail.ingredients.Ingredient;
import org.cocktail_scrapper.cocktail.ingredients.Unit;
import org.cocktail_scrapper.extractors.threads.ThreadReader;
import org.cocktail_scrapper.extractors.threads.ThreadWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mongodb.client.model.Filters.regex;
import static javax.management.Query.eq;

public class Main {
    static String formattedURL = null;
    static Integer counter = 0;

    public static void main(String[] args) throws JsonProcessingException {
        List<String> urls;
        //initialize urls from file

        //Loaded cocktails from urls
        BlockingQueue<CocktailData> cocktailDataBlockingQueue = new LinkedBlockingQueue<>(10);

        List<CocktailData> cocktailDataList = null;
        List<CocktailData> fixed = null;


        String rootPath = "C:\\Users\\Demch\\OneDrive\\BarMixer\\resource\\allCocktails";
        String cocktailFilename = "cocktails.json";

        // Deserialize
        var start = System.currentTimeMillis();
        cocktailDataList = CocktailSerializer.fromJsonToList(new File(rootPath + "\\recipes\\" + cocktailFilename));

        var noImage = checkerImage(cocktailDataList, rootPath + "\\img\\");
        if (noImage != null && noImage.size() > 0) {
            noImage.forEach((cocktail -> System.out.println(cocktail.name())));
        } else {
            System.out.println("All Cocktails has images!");
        }

        Properties properties = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            // Load properties from the config.properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String connectionUrl = properties.getProperty("connectionUrl");
        try (MongoClient mongoClient = MongoClients.create(connectionUrl)) {
            MongoDatabase database = mongoClient.getDatabase("BarMixerDB");
            MongoCollection<Document> collection = database.getCollection("cocktails");
            List<Document> documents = new ArrayList<>();
            for (CocktailData cocktailData : cocktailDataList) {
                String json = CocktailSerializer.toJson(cocktailData);
                documents.add(Document.parse(json));

            }
            collection.insertMany(documents);
            Bson query = regex("name", "100 year old cigar", "i");
            var doc = collection.find(query).first();
            System.out.println(doc);

            // Creates instructions to project two document fields


        }

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

    public static List<String> getUrlsFromLog(String path) {
        List<String> urls = new ArrayList<>();
        File file = new File(path);
        Scanner scanner;
        String keyWord = "Problem Url";
        if (file.exists()) {
            try {
                scanner = new Scanner(file);
                String currentRow = null;
                String url;
                while (scanner.hasNextLine()) {
                    currentRow = scanner.nextLine();
                    if (currentRow.contains(keyWord)) {
                        url = currentRow.substring(currentRow.indexOf(keyWord) + keyWord.length()).trim();
                        urls.add(url);
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("File doesn't exists " + path);
        }
        return urls;
    }

    /**
     * Current method checks are all cocktails has an image in the rootPath
     *
     * @param cocktailDataList
     * @param rootPath
     * @return
     */
    public static List<CocktailData> checkerImage(List<CocktailData> cocktailDataList, String rootPath) {
        List<CocktailData> cocktailsNoImage = null;
        String name;
        File imgFile;
        String path;
        for (CocktailData cocktailData : cocktailDataList) {
            name = Cocktail.sanitizeFileName(cocktailData.name());
            path = rootPath + name + ".webp";
            imgFile = new File(path);
            if (!imgFile.exists()) {
                if (cocktailsNoImage != null) {
                    cocktailsNoImage.add(cocktailData);
                } else {
                    cocktailsNoImage = new ArrayList<>();
                    cocktailsNoImage.add(cocktailData);
                }
            }
        }
        return cocktailsNoImage;
    }

    /**
     * @param cocktailDataBlockingQueue - BlockingQueue for passing data between threads
     * @param rows                      - how many row has to be read. Has to be no more the size of the list
     * @param urls                      - List urls of cocktail row by row
     * @param rootPath                  - Path to save the data
     * @param cocktailFileName          - Name of the file to save the data
     * @param stopKeyQueue              -this word will be put in to the object as a name of cocktail to notify the process has been finished
     */


    private static void runMultiThreads(BlockingQueue<CocktailData> cocktailDataBlockingQueue, int rows, List<String> urls, String rootPath, String cocktailFileName, String stopKeyQueue) {
        Logger.setPATH(rootPath);
        ThreadReader threadReader = new ThreadReader(cocktailDataBlockingQueue, rows, urls, stopKeyQueue);
        threadReader.setName("ThreadReader");
        Thread threadWriter = new ThreadWriter(cocktailDataBlockingQueue, rootPath, cocktailFileName, threadReader, stopKeyQueue);
        threadWriter.setName("Writer Thread");


        threadReader.start();
        threadWriter.start();
    }

    //TODO Current method was created with a purpose to change some problem unfinished data. Can be removed
    private static CocktailData fixCocktailIngredients(CocktailData checkingCocktail) {
        Cocktail fixedCocktail = new Cocktail(checkingCocktail);
        Ingredient currentIngredients;
        for (int i = 0; i < fixedCocktail.getIngredients().size(); i++) {
            currentIngredients = fixedCocktail.getIngredients().get(i);
            Ingredient newIngredient;
            if (currentIngredients.unit() == Unit.UNKNOWN) {
                System.out.println("++");
                newIngredient = fixIngredient(currentIngredients);
                if (newIngredient != null) {
                    fixedCocktail.getIngredients().set(i, newIngredient);
                } else {
                    //Manual method with Scanner input
                    System.out.println(fixedCocktail);
                    System.out.println(currentIngredients);
                    System.out.println("Enter Name of ingredient ");
                    String name;
                    String unitAbbreviate;
                    double amount;
                    name = new Scanner(System.in).nextLine();
                    System.out.println("Enter Name of Uit ");
                    unitAbbreviate = new Scanner(System.in).next();
                    System.out.println("Enter Name of amount ");
                    amount = new Scanner(System.in).nextDouble();
                    System.out.println(name + " " + unitAbbreviate + " " + amount);
                    name = (name == null || name.isEmpty()) ? currentIngredients.name() : name;
                    amount = (amount == 0.0) ? currentIngredients.quantity() : amount;
                    unitAbbreviate = (unitAbbreviate == null || unitAbbreviate.isEmpty()) ? currentIngredients.unit().getAbbreviation() : unitAbbreviate;
                    newIngredient = new Ingredient(name, amount, Unit.fromAbbreviation(unitAbbreviate));
                    fixedCocktail.getIngredients().set(i, newIngredient);
                }
                fixedCocktail.getIngredients().set(i, newIngredient);
            }
        }
        return new CocktailData(fixedCocktail);
    }

    //TODO This method is a par of the method who do the fixing for a data. Can be removed
    public static Ingredient fixIngredient(Ingredient currentIngredient) {
        Ingredient newIngredient = null;

        if (currentIngredient.name().toLowerCase().contains("Single cream/half-and-half".toLowerCase())) {

            newIngredient = new Ingredient("Single cream/half-and-half", 0.0, Unit.FLOAT);
            return newIngredient;
        } else if (currentIngredient.name().toLowerCase().contains("Franklin & Sons 1886 Soda Water".toLowerCase())) {
            newIngredient = new Ingredient("Franklin & Sons 1886 Soda Water", 1.0, Unit.SPLASH);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 1⁄2 inch - Cucumber".toLowerCase())) {
            newIngredient = new Ingredient("Cucumber (fresh)", 1.5, Unit.INCH);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("Float - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("Float - ", "");
            newIngredient = new Ingredient(newWord.trim(), 1.0, Unit.FLOAT);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 1⁄2 barspoon - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1 1⁄2 barspoon - ", "");
            newIngredient = new Ingredient(newWord.trim(), 1.5, Unit.BARSPOON);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("2⁄3 fill glass with - Stout beer".toLowerCase())) {
            String newWord = currentIngredient.name();
            newIngredient = new Ingredient(newWord.trim(), 0.0, Unit.GLASS);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 1⁄2 fresh - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1 1⁄2 fresh - ", "");
            newIngredient = new Ingredient(newWord.trim(), 1.5, Unit.WHOLE);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1⁄2 fill glass with - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1⁄2 fill glass with - ", "");
            newIngredient = new Ingredient(newWord.trim(), 0.5, Unit.GLASS);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("4 1⁄2 fresh - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("4 1⁄2 fresh - ", "");
            newIngredient = new Ingredient(newWord.trim(), 4.5, Unit.WHOLE);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 grated zest of - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1 grated zest of - ", "");
            newIngredient = new Ingredient(newWord.trim(), 1.0, Unit.GRATED_ZEST);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 1⁄2 inch - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1 1⁄2 inch - ", "");
            newIngredient = new Ingredient(newWord.trim(), 1.5, Unit.INCH);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("2⁄3 fill glass with - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("2⁄3 fill glass with - ", "");
            newIngredient = new Ingredient(newWord.trim(), 0.6, Unit.GLASS);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("2 1⁄2 dash - ".toLowerCase())) {
            String newWord = currentIngredient.name().replace("2 1⁄2 dash - ", "");
            newIngredient = new Ingredient(newWord.trim(), 2.5, Unit.DASH);
            return newIngredient;

        } else if (currentIngredient.name().toLowerCase().contains("1 1⁄4 litre".toLowerCase())) {
            String newWord = currentIngredient.name().replace("1 1⁄4 litre", "");
            newIngredient = new Ingredient(newWord.trim(), 1.15, Unit.L);
            return newIngredient;

        }
        return newIngredient;
    }

}
