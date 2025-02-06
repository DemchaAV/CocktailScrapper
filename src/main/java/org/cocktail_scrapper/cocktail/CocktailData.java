package org.cocktail_scrapper.cocktail;


import org.cocktail_scrapper.cocktail.ingredients.Ingredient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.print.Print;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @param name        name of cocktail
 * @param ingredients all ingredients
 * @param strength    scale of strength (Boozy - Gentle) 0-10
 * @param taste       scale (Sweet - Dry/sour) 0-10
 * @param instruction How to make
 * @param nutrition   One serving of drink contain calories
 * @param unitsOfAlc  units of alcohol
 * @param alcPercent  % alc/vol
 * @param gramsAlc    grams of pure alcohol
 * @param history     A cocktail History
 */
public record CocktailData(
        String name, List<Ingredient> ingredients, List<String> allergens, String glassWear, String garnish,
        int strength, int taste,
        List<String> instruction,
        String history, int nutrition, double unitsOfAlc, double alcPercent, double gramsAlc, byte[] img) {

    public CocktailData(Builder builder) {
        this(
                builder.name,
                new ArrayList<>(builder.ingredients),
                new ArrayList<>(builder.allergens),
                builder.glassWear,
                builder.garnish,
                builder.strength,
                builder.taste,
                new ArrayList<>(builder.instruction),
                builder.history,
                builder.nutrition,
                builder.unitsOfAlc,
                builder.alcPercent,
                builder.gramsAlc,
                builder.img

        );
    }

    //todo Need delete variable s
    public String print() {
        String s = "";
        return Print.format("[m][bd]Cocktail Name:[reset] [bd]{}[reset]\n"
                            + "[bl]Ingredients:[reset] [bd]{}[reset]\n"
                            + "[bl]Allergens:[reset] [bd]{}[reset]\n"
                            + "[bl]GlassWera:[reset] [bd]{}[reset]\n"
                            + "[bl]Garnish:[reset] [bd]{}[reset]\n"
                            + "[bl]Strength:[reset] [bd]{}[reset]\n"
                            + "[bl]Taste:[reset] [bd]{}[reset]\n"
                            + "[bl]Instruction steps:[reset] [bd]{}[reset]\n"
                            + "[bl]History Drink:[reset] [bd]{}[reset]\n"
                            + "[bl]Nutrition in cl:[reset] [bd]{}[reset]\n"
                            + "[bl]Units of Alcohol in cl:[reset] [bd]{}[reset]\n"
                            + "[bl]Alcohol Percent in cl:[reset] [bd]{}[reset]\n"
                            + "[bl]Grams Alcohol in cl:[reset] [bd]{}[reset]\n"
                , name
                , String.join("\n", ingredients.toString())
                , String.join("\n", allergens.toString())
                , glassWear
                , garnish
                , strength
                , taste
                , String.join("\n", instruction.toString())
                , history
                , nutrition
                , unitsOfAlc
                , alcPercent
                , gramsAlc

        );
    }

    public String toSvgFormat() {
        String foramated = (
                "%s," +     //name
                " %s," +     //ingredients
                " %s," +     //allergens
                "\"%s\"," +     //glassWear
                " \"%s\"," +     //garnish
                " %d," +     //strength
                " %d," +     //taste
                " %s," +     //instruction
                " \"%s\"," +     //history
                " %d," +     //nutrition
                " %.2f," +     //unitsOfAlc
                " %.2f," +     //alcPercent
                " %.2f"       //gramsAlc
        ).formatted(
                this.name,
                this.ingredients,
                this.allergens,
                this.glassWear,
                this.garnish,
                this.strength,
                this.taste,
                this.instruction,
                this.history,
                this.nutrition,
                this.unitsOfAlc,
                this.alcPercent,
                this.gramsAlc
        );
        return foramated;
    }

    @Override
    public String toString() {

        return "Cocktail\n" +
               "name=" + Print.format("[bl]{}[reset]", name
                , String.join("\n", ingredients.toString())
                , String.join("\n", allergens.toString())
                , glassWear
                , strength
                , taste
                , String.join("\n", instruction.toString())
                , history
                , nutrition
                , unitsOfAlc
                , alcPercent
                , gramsAlc
        ) +
               "\ningredients=\n" + (String.join("\n", ingredients.toString())) +
               "\nAlergis=\n" + (String.join("\n", allergens.toString())) +
               "\n glassWear= " + glassWear +
               ",\n strength= " + strength +
               ",\n taste=" + taste +
               ",\n instruction= " + String.join("\n", instruction.toString()) +
               ",\n history=" + history +
               ",\n nutrition=" + nutrition +
               ",\n unitsOfAlc=" + unitsOfAlc +
               ",\n alcPercent=" + alcPercent +
               ",\n gramsAlc=" + gramsAlc;
    }

    public static class Builder {
        private final Document doc;
        private final Document fragment;
        String currentUrl;

        private String name;
        private List<Ingredient> ingredients;
        private List<String> allergens;
        private String glassWear;
        private int strength;
        private int taste;
        private List<String> instruction;
        private String history;
        private int nutrition;
        private double unitsOfAlc;
        private double alcPercent;
        private double gramsAlc;
        private String garnish;
        private byte[] img;


        public Builder(Document doc, Document fragment, String currentUrl) {
            this.doc = doc;
            this.fragment = fragment;
            this.currentUrl = currentUrl;

        }

        public Builder getName() {
            Element headingElement = doc.selectFirst("h1.strip__heading");
            String cocktailName;
            // Проверка, что элемент найден, и извлечение текста
            if (headingElement != null) {
                cocktailName = headingElement.text();
            } else {
                this.name = null;
                return this;
            }
            boolean contains = cocktailName.contains("/ ");
            cocktailName = cocktailName.replace("'", "")
                    .replace("/ ", "(")
                    .replace("(Difford's recipe)", "")
                    .replace("(difford's recipe)", "")
                    .replace("(Difford's Recipe)", "")
                    .replace("(difford's Recipe)", "")
                    .replace("(Diffords recipe)","")
                    .replace("(diffords recipe)","")
                    .replace("(diffords Recipe)","")
                    .replace("(Diffords Recipe)","")
                    .replace("(Difford Recipe)","")
                    .replace("(difford Recipe)","")
                    .replace("(difford recipe)","")
                    .replace("(Difford recipe)","")
                    .replace("  ", " ")
                    .trim();
            cocktailName = contains ? cocktailName + ")" : cocktailName;

            this.name = cocktailName;

            return this;
        }

        public Builder getImage() {
            String imageUrl = "";
            byte[] image = new byte[0];
            // Use a CSS selector to locate the desired image element.
            // In this example, we target the image within the product gallery container.
            Element imgElement = doc.select("div.product-gallery-static img").first();
            String linck = doc.absUrl("<img");

            if (imgElement != null) {
                // Extract the 'src' attribute which contains the image URL.
                imageUrl = imgElement.attr("src");
            } else {

                imgElement = doc.select("div.product-gallery__display img, div.product-gallery__item img").first();

                if (imgElement != null) {
                    // Извлекаем атрибут 'src', который содержит URL изображения
                    imageUrl = imgElement.attr("src");
                } else {
                    logWriter("Element is null, parse was failed");
                }

            }
            if (!imageUrl.isEmpty()) {
                try {
                    URL url = new URL(imageUrl);
                    try (InputStream in = url.openStream()) {
                        image = in.readAllBytes();
                    }
                } catch (MalformedURLException e) {
                    logWriter("Invalid URL: " + imageUrl);
                } catch (IOException e) {
                    logWriter("Failed to download image: " + e.getMessage());
                }
            } else {
                System.out.println(currentUrl);
                logWriter(" url is empty");

            }

            this.img = image;

            return this;

        }

        private void logWriter(String log) {
            BufferedWriter writer = null;
            try {
                // Открываем файл в режиме добавления
                writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File("C:\\Users\\Demch\\OneDrive\\Рабочий стол\\Cockatails\\logImageUrl.txt"), true),
                                StandardCharsets.UTF_8));
                writer.write("%s, %s  %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm  dd.MM.yyyy")), log, currentUrl));
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


        public Builder getIngredients() {
            List<Ingredient> ingredients = new ArrayList<>();
            Elements ingredientRows = fragment.select(".ingredients-table tbody tr");
            for (int i = 0; i < ingredientRows.size() - 1; i++) {
                Ingredient ingredient = null;
                Element row = ingredientRows.get(i);
                String record = row.text();
                String amount = row.select("td:first-child").text();
                String ingredientName = row.select("td:last-child").text();
//                ingredient = new Ingredient(amount + " - " + ingredientName);
                ingredient = new Ingredient(amount + " - " + ingredientName);
                ingredients.add(ingredient);
            }
            this.ingredients = ingredients;
            return this;
        }

        public Builder getAllergens() {
            List<String> allergens = new ArrayList<>();
            Elements allergenElements = fragment.select("ul.no-margin-bottom li");
            if (allergenElements != null && !allergenElements.isEmpty()) {
                for (Element allergen : allergenElements) {
                    // Add each allergen text to the list
                    if (allergen.toString().contains("units of alcohol") ||
                        allergen.toString().contains("alc./vol") ||
                        allergen.toString().contains("grams of pure alcohol")) {
                        break;
                    }
                    allergens.add(allergen.text().trim());
                }
            }
            this.allergens = allergens;

            return this;
        }

        public Builder getGlassWear() {
            String glassWear = null;
            // Correct selector: <h3> with text "Serve in a" followed by <a>
            Element step = doc.selectFirst("h3:contains(Serve in a) + a");
            if (step != null) {
                glassWear = step.text();
            }
            this.glassWear = glassWear;
            return this;
        }

        public Builder getStrength() {
            int strength = 0; // default
            Element strengthElement = fragment.selectFirst(".svg-range img[alt]");
            if (strengthElement != null) {
                strength = Integer.parseInt(strengthElement.attr("alt"));
            }
            this.strength = strength;
            return this;
        }

        public Builder getTaste() {
            int taste = 0; // значение по умолчанию
            // Выбираем все элементы с классом "svg-range" и изображением внутри
            Elements tasteElements = fragment.select(".svg-range img[alt]");
            // Проверяем, есть ли хотя бы два элемента (strength и taste)
            if (tasteElements.size() >= 2) {
                // Берем второй элемент (индекс 1) для taste
                Element tasteElement = tasteElements.get(1);
                taste = Integer.parseInt(tasteElement.attr("alt"));
            }
            this.taste = taste;
            return this;
        }

        public Builder getInstruction() {
            List<String> listSteps = new ArrayList<>();
            Elements steps = fragment.select("ol.no-margin-bottom li");
            if (steps != null && steps.size() > 0) {
                for (Element step : steps) {
                    listSteps.add(step.text());
//                System.out.println(steps);
                }
            } else {
                Element step = doc.selectFirst("h2:contains(How to make:) + p");
                if (steps != null) {
                    listSteps.add(step.text());
                }
            }
            this.instruction = listSteps;
            return this;
        }

        public Builder getHistory() {
            String history = "";
            Element historyElement = doc.selectFirst("h2:contains(History:) + p");
            if (historyElement != null) {
                history = historyElement.text();
            }
            this.history = history;
            return this;
        }

        public Builder getNutrition() {
            int nutrition = 0; // default
            Element nutritionElement = fragment.selectFirst("h3:contains(Nutrition:) + p");
            if (nutritionElement != null) {
                String nutritionText = nutritionElement.text().replaceAll("[^0-9]", "");
                nutrition = Integer.parseInt(nutritionText);
            }
            this.nutrition = nutrition;
            return this;
        }

        public Builder getUnitsOfAlc() {
            double unitsOfAlc = 0.0; // default
            Element unitsElement = fragment.selectFirst("li:contains(units of alcohol)");
            if (unitsElement != null) {
                String unitsText = unitsElement.text().replaceAll("[^0-9.]", "");
                unitsOfAlc = Double.parseDouble(unitsText);
            }
            this.unitsOfAlc = unitsOfAlc;
            return this;
        }

        public Builder getAlcPercent() {
            double alcPercent = 0.0; // default
            Element alcPercentElement = fragment.selectFirst("li:contains(% alc./vol.)");
            if (alcPercentElement != null) {
                String percentText = alcPercentElement.text();

                // Регулярное выражение для поиска процента алкоголя
                String regex = "([\\d.]+)%\\s+alc\\./vol";

                // Компиляция регулярного выражения
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(percentText);

                // Проверяем совпадение
                if (matcher.find()) {
                    // Извлекаем процент алкоголя (первая группа)
                    alcPercent = Double.parseDouble(matcher.group(1));
                }
            }
            this.alcPercent = alcPercent;
            return this;
        }

        public Builder getGramsAlc() {
            double gramsAlc = 0.0; // default
            Element gramsAlcElement = fragment.selectFirst("li:contains(grams of pure alcohol)");
            if (gramsAlcElement != null) {
                String gramsText = gramsAlcElement.text().replaceAll("[^0-9.]", "");
                gramsAlc = Double.parseDouble(gramsText);
            }
            this.gramsAlc = gramsAlc;
            return this;
        }

        public Builder getGarnish() {
            Element garnishElement = fragment.selectFirst("div.cell h3:containsOwn(Garnish) + p");
            this.garnish = (garnishElement != null) ? garnishElement.text() : "";
            return this;

        }

        public CocktailData build() {
            return new CocktailData(this);
        }

    }
}

