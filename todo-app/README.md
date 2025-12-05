📝 Todo-app – Backend & Frontend

Detta projekt är en fullstack-applikation byggd med Spring Boot, Spring Security (JWT), PostgreSQL, och en Next.js-frontend.
Syftet är att visa hur autentisering, rollhantering och CRUD-funktionalitet kan implementeras i ett modernt fullstack-projekt.

🚀 Funktionalitet
Backend

Spring Boot REST API

JWT-baserad autentisering

Roller: USER och ADMIN

Auktorisering via @PreAuthorize

CRUD för todos

Kopplat till PostgreSQL

Kan köras i Rabbit Docker (din skolas centrala Docker-miljö)

Frontend

Next.js (React)

Inloggning, registrering och todo-hantering

JWT hanteras via localStorage

Kommunicerar med backend via fetch

🔐 Roller & säkerhet
USER

Har tillgång till sina egna todos (CRUD)

ADMIN

Har utökade rättigheter (t.ex. administrativ åtkomst beroende på implementation)

Admin för testning

När läraren loggar in finns en default-admin:

username: admin
password: admin123


Den skapas automatiskt (endast för test).

/**
* Denna konfiguration skapar en standardadministratör ENDAST för test- och examinationssyfte.
* Syftet är att läraren/examinatorn ska kunna logga in och verifiera behörigheter och roller.
*
* I en riktig produktionsmiljö skulle denna användare inte skapas här.
  */

🐳 Körning i Rabbit Docker

Projektet är anpassat för att kunna köras i skolans Rabbit Docker-miljö.
Du kan ladda upp projektet till Rabbit och starta det via skolans instruktioner.

(Eftersom lokal Docker inte används behövs ingen docker-compose-fil.)

▶️ Starta projektet lokalt (om man vill)
Backend
mvn spring-boot:run

Frontend
npm install
npm run dev

📦 Teknologier

Java 17

Spring Boot

Spring Security + JWT

PostgreSQL

Next.js / React

Rabbit Docker (distribution av backend)