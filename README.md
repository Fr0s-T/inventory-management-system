# Inventory Management System (JavaFX)

![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=java\&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-brightgreen?logo=oracle)
![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-lightgrey?logo=microsoftsqlserver\&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

A desktop application for managing warehouse products and shipments, built with JavaFX and connected to a SQL Server backend.

---

## 📦 Features

* **User Authentication** – Secure login interface for warehouse personnel.
* **Product Management** –

  * Dynamic product listing by warehouse.
  * Live search by name, item code, and color.
  * Real-time background syncing of inventory.
* **Shipment Handling** –

  * View shipments tied to warehouse location.
  * Dynamic pane switching for seamless UX.
* **Role Management** – Admins, Supervisors, Users with access-based screen rendering.
* **Session-Based Caching** – Reduces redundant DB calls for efficiency.

---

## 🔧 Installation

### Requirements

* Java 17 or higher
* JavaFX SDK 17+
* SQL Server (with proper schema and view setup)
* Maven or manual JAR management

### Setup Instructions

1. Clone the repo:

   ```bash
   git clone https://github.com/yourname/inventory-system-javafx.git
   ```

2. Open in IntelliJ or VS Code (Java)

3. Add JavaFX to VM options:

   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

4. Update DB credentials in `DataBaseConnection.java`

5. Run `Main.java`

---

## 📁 Project Structure

```plaintext
src/
├── Controllers/           # Entry scene logic (e.g., LoginPage, SceneLoader)
├── ViewsControllers/      # Controllers linked to specific views (e.g., Products, Reports)
├── Models/                # Data models (Product, Session, User...)
├── Services/              # Business logic + DB interaction
├── Views/                 # FXML UI layouts (Products.fxml, Reports.fxml...)
├── FXML/                  # Login & frame views (UserFrame.fxml, LoginPage.fxml)
├── Icons/                 # Image assets
├── Main.java              # Entry point
```

---

## 🛠️ Tech Stack

* **JavaFX** – UI and event handling
* **JDBC** – SQL Server DB access
* **FXML** – Declarative UI layout
* **SQL Views** – For clean and efficient queries

---

## 📌 Known Design Principles

* MVC-ish separation (Controllers, Models, Views)
* Lazy loading of views for dynamic switching
* Background product sync via `ScheduledExecutorService`

---

## 📸 Screenshots

> *(You can drag and drop screenshots below when ready)*

* Login Page
* Product Table
* Shipment View
* Role-Based Dashboards

---

## 🤝 Contribution

This project is currently maintained by \[Frost and Ilia]. If you wish to contribute:

1. Fork the repo
2. Create a branch: `feature/your-feature`
3. Submit a pull request with clear description

---

## 🧹 Future Enhancements

* [ ] Admin dashboard for system-wide metrics
* [ ] Import/export CSV for product inventory
* [ ] Role-permission management in GUI
* [ ] Shipment creation + live tracking
* [ ] DAO refactor for better data access handling
* [ ] External SQL query files for modularity

---

## 📜 License

MIT (or your custom license here)
