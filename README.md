# miscale2pg

Import Mi Body Composition Scale 2 measurements into PostgreSQL

`miscale2pg` is a small Spring Boot service that accepts the export file from the Mi Fit / Zepp Life app (via [user.huami.com](https://user.huami.com/privacy/index.html)) and stores the measurements into a PostgreSQL table. Stored data can then be queried through a simple REST API and visualized with the included Grafana dashboard.

## Getting Started

### Run locally with Docker Compose

```bash
docker compose up -d
```

This starts PostgreSQL and `miscale2pg` (pulled from `ghcr.io/nasenov/miscale2pg:latest`), wired together, exposing the API on http://localhost:8080

## Configuration

| Environment Variable         | Default | Description                         |
| ---------------------------- | ------- | ----------------------------------- |
| `SERVER_PORT`                | 8080    | Server port                         |
| `SPRING_DATASOURCE_URL`      | -       | JDBC URL of the PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | -       | Database username                   |
| `SPRING_DATASOURCE_PASSWORD` | -       | Database password                   |

## REST API

The REST API contract is published as an OpenAPI 3.2 document at [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml), served by the running application at `/openapi.yaml`

## Observability

Actuator endpoints are exposed at:

- `GET /actuator/health` - liveness/readiness
- `GET /actuator/prometheus` - Prometheus-formatted metrics (via Micrometer)

## Grafana Dashboard

A ready-made dashboard is provided at [`dashboards/miscale2pg.json`](./dashboards/miscale2pg.json). It reads data directly from the `GET /api/measurements` endpoint using the [Infinity](https://grafana.com/grafana/plugins/yesoreyeram-infinity-datasource/) data source plugin.

To use it:

1. Install the **Infinity** data source plugin in Grafana and configure a data source pointing at your running `miscale2pg` instance.
2. Import `dashboards/miscale2pg.json` (**Dashboards → New → Import**) and select the Infinity data source when prompted.

The dashboard includes:

- **Current** - latest weight, BMI, body fat %, muscle mass, water %, basal metabolism, visceral fat, and bone mass, color-coded against healthy reference ranges.
- **Average** - the same metrics averaged over the selected time range.
- **Historical** - time series of body composition (fat/muscle/weight) and body fat % over time.
