# Arquitectura LifeOS (Notion + Make + ntfy + Android)

## Componentes
- **Notion**: fuente de datos (BDs: Days, Habits, Habit Logs, Player, Recovery Quests, XP Ledger, ocio).
- **Make (1 escenario)**:
  - Rachas + bonus (diario)
- **ntfy (ntfy.sh)**: canal de notificaciones push (HTTP).
- **Android (LifeOs Detox)**: registro de tiempo por app/categoría + export CSV.

## Diagrama (alto nivel)
```mermaid
flowchart LR
  U[Usuario] -->|Rellena día + logs| N[Notion]
  N -->|Dispara/da pie a ejecución| M[Make: Rachas + bonus]
  M -->|Lee/Escribe: actualiza rachas, XP, HP, estado día| N
  M -->|HTTP POST: notificación| P[ntfy.sh]
  P -->|Push al dispositivo| A[Dispositivo del usuario]
  A -->|Consulta datos| N
  A -->|Usa la app| D[App LifeOs Detox]
  D -->|Exporta CSV: historial de uso rango de tiempo| C[CSV]
  C -->|Se usa en Notion para evaluar desintoxicación digital| N
````
Flujo de datos (resumen)
1. Usuario crea/edita Days y añade Habit Logs.
2. Make ejecuta el escenario programado:
3. Valida el día y notifica.
4. Calcula rachas/HP/XP y gestiona recovery.
5. Las notificaciones se publican en ntfy y llegan al móvil.
6. App Detox genera CSV para análisis/registro (manual por ahora).
