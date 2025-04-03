# 🍹 CocktailScrapper — Web Crawler for Cocktail Recipes

**CocktailScrapper** is a powerful Java-based web scraping tool designed to collect cocktail recipes and store them in structured formats. It utilizes Jsoup for parsing HTML, supports multi-threaded crawling, applies anti-bot techniques such as request throttling and user-agent rotation, and saves results in JSON or MongoDB.

---

## 📦 Features

- ✅ Multi-threaded data extraction from cocktail recipe websites
- ✅ Jsoup-based HTML parsing for precise data targeting
- ✅ Saves data in `.json` format (list of `CocktailData` objects)
- ✅ MongoDB integration for scalable storage
- ✅ Image verification for every recipe
- ✅ Fixing or enriching problematic ingredients automatically
- ✅ MySQL support for ingredient fixes (optional)
- ✅ Configurable settings via `config.properties`

---

## 🧪 Technologies Used

- **Java 17**
- **Jsoup** — HTML parser
- **Jackson** — JSON serialization/deserialization
- **MongoDB Java Driver** — for saving cocktails to database
- **MySQL** — ingredient unit fixing (optional)
- **Multi-threading (BlockingQueue)** — to parallelize scraping & writing

---

## 🚀 How It Works

1. Loads list of URLs from a text/log file
2. Uses separate `ThreadReader` and `ThreadWriter` for parallel processing
3. Scrapes recipe data using Jsoup
4. Creates structured `CocktailData` objects with nested `Ingredient` and `Unit`
5. Writes JSON data and optionally inserts into MongoDB or MySQL

---

## 📁 Project Structure Overview

```
org.cocktail_scrapper
├── Main.java                       # Entry point with full pipeline
├── cocktail/
│   ├── Cocktail.java               # Core cocktail model
│   ├── CocktailData.java
│   ├── CocktailSerializer.java     # JSON read/write utilities
│   └── ingredients/
│       ├── Ingredient.java
│       └── Unit.java               # Ingredient units like ml, dash, etc.
├── extractors/threads/
│   ├── ThreadReader.java           # Scrapes and pushes to queue
│   └── ThreadWriter.java           # Pulls from queue and writes to file
├── resource/
│   ├── allCocktails/
│   └── config.properties           # MongoDB/MySQL connection URL
```

---

## 🔄 Data Flow

```
URLs (txt/log) → Jsoup HTML Parser → CocktailData → JSON & DB
                                         ↳ MongoDB
                                         ↳ Image Check
                                         ↳ Ingredient Fix (optional)
```

---

## 🔒 Anti-Ban Strategy

- 🕵️‍♂️ Uses randomized **User-Agent** headers
- ⏱️ Adds sleep between requests
- 🧵 Multi-threaded crawling with bounded queues

---

## 🧪 Example JSON Output

```json
{
  "name": "Old Fashioned",
  "instructions": "Stir with ice, strain into glass",
  "imagePath": "img/OldFashioned.webp",
  "ingredients": [
    {
      "name": "Bourbon",
      "quantity": 50.0,
      "unit": "ml"
    },
    {
      "name": "Sugar cube",
      "quantity": 1.0,
      "unit": "piece"
    }
  ]
}
```

---

## 🗂️ Configuration

File: `config.properties`
```properties
connectionUrl=mongodb+srv://USERNAME:PASSWORD@host.mongodb.net
```
Set environment variables for MySQL:
- `MYSQL_USER`
- `MYSQL_PASS`

---

## 📄 License

MIT License

---

## 🙌 Contributing

Pull requests and improvements are welcome! 🍸

