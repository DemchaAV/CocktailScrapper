package org.cocktail_scrapper.cocktail;

import org.cocktail_scrapper.cocktail.ingredients.Ingredient;
import org.cocktail_scrapper.cocktail.ingredients.Unit;

import java.util.ArrayList;
import java.util.Arrays;
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
        this.garnis = cocktailData.garnish();
        this.strength = cocktailData.strength();
        this.taste = cocktailData.taste();
        this.instruction = new ArrayList<>(cocktailData.instruction());
        this.history = cocktailData.history();
        this.nutrition = cocktailData.nutrition();
        this.unitsOfAlc = cocktailData.unitsOfAlc();
        this.alcPercent = cocktailData.alcPercent();
        this.gramsAlc = cocktailData.gramsAlc();
        this.img = (cocktailData.img() == null) ? null : Arrays.copyOf(cocktailData.img(), cocktailData.img().length);
    }

    public static Cocktail getCocktailOnName(String keyWord, List<CocktailData> cocktails) {
        Cocktail cocktail = null;
        if (keyWord != null) {
            keyWord.toLowerCase()
                    .trim();
        }else {
            return null;
        }
        for (CocktailData cocktailData : cocktails) {
            String name = cocktailData.name().toLowerCase();
            if (name.contains(keyWord.toLowerCase())) {
                cocktail = new Cocktail(cocktailData);
                break;
            }
        }
        return cocktail;
    }

    public static List<Cocktail> getCocktailSearchByName(String keyWord, List<CocktailData> cocktails) {
        List<Cocktail> cocktailsSerach = null;
        if (keyWord != null) {
            keyWord.toLowerCase()
                    .trim();
        }else {
            return null;
        }
        for (CocktailData cocktailData : cocktails) {
            String name = cocktailData.name().toLowerCase();
            if (name.contains(keyWord.toLowerCase())) {
                if (cocktailsSerach == null) {
                    cocktailsSerach = new ArrayList<>();
                    cocktailsSerach.add(new Cocktail(cocktailData));
                } else {

                    cocktailsSerach.add(new Cocktail(cocktailData));
                }
            }
        }
        return cocktailsSerach;
    }

    public static Cocktail getCocktailOfStrength(List<CocktailData> cocktails, int lvl) {
        Cocktail cocktail = null;
        for (CocktailData cocktailData : cocktails) {
            if (cocktailData.strength() == lvl) {
                cocktail = new Cocktail(cocktailData);
                break;
            }
        }
        return cocktail;
    }

    public static String sanitizeFileName(String input) {
        if (input == null || input.isEmpty()) {
            return "default_filename";
        }

        return input.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[<>:\"/\\\\|?*]", "")
                .toLowerCase()
                .trim();
    }

    public static List<Cocktail> getAlcFree(List<CocktailData> cocktails) {
        List<Cocktail> cocktailsAlcFree = null;
        for (CocktailData cocktail : cocktails) {
            if ((cocktail.alcPercent() + cocktail.unitsOfAlc() + cocktail.gramsAlc()) == 0) {
                if (cocktailsAlcFree == null) {
                    cocktailsAlcFree = new ArrayList<>();
                    cocktailsAlcFree.add(new Cocktail(cocktail));
                } else {
                    cocktailsAlcFree.add(new Cocktail(cocktail));
                }
            }
        }
        return cocktailsAlcFree;
    }

    public double getAmount() {
        double amount = 0;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.unit() == Unit.ML) {
                amount += ingredient.quantity();
            }
        }
        return amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setIngredients(Ingredient ingredient) {
        if (this.ingredients != null) {
            this.ingredients.add(ingredient);
        } else {
            this.ingredients = new ArrayList<>();
            this.ingredients.add(ingredient);
        }

    }

    public void setIngredients(int pos, Ingredient ingredient) {
        if (this.ingredients == null) {
            throw new NullPointerException("Ingredients is null");
        } else if (pos < 0 || pos > this.ingredients.size()) {
            throw new ArrayIndexOutOfBoundsException("Invalid position: " + pos);
        } else {
            this.ingredients.set(pos, ingredient);
        }

    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public String getGlassWear() {
        return glassWear;
    }

    public void setGlassWear(String glassWear) {
        this.glassWear = glassWear;
    }

    public String getGarnish() {
        return garnis;
    }

    public void setGarnis(String garnis) {
        this.garnis = garnis;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getTaste() {
        return taste;
    }

    public void setTaste(int taste) {
        this.taste = taste;
    }

    public List<String> getInstruction() {
        return instruction;
    }

    public void setInstruction(List<String> instruction) {
        this.instruction = instruction;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public int getNutrition() {
        return nutrition;
    }

    public void setNutrition(int nutrition) {
        this.nutrition = nutrition;
    }

    public double getUnitsOfAlc() {
        return unitsOfAlc;
    }

    public void setUnitsOfAlc(double unitsOfAlc) {
        this.unitsOfAlc = unitsOfAlc;
    }

    public double getAlcPercent() {
        return alcPercent;
    }

    public void setAlcPercent(double alcPercent) {
        this.alcPercent = alcPercent;
    }

    public double getGramsAlc() {
        return gramsAlc;
    }

    public void setGramsAlc(double gramsAlc) {
        this.gramsAlc = gramsAlc;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "Cocktail{" +
               "name='" + name + '\'' +
               ", ingredients=" + ingredients +
               ", allergens=" + allergens +
               ", glassWear='" + glassWear + '\'' +
               ", garnis='" + garnis + '\'' +
               ", strength=" + strength +
               ", taste=" + taste +
               ", instruction=" + instruction +
               ", history='" + history + '\'' +
               ", nutrition=" + nutrition +
               ", unitsOfAlc=" + unitsOfAlc +
               ", alcPercent=" + alcPercent +
               ", gramsAlc=" + gramsAlc +
               ", img=" + Arrays.toString(img) +
               '}';
    }
}
