# Movie Offer Service

Backend service for movie booking offers.

## What it does
- Create and list offers
- Evaluate eligible offers for a booking context
- Apply an offer to a booking (with redemption tracking)
- List all applied offers
- Supports internal, user-specific, and third-party offer codes

## Run
```bash
./mvnw spring-boot:run
```

## Test
```bash
./mvnw test
```

## API
Base path: `/api/v1/offers`

- `POST /api/v1/offers`
- `GET /api/v1/offers`
- `POST /api/v1/offers/evaluate`
- `POST /api/v1/offers/apply`
- `GET /api/v1/offers/applied`

## API Docs
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## Database
- H2 file DB (local): `./data/movieoffers-db`
- Startup seed data: `src/main/resources/data.sql`
