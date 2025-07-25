# Warehouse Management System 🏭📦

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-24-orange)
![SQLServer](https://img.shields.io/badge/Database-SQL_Server-red)
![License](https://img.shields.io/badge/License-MIT-green)

A comprehensive warehouse management solution with role-based access control and inventory tracking.

## Key Features ✨

### Role-Based Access Control
- **General Manager**: Full system access
- **Regional Manager**: Multi-warehouse oversight
- **Warehouse Manager**: Single location control  
- **Floor/Shift Manager**: Department-level operations
- **Employee**: Basic inventory functions

### Core Modules
- 📊 Real-time inventory tracking
- 👥 Employee performance analytics
- 🚚 Shipment lifecycle management
- 📈 Interactive data dashboards

## Requirements

### Development
- Java 24+
- JavaFX SDK 24.0.2
- SQL Server 2019+

### Runtime
- JRE 24+
- SQL Server connection

## Installation

1. **Database Setup**:
```sql
-- Execute initialization script
USE master;
CREATE DATABASE WarehouseDB;
