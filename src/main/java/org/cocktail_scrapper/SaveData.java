package org.cocktail_scrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SaveData {

   public static BufferedWriter saveDocumentToTextFile(String row, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file),
                        StandardCharsets.UTF_8));
        writer.write(row);
        writer.newLine();

return writer;
    }

    public static void imgExporter(byte[] imageData, String name, String format, String rootPath) throws IOException {
        // Ensure the rootPath ends with a file separator.
        if (!rootPath.endsWith(File.separator)) {
            rootPath = rootPath + File.separator;
        }
        String fileName = rootPath + name.toLowerCase() + "." + format;
        File file = getFile(fileName);
        imgExporter(imageData, name, format, file);
    }

    public static void imgExporter(byte[] imageData, String name, String format, File file) {

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageData);
        } catch (IOException e) {
            System.out.println("Failed to write image: " + e.getMessage());
        }
    }

    private static File getFile(String path) throws IOException {
        File file = new File(path);

        // Получаем родительскую директорию
        File parentDir = file.getParentFile();

        // Если родительская директория не существует, создаём её
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // Метод mkdirs() создаёт все необходимые промежуточные директории
        }

        // Если файл не существует, создаём его
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }
}
