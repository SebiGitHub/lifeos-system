# Arquitectura LifeOS (Notion + Make + ntfy + Android)

## Componentes
- **Notion**: fuente de datos (BDs: Days, Habits, Habit Logs, Player, Recovery Quests, XP Ledger, ocio).
- **Make (2 escenarios)**:
  1) Cierre del día
  2) Rachas + bonus (diario)
- **ntfy (ntfy.sh)**: canal de notificaciones push (HTTP).
- **Android (LifeOs Detox)**: registro de tiempo por app/categoría + export CSV.

## Diagrama (alto nivel)
```mermaid
flowchart LR
  U[Usuario] -->|Rellena día + logs| N[Notion]
  M1[Make: Cierre del día] -->|Lee/Escribe| N
  M2[Make: Rachas + bonus] -->|Lee/Escribe| N
  M1 -->|HTTP POST| P[ntfy.sh]
  M2 -->|HTTP POST| P
  P -->|Push| A[Móvil Android]
  D[App LifeOs Detox] -->|Export CSV| U
  D[App LifeOs Detox] --> X[Import/consulta: manual o futura integracion] --> N
````
Flujo de datos (resumen)
1. Usuario crea/edita Days y añade Habit Logs.
2. Make ejecuta escenarios programados:
3. Valida el día y notifica (Cierre del día).
4. Calcula rachas/HP/XP y gestiona recovery (Rachas + bonus).
5. Las notificaciones se publican en ntfy y llegan al móvil.
6. App Detox genera CSV para análisis/registro (manual por ahora).
