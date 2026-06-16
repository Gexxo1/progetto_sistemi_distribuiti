# Progetto Sistemi Distribuiti

**Studente:** Giuliano Spata  
**Anno accademico:** 2025/2026

Questo repository contiene il progetto d'esame per il corso di **Sistemi Distribuiti** della Laurea Magistrale in Informatica. L'applicazione e' un backend REST sviluppato in Java 17 con Spring Boot 3.2.0 per consultare e gestire un catalogo di prodotti caricato da file CSV.

Il sistema espone una Remote Facade HTTP sotto `/api/products`: il client non accede direttamente alla logica applicativa o ai dataset, ma interagisce con un insieme di endpoint REST. Le operazioni di lettura sono pubbliche, mentre le operazioni di scrittura sono protette tramite autenticazione JWT e controllo dei ruoli.

## Tecnologie usate

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Security
- JWT con libreria `jjwt`
- Apache Commons CSV
- JUnit e MockMvc per i test automatici
- Maven Wrapper

## Funzionalita' principali

- caricamento in memoria di dataset CSV di prodotti;
- elenco delle categorie disponibili;
- ricerca prodotti tramite parola chiave;
- recupero dei prodotti con rating migliore per categoria;
- recupero del prodotto con prezzo migliore per categoria;
- ricerca di prodotti simili per categoria e nome;
- ricerca di prodotti simili partendo dall'id del prodotto;
- consultazione delle recensioni;
- raggruppamento dei prodotti per fasce di prezzo;
- cronologia delle ricerche nella sessione utente;
- endpoint batch `/api/products/suggestions`, che aggrega rating migliore, prezzo migliore e prodotti simili;
- login con JWT;
- aggiunta recensioni consentita ai revisori;
- modifica dei prezzi consentita agli amministratori.

## Struttura del progetto

Codice applicativo principale:

```text
src/main/java/com/progetto_sistemi_distribuiti
|-- controller   API REST e Remote Facade
|-- service      logica applicativa
|-- repository   caricamento e normalizzazione dei CSV
|-- model        modello interno, DTO e sessione utente
`-- security     login, JWT, filtri e regole di autorizzazione
```

Codice test automatici:

```text
src/test/java/com/progetto_sistemi_distribuiti
```

Le risorse e i dataset CSV si trovano in:

```text
src/main/resources
```

## Utenti di prova

| Username | Password | Ruoli |
| --- | --- | --- |
| `mario` | `password` | `UTENTE` |
| `luigi` | `password` | `REVISORE` |
| `prof` | `password` | `AMMINISTRATORE`, `REVISORE` |

## Avvio del progetto

Da terminale, nella cartella del progetto:

```powershell
.\mvnw.cmd spring-boot:run
```

L'applicazione parte su `http://localhost:8080`.

Per eseguire i test:

```powershell
.\mvnw.cmd test
```

## Esempi di chiamate pubbliche

Le seguenti chiamate possono essere provate direttamente da Thunder Client, Postman o da un qualsiasi client HTTP dopo aver avviato l'applicazione.

```http
GET http://localhost:8080/api/products/suggestions?category=Skincare
GET http://localhost:8080/api/products/categories
GET http://localhost:8080/api/products/search?keyword=MacBook
GET http://localhost:8080/api/products/my-history
GET http://localhost:8080/api/products/top-rated?category=Laptops
GET http://localhost:8080/api/products/best-price?category=Fashion
GET http://localhost:8080/api/products/reviews?id=1
GET http://localhost:8080/api/products/price-ranges
GET http://localhost:8080/api/products/similar?category=Laptops&productName=Apple MacBook Pro
GET http://localhost:8080/api/products/similar?category=Laptops&productName=Apple%20MacBook%20Pro
GET http://localhost:8080/api/products/1/similar
GET http://localhost:8080/api/products/suggestions?category=Laptops&productName=Apple MacBook Pro
GET http://localhost:8080/api/products/suggestions?category=Laptops&productName=Apple%20MacBook%20Pro
```

L'endpoint `/suggestions` rappresenta il Request Batch del progetto: in una singola risposta restituisce prodotti con rating migliore, prodotto con prezzo migliore e prodotti simili. In pratica aggrega queste tre operazioni:

```http
GET http://localhost:8080/api/products/top-rated?category=Laptops
GET http://localhost:8080/api/products/best-price?category=Laptops
GET http://localhost:8080/api/products/similar?category=Laptops&productName=Apple%20MacBook%20Pro
```

Il dettaglio interno di un prodotto restituisce il modello `Product` completo, incluse le recensioni, ma richiede il ruolo `AMMINISTRATORE`:

```http
GET http://localhost:8080/api/products/1
Authorization: Bearer <token>
``r

## Login e chiamate protette

Per ottenere un token JWT:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "luigi",
  "password": "password"
}
```

La risposta contiene un campo `token`. Per le chiamate protette va aggiunto l'header:

```text
Authorization: Bearer <token>
```

Esempi:

```http
POST http://localhost:8080/api/products/1/reviews
Content-Type: text/plain
Authorization: Bearer <token>

Recensione inserita dal revisore
```

```http
PUT http://localhost:8080/api/products/1/price?price=99.99
Authorization: Bearer <token>
```

## Percorso consigliato per la dimostrazione 

Esempio di dimostrazione manuale:
1. Avviare l'applicazione `.\mvnw.cmd spring-boot:run` oppure avviare `ProgettoSistemiDistribuitiApplication.java`
2. Effettuare il login `[POST] /api/auth/login`con `mario` (che ha ruolo `UTENTE`) compilando il JSON & inserire il token (per maggiori dettagli vedere `Esempi di chiamate pubbliche`).
3. Mostrare il Request Batch con `[GET] api/products/similar?category=Laptops&productName=Apple%20MacBook%20Pro`.
4. Mostrare la cronologia di sessione con `[GET] /api/products/my-history` dopo una ricerca.
5. Effettuare il login `luigi` (come fatto in `#2`).
6. Aggiungere una recensione con `[POST] /api/{id}/reviews`.
7. Verificare la recensione con `[GET] /api/products/reviews?id=1`.
8. Effettuare il login con `prof` (come fatto in `#2`).
9. Mostrare `[PUT] /api/products/{id}/price` per provare a modificare prezzi.
10. Controllare con `[GET] /api/products/1` se il prezzo è stato cambiato.

Testing automatico:
1. Eseguire i test automatici con `.\mvnw.cmd test`.

## Documentazione

La relazione tecnica completa e' disponibile in `RELAZIONE.md` e nel PDF `Relazione_Sistemi_Distribuiti.pdf`.
