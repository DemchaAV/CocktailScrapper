package org.cocktail_scrapper.extractors.extraxtTop100;

public record CocktailDataLinksTop100(String cocktailName, String link, String description) {
    @Override
    public String toString() {
        return "Cocktail: " + cocktailName +
               "\nLink: " + link +
               "\nDescription: " + description +
               "\n----";
    }
}
