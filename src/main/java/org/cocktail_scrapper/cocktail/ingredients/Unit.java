package org.cocktail_scrapper.cocktail.ingredients;

public enum Unit {
    ML("ml", "Millilitres"),
    CL("cl", "Centilitres"),
    OZ("oz", "Ounces"),
    DASH("dash", "Dash"),
    DROP("drop", "Drop"),
    SHOT("shot", "Shot"),
    TBSP("tbsp", "Tablespoon"),
    TSP("tsp", "Teaspoon"),
    CUP("cup", "Cup"),
    L("l", "Litres"),
    WHOLE("whole", "Whole"),
    FRESH("fresh","Fresh"),
    CUBE("cube","Cube"),
    TOP_UP("top up","Top up With"),
    GRAM("gram","Gram"),
    OTHER("other","Other"),
    UNKNOWN("unknown","Unknown");

    private final String abbreviation; // Краткое обозначение единицы
    private final String fullName;    // Полное название единицы

    Unit(String abbreviation, String fullName) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
    }

    // Получение краткого обозначения
    public String getAbbreviation() {
        return abbreviation;
    }

    // Получение полного названия
    public String getFullName() {
        return fullName;
    }

    // Поиск единицы измерения по её аббревиатуре
    public static Unit fromAbbreviation(String abbreviation) {
        for (Unit unit : values()) {
            if (unit.abbreviation.equalsIgnoreCase(abbreviation)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown unit abbreviation: " + abbreviation);
    }
}

