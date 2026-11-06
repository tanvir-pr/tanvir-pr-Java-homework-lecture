# Java Homework Lecture

Browser-based Spring Boot application (Java 25).

## Run

```bash
java -jar homework.jar
```

Or during development:

```bash
mvn spring-boot:run
```

Open http://localhost:8080

## Secrets

Copy `password.txt.example` to `password.txt` and add your Oanda practice API token.  
`password.txt` is gitignored — never commit secrets.

## Theme

Free responsive HTML/CSS theme inspired by **HTML5 UP Massively** patterns
(CC-style free template approach: https://html5up.net/). Integrated as Thymeleaf layout + `static/css/theme.css`.

## React CRUD

- Source: `react-app/` (Vite + React + Axios build)
- Built assets served from `/react/`
- REST API: `/api/employees`
- H2 file database: `C:/data/homeworkdb`

## Build JAR

```bash
mvn -DskipTests package
copy target\homework.jar homework.jar
java -jar homework.jar
```

## GitHub

https://github.com/tanvir-pr/tanvir-pr-Java-homework-lecture

## Team

- TANVIR HASAN / C0FMUG (`tanvir-pr`)
- Islam md Tariqul / WEP2KE (`tariqul71`)

Company: **Astha Life**
