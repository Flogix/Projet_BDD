# Usage Scenario for ArtConnect Pro Demo

Here is a step-by-step scenario to demonstrate the new features of the ArtConnect Pro application effectively during your presentation:

## 1. Application Launch & Discover Dashboard
* **Action:** Run the application (`mvn clean javafx:run`).
* **Highlight:** When the app opens, start on the **Discover** tab.
* **Talking Points:** Show how the dashboard automatically pulls the latest "Featured Exhibitions and Upcoming Workshops" directly from the database using the new SQL Views (`vue_details_expositions` and `vue_details_ateliers`). Mention the clean, flat UI design provided by the new `style.css` stylesheet.

## 2. Managing Galleries (CRUD Operations)
* **Action:** Switch to the **Galleries** tab.
* **Highlight:** The sleek TableView displaying all galleries.
* **Talking Points:** Explain that this data is fetched live from the MySQL `galerie` table. 
* **Demo:** 
  1. Click on a gallery in the table to see its details auto-fill in the bottom form.
  2. Change the rating to a very high number (e.g., `8.0`) and click **"Modifier" (Update)**.
  3. Explain that a **MySQL Trigger** (`trg_before_insert_galerie` / `trg_before_update_galerie`) ensures the rating stays between 0 and 5, so it automatically clamps it to 5.0. *(Note: Make sure your trigger handles UPDATE if you defined it that way, or demonstrate an INSERT instead!)*
  4. Create a new gallery to show the immediate UI refresh.

## 3. Advanced MySQL Interactions (Functions & Views)
* **Action:** Switch to the **Workshops (Ateliers)** tab.
* **Highlight:** Select a workshop from the table.
* **Demo:** Click the **"💰 Calculer Revenu Max"** button.
* **Talking Points:** An alert will pop up showing the maximum estimated revenue. Explain that this is not calculated in Java, but rather it triggers a custom **MySQL Function** (`get_max_revenu_atelier`) or a custom query (`prix * places_max`) directly in the database. This demonstrates seamless back-end integration of complex SQL business logic.

## 4. Analytics & Insights
* **Action:** Switch to the **📊 Analytics** tab.
* **Highlight:** The Key Performance Indicators (KPIs) and Ranking tables.
* **Talking Points:** Show how these analytics depend entirely on aggregate queries and custom Views (`vue_statistiques_artistes`, `vue_stats_galeries`) running in real-time on your MySQL server. Emphasize how indexes (like `idx_galerie_nom`) optimize these read-heavy dashboard queries.

## 5. Conclusion
* **Talking Points:** Summarize how the transition from "In-Memory" mock services to fully integrated JDBC DAOs allows for robust, persistent data management, wrapped in an elegant and user-friendly JavaFX interface.
