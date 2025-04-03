package org.cocktail_scrapper.restore_data_from_file;

import org.cocktail_scrapper.cocktail.ingredients.Ingredient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class LoadCSV {
    final String PATH;
    File file;
    List<String> objects;

    public LoadCSV(String PATH) {
        this.PATH = PATH;
    }



    public static String[] getObject(String row) {

        String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(?=(?:[^\\[\\]]*\\[[^\\[\\]]*\\])*[^\\[\\]]*$)";
        String[] parts = row.split(regex);
        for (int j = 0; j < parts.length; j++) {
            parts[j] = parts[j].trim();
        }
        return parts;
    }

    public LoadCSV load() throws FileNotFoundException {
        File file = new File(this.PATH);
        if (file.exists()) {
            return this;
        } else {
            System.out.println(PATH + "File not exists");
            new FileNotFoundException();
            return this;
        }
    }

    public LoadCSV getObjects() throws FileNotFoundException {
        List<String> objects = new ArrayList<>();

        Scanner scanner = new Scanner(file);
        if (!scanner.hasNextLine()) {
            return this;
        }
        while (scanner.hasNextLine()) {
            String object = scanner.nextLine();
            objects.add(object);
        }
        this.objects = new ArrayList<>(objects);
        return this;
    }

    public String[] getObject(int i) {
        if (objects == null) {
            new NullPointerException("objects has not been initialize");
        }
        if (i > objects.size() || i < 0) {
            new ArrayIndexOutOfBoundsException("Objects has been initialize " + objects.size());
        }
        String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(?=(?:[^\\[\\]]*\\[[^\\[\\]]*\\])*[^\\[\\]]*$)";
        String[] parts = objects.get(i).split(regex);
        for (int j = 0; j < parts.length; j++) {
            parts[j] = parts[j].trim();
        }
        return parts;
    }

    public static class Builder {
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
    }

}

class test {
    public static void main(String[] args) {
        String cocktail = "Margarita on-the-rocks, [45.0 ml - Patrón Reposado tequila, 22.5 ml - L'Original Combier Triple Sec, 22.5 ml - Lime juice (freshly squeezed), 5.0 ml - Agave syrup, 2.0 drop - Difford's Saline Solution (or ½ pinch salt) (optional), 4.0 drop - Difford's Margarita Bitters (optional)], [],\"Old-fashioned glass\", \"\", 9, 7, [Select and pre-chill an Old-fashioned glass., Optionally, rim glass with salt (moisten outside edge with lime juice and dip into salt)., Prepare garnish of lime wedge., SHAKE all ingredients with ice., STRAIN into ice-filled glass., Garnish with lime wedge.], \"The traditional Margarita recipe is 2 parts tequila, 1 part triple sec and 1 part lime juice. This produces a drink which is a tad on the sour side with the sweetness of the triple sec not quite balancing the sourness of the lime. Hence, depending on the sourness of the limes, I like to add a spoon (5ml) of agave syrup to boost flavour and improve the drink's balance. See: Margarita cocktail history.\", 167, 2.50, 20.89, 19.90\n";
      String [] array =   LoadCSV.getObject(cocktail);
      Arrays.stream(array).forEach(System.out::println);

    }
}
