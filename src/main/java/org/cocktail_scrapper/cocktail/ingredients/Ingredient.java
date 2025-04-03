package org.cocktail_scrapper.cocktail.ingredients;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Ingredient(String name, double quantity, Unit unit) {

    public Ingredient(String record) {
        this(parseString(record));
    }
    private Ingredient(Ingredient i){
        this(i.name, i.quantity, i.unit);
    }

    private static Ingredient parseString(String record) {
        // Обновленное регулярное выражение для дробей, десятичных и целых чисел
        String regex = "((?:\\d+[⁄/]\\d+|\\d+(?:\\.\\d+)?|\\d+))\\s+(\\w+)\\s+-\\s+(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(record);

        if (matcher.matches()) {
            String quantityStr = matcher.group(1);
            String unit = matcher.group(2);
            String name = matcher.group(3);

            double quantity;
            // Парсим дроби вида 1⁄2 или 1/2
            if (quantityStr.contains("⁄") || quantityStr.contains("/")) {
                String[] parts = quantityStr.split("[/⁄]");
                double numerator = Double.parseDouble(parts[0]);
                double denominator = Double.parseDouble(parts[1]);
                quantity = numerator / denominator;
            } else {
                quantity = Double.parseDouble(quantityStr);
            }

            // Проверяем, существует ли Unit для граммов
            try {
                Unit unitEnum = Unit.valueOf(unit.toUpperCase());
                return new Ingredient(name, quantity, unitEnum);
            } catch (IllegalArgumentException e) {
                // Обработка неизвестных единиц измерения
                return new Ingredient(name, quantity, Unit.OTHER);
            }
        } else {
            // Логика для случаев вроде "Top up with"
            String[] parts = record.split("-");
            if (parts.length > 1 && parts[0].trim().equalsIgnoreCase("top up with")) {
                return new Ingredient(parts[1].trim(), 0.0, Unit.TOP_UP);
            }
            return new Ingredient(record, 0.0, Unit.UNKNOWN);
        }
    }


    public static void main(String[] args) {
        String s ="30 ml - La Fée Parisienne absinthe";
        Ingredient ingredient = new Ingredient(s);
        System.out.println(ingredient);
    }
}

