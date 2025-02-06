package org.cocktail_scrapper.cocktail;

import org.cocktail_scrapper.cocktail.ingredients.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class Cocktail {
    private String name;
    private List<Ingredient> ingredients;
    private List<String> allergens;
    private String glassWear;
    private String garnis;
    private int strength;
    private int taste;
    private List<String> instruction;
    private String history;
    private int nutrition;
    private double unitsOfAlc;
    private double alcPercent;
    private double gramsAlc;
    private byte[] img;


    public Cocktail(CocktailData cocktailData) {
        this.name = cocktailData.name();
        this.ingredients = new ArrayList<>(cocktailData.ingredients());
        this.allergens = new ArrayList<>(cocktailData.allergens());
        this.glassWear = cocktailData.glassWear();
        this.strength = cocktailData.strength();
        this.taste = cocktailData.taste();
        this.instruction = new ArrayList<>(cocktailData.instruction());
        this.history = cocktailData.history();
        this.nutrition = cocktailData.nutrition();
        this.unitsOfAlc = cocktailData.unitsOfAlc();
        this.alcPercent = cocktailData.alcPercent();
        this.gramsAlc = cocktailData.gramsAlc();
    }
}
