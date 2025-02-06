package org.cocktail_scrapper.cocktail;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CocktailTestCoctailData {

    @Test
    void testCocktailFieldsMatch() {
        Field[] fieldsCocktailData = CocktailData.class.getDeclaredFields();
        Field[] fieldsCocktailClass = Cocktail.class.getDeclaredFields();
        boolean equals = fieldsCocktailData.length == fieldsCocktailClass.length;


        if (equals) {
            for (Field fieldData : fieldsCocktailData) {
                String fieldDataStr = fieldData.getType() + " " + fieldData.getName();
                boolean contains = false;

                for (Field fieldClass : fieldsCocktailClass) {
                    String fieldClassStr = fieldClass.getType() + " " + fieldClass.getName();
                    if (fieldDataStr.equals(fieldClassStr)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    equals = false;
                }
            }

            for (Field fieldClass : fieldsCocktailClass) {
                String fieldClassStr = fieldClass.getType() + " " + fieldClass.getName();
                boolean contains = false;

                for (Field fieldData : fieldsCocktailData) {
                    String fieldDataStr = fieldData.getType() + " " + fieldData.getName();
                    if (fieldClassStr.equals(fieldDataStr)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    equals = false;
                }
            }
        }

        // Выводим несоответствия в консоль

        assertTrue(equals, "Fields in CocktailData do not match fields in Cocktail");
    }
    @Test
    void testCocktailDataFieldsMatchToCocktailDataBuilder() {
        Field[] fieldsCocktailData = CocktailData.class.getDeclaredFields();
        Field[] fieldsCocktailClass = CocktailData.Builder.class.getDeclaredFields();
        boolean equals = fieldsCocktailData.length == fieldsCocktailClass.length-2;


        if (equals) {
            for (Field fieldData : fieldsCocktailData) {
                String fieldDataStr = fieldData.getType() + " " + fieldData.getName();
                boolean contains = false;

                for (Field fieldClass : fieldsCocktailClass) {
                    String fieldClassStr = fieldClass.getType() + " " + fieldClass.getName();
                    if (fieldDataStr.equals(fieldClassStr)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    equals = false;
                }
            }

            for (Field fieldClass : fieldsCocktailClass) {
                String fieldClassStr = fieldClass.getType() + " " + fieldClass.getName();
                boolean contains = false;

                for (Field fieldData : fieldsCocktailData) {
                    String fieldDataStr = fieldData.getType() + " " + fieldData.getName();
                    if (fieldClassStr.equals(fieldDataStr)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    equals = false;
                }
            }
        }

        // Выводим несоответствия в консоль

        assertTrue(equals, "Fields in CocktailData do not match fields in Cocktail");
    }
}

