package org.cocktail_scrapper.database;

import org.cocktail_scrapper.cocktail.CocktailData;
import org.cocktail_scrapper.cocktail.ingredients.Ingredient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class CocktailDAO {
    private final Connection connection;

    public CocktailDAO(Connection connection) {
        this.connection = connection;
    }

    public void insertCocktailData(CocktailData cocktail) throws SQLException {
        // Отключаем автокоммит для обеспечения атомарности операции
        connection.setAutoCommit(false);

        try {
            // Вставляем основные данные коктейля
            int cocktailId = insertMainCocktailData(cocktail);

            // Вставляем ингредиенты
            for (Ingredient ingredient : cocktail.ingredients()) {
                insertIngredient(cocktailId, ingredient);
            }

            // Вставляем аллергены
            for (String allergen : cocktail.allergens()) {
                insertAllergen(cocktailId, allergen);
            }

            // Вставляем гарнир
            if (cocktail.garnish() != null) {
                insertGarnish(cocktailId, cocktail.garnish());
            }

            // Вставляем инструкции
            for (int i = 0; i < cocktail.instruction().size(); i++) {
                insertInstruction(cocktailId, i + 1, cocktail.instruction().get(i));
            }

            // Если все операции успешны, коммитим транзакцию
            connection.commit();

        } catch (SQLException e) {
            // В случае ошибки откатываем все изменения
            connection.rollback();
            throw e;
        } finally {
            // Восстанавливаем автокоммит
            connection.setAutoCommit(true);
        }
    }

    private int insertMainCocktailData(CocktailData cocktail) throws SQLException {
        String sql = """
            INSERT INTO cocktail_db.cocktails (name, glasswear, strength, taste, history, 
                                 nutrition, units_of_alc, alc_percent, grams_alc, img)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cocktail.name());
            stmt.setString(2, cocktail.glassWear());
            stmt.setInt(3, cocktail.strength());
            stmt.setInt(4, cocktail.taste());
            stmt.setString(5, cocktail.history());
            stmt.setInt(6, cocktail.nutrition());
            stmt.setDouble(7, cocktail.unitsOfAlc());
            stmt.setDouble(8, cocktail.alcPercent());
            stmt.setDouble(9, cocktail.gramsAlc());
            stmt.setBytes(10, cocktail.img());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating cocktail failed, no ID obtained.");
                }
            }
        }
    }

    private void insertIngredient(int cocktailId, Ingredient ingredient) throws SQLException {
        // Сначала вставляем или получаем ID ингредиента
        String insertIngredientSql = "INSERT IGNORE INTO cocktail_db.ingredients (name) VALUES (?)";
        String getIngredientIdSql = "SELECT id FROM cocktail_db.ingredients WHERE name = ?";

        int ingredientId;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertIngredientSql)) {
            insertStmt.setString(1, ingredient.name());
            insertStmt.execute();
        }

        try (PreparedStatement getIdStmt = connection.prepareStatement(getIngredientIdSql)) {
            getIdStmt.setString(1, ingredient.name());
            try (ResultSet rs = getIdStmt.executeQuery()) {
                if (rs.next()) {
                    ingredientId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to get ingredient ID");
                }
            }
        }

        // Связываем ингредиент с коктейлем
        String linkSql = "INSERT IGNORE INTO cocktail_db.cocktail_ingredients (cocktail_id, ingredient_id) VALUES (?, ?)";
        try (PreparedStatement linkStmt = connection.prepareStatement(linkSql)) {
            linkStmt.setInt(1, cocktailId);
            linkStmt.setInt(2, ingredientId);
            linkStmt.execute();
        }
    }

    private void insertAllergen(int cocktailId, String allergenName) throws SQLException {
        String insertAllergenSql = "INSERT IGNORE INTO cocktail_db.allergens (name) VALUES (?)";
        String getAllergenIdSql = "SELECT id FROM cocktail_db.allergens WHERE name = ?";

        int allergenId;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertAllergenSql)) {
            insertStmt.setString(1, allergenName);
            insertStmt.execute();
        }

        try (PreparedStatement getIdStmt = connection.prepareStatement(getAllergenIdSql)) {
            getIdStmt.setString(1, allergenName);
            try (ResultSet rs = getIdStmt.executeQuery()) {
                if (rs.next()) {
                    allergenId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to get allergen ID");
                }
            }
        }

        String linkSql = "INSERT IGNORE INTO cocktail_db.cocktail_allergens (cocktail_id, allergen_id) VALUES (?, ?)";
        try (PreparedStatement linkStmt = connection.prepareStatement(linkSql)) {
            linkStmt.setInt(1, cocktailId);
            linkStmt.setInt(2, allergenId);
            linkStmt.execute();
        }
    }

    private void insertGarnish(int cocktailId, String garnishName) throws SQLException {
        String insertGarnishSql = "INSERT IGNORE INTO garnishes (name) VALUES (?)";
        String getGarnishIdSql = "SELECT id FROM garnishes WHERE name = ?";

        int garnishId;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertGarnishSql)) {
            insertStmt.setString(1, garnishName);
            insertStmt.execute();
        }

        try (PreparedStatement getIdStmt = connection.prepareStatement(getGarnishIdSql)) {
            getIdStmt.setString(1, garnishName);
            try (ResultSet rs = getIdStmt.executeQuery()) {
                if (rs.next()) {
                    garnishId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to get garnish ID");
                }
            }
        }

        String linkSql = "INSERT IGNORE INTO cocktail_garnishes (cocktail_id, garnish_id) VALUES (?, ?)";
        try (PreparedStatement linkStmt = connection.prepareStatement(linkSql)) {
            linkStmt.setInt(1, cocktailId);
            linkStmt.setInt(2, garnishId);
            linkStmt.execute();
        }
    }

    private void insertInstruction(int cocktailId, int stepNumber, String description) throws SQLException {
        String sql = "INSERT INTO instructions (cocktail_id, step_number, description) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cocktailId);
            stmt.setInt(2, stepNumber);
            stmt.setString(3, description);
            stmt.execute();
        }
    }
    public void updateImageInDatabase(int cocktailId, String imagePath) throws SQLException, IOException {
        String sql = "UPDATE cocktails SET img = ? WHERE id = ?";

        // Читаем файл в массив байтов
        byte[] imgData = Files.readAllBytes(Paths.get(imagePath));

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBytes(1, imgData);
            stmt.setInt(2, cocktailId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Cocktail not found with ID: " + cocktailId);
            }
        }
    }
}

