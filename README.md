# DB-controller - Minimalist Transactional Database Engine

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)

**DB-controller** - это легковесная транзакционная база данных, реализующая принципы ACID, разработанная в рамках учебного курса "Технологии разработки программного обеспечения".

## 🌟 Ключевые особенности

- **Полная поддержка ACID**: атомарность, согласованность, изоляция, долговечность
- **Транзакции** с поддержкой COMMIT и ROLLBACK
- **Журналирование изменений** в отдельный .log файл
- **Страничная организация данных** (8 КБ на страницу)
- **Консольный интерфейс** с интерактивным вводом SQL-команд
- **Кросс-платформенность**: работает на Windows, Linux, macOS

## 📦 Быстрый старт

### Требования
- Java 17 или новее
- 100 МБ свободного места на диске (для тестовой БД)

### Установка
```bash
git clone https://gitverse.ru/hlebnoe_pole/db-controller.git
cd db-controller
```

### Запуск
```bash
java -jar asudb.jar /path/to/database.db
```

Если файл БД не существует, программа предложит его создать.

## 🛠 Поддерживаемые SQL-команды

| Команда                | Пример                          | Описание                     |
|------------------------|---------------------------------|------------------------------|
| CREATE TABLE           | `CREATE TABLE users (id INT PRIMARY KEY, name TEXT);` | Создание таблицы |
| INSERT                 | `INSERT INTO users VALUES (1, 'Alice');` | Вставка данных |
| SELECT                 | `SELECT * FROM users WHERE id > 10;`     | Выборка данных |
| BEGIN TRANSACTION      | `BEGIN TRANSACTION;`            | Начало транзакции |
| COMMIT                 | `COMMIT;`                       | Фиксация изменений |
| ROLLBACK               | `ROLLBACK;`                     | Откат транзакции |
| SHOW TABLES            | `SHOW TABLES;`                  | Список таблиц |
| DROP TABLE             | `DROP TABLE users;`             | Удаление таблицы |

## 🏗 Архитектура системы

```
AsuDB
├── Storage Engine (Page-based, 8KB)
├── Transaction Manager (ACID)
├── SQL Parser (Lexer → Parser → Executor)
├── WAL (Write-Ahead Logging)
└── CLI Interface
```

## 📝 Пример работы

```sql
> BEGIN TRANSACTION;
> CREATE TABLE products (id INT, name TEXT, price REAL);
Table created
> INSERT INTO products VALUES (1, 'Laptop', 999.99);
Rows inserted: 1
> COMMIT;
Transaction committed
> SELECT * FROM products;
| id | name   | price  |
|----|--------|--------|
| 1  | Laptop | 999.99 |
```
