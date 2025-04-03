package org.cocktail_scrapper.cocktail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cocktail_scrapper.Logger;

import java.io.*;
import java.util.Base64;
import java.util.List;

public class CocktailSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Сериализация в JSON
    public static String toJson(CocktailData cocktail) throws JsonProcessingException {
        return objectMapper.writeValueAsString(cocktail);
    }
    public static String toJson(List<CocktailData> cocktail) throws JsonProcessingException {

        return objectMapper.writeValueAsString(cocktail);
    }

    public static String toCsvFormat(CocktailData cocktail) {
        StringBuilder foramated = new StringBuilder();
        foramated.append(cocktail.name())
                .append(", ")
                .append(cocktail.ingredients())
                .append(", ")
                .append(cocktail.allergens())
                .append(", ")
                .append(cocktail.glassWear())
                .append(", ")
                .append(cocktail.garnish())
                .append(", ")
                .append(cocktail.strength())
                .append(", ")
                .append(cocktail.taste())
                .append(", ")
                .append(cocktail.instruction())
                .append(", ")
                .append(cocktail.history())
                .append(", ")
                .append(cocktail.nutrition())
                .append(", ")
                .append(cocktail.unitsOfAlc())
                .append(", ")
                .append(cocktail.alcPercent())
                .append(", ")
                .append(cocktail.gramsAlc());

        return foramated.toString();
    }

    // Десериализация из JSON
    public static CocktailData fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, CocktailData.class);
    }
    public static List<CocktailData> fromJsonToList(File file) throws JsonProcessingException {
        List<CocktailData> cocktailList = null;
        try {
           cocktailList = objectMapper.readValue(
                    file,
                    new TypeReference<List<CocktailData>>() {}
            );
        } catch (IOException e) {
            Logger.logWriter(e.getMessage());
            throw new RuntimeException(e);
        }
        return cocktailList;
    }

    // Сериализация в байты (для сохранения в файл или базу данных)
    public static byte[] toBytes(CocktailData cocktail) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(cocktail);
            return bos.toByteArray();
        }
    }

    // Десериализация из байтов
    public static CocktailData fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (CocktailData) ois.readObject();
        }
    }

    // Сериализация в Base64 строку (удобно для передачи по сети)
    public static String toBase64(CocktailData cocktail) throws IOException {
        return Base64.getEncoder().encodeToString(toBytes(cocktail));
    }

    // Десериализация из Base64 строки
    public static CocktailData fromBase64(String base64) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return fromBytes(bytes);
    }
}
