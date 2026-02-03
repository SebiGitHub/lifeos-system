# LifeOS System (Notion Life OS)

Sistema personal “LifeOS” para organizar hábitos, gamificación (XP/HP/rachas), registro diario, ocio y detox digital.

## Tecnologías
- **Notion (free plan):** base de datos (Days, Habits, Habit Logs, Player, Recovery Quests, XP Ledger, ocio…).
- **Make (free plan):** automatizaciones (máx. 2 escenarios activos).
- **ntfy (ntfy.sh):** notificaciones push vía HTTP.
- **Android (LifeOs Detox):** app para registrar tiempo por app/categoría y exportar CSV.
- **Windows + Android** (uso diario).

## Objetivo
- Tener un “sistema operativo” personal sencillo:
  - Registrar el día (Daily Log).
  - Registrar hábitos (con logs por día).
  - Calcular rachas y gamificación (XP/HP).
  - Si se rompe la racha y HP llega a 0, activar “Recovery Quest”.
  - Llevar trackers de ocio y un módulo de detox digital.

## Cómo se usa (flujo diario)
1. **Abrir Notion → Days → página de hoy.**
2. Rellenar lo mínimo:
   - Fecha (hoy)
   - `Dato importante`
   - Si el día es malo: `¿Por qué no?`
4. Añadir tus **Habit Logs** del día (si existe log, cuenta como hecho).
5. A las **23:30** (aprox.) el escenario **Cierre del día** verifica que el día existe y está completo/incompleto y avisa por ntfy.
6. A las **23:45** (aprox.) el escenario **Rachas + bonus**:
   - Comprueba hábitos “Cuenta racha”
   - Suma/resta HP
   - Incrementa/resetea racha de días completados
   - Calcula bonus XP por tramos
   - Si HP llega a 0 crea Recovery Quest y bloquea la racha hasta completarla

> Nota: Make free solo permite 2 escenarios ON, por eso el sistema se concentra en estos dos.

## Qué hacen los 2 escenarios de Make
### 1) Cierre del día
- Busca el Day de hoy por `DayKey` + `Player`.
- Si existe:
  - Si están rellenos los campos mínimos → marca “Completado” y notifica.
  - Si faltan campos → marca “Incompleto” y notifica.
- Si no existe:
  - Crea el Day de hoy como “Incompleto” y notifica.

### 2) Rachas + bonus (diario)
- Busca el Player.
- Si `HP <= 0`:
  - Comprueba si hay Recovery Quest completada (y la procesa) o avisa de que sigue en recovery.
- Si `HP > 0`:
  - Busca hábitos “Cuenta racha”.
  - Comprueba si hay logs hoy para esos hábitos.
  - Si cumple todos → racha +1, bonus XP, HP +1 por hábito de racha (capado a HP Max).
  - Si falla alguno → racha a 0, HP -5 por hábito de racha faltante; si HP <= 0 crea Recovery Quest.
- Anti-duplicado:
  - Marca en `Days` un flag tipo “Rachas procesadas” para evitar doble ejecución.

## Documentación
- `docs/arquitectura.md`
- `docs/notion-modelo.md`
- `docs/make-escenarios.md`
- `docs/ntfy.md`
- `docs/app-detox.md`
- `docs/troubleshooting.md`

## Conclusiones
- Sistema funcional con 0€ usando Notion + Make free + ntfy.
- El foco está en robustez (anti-duplicado), claridad de UI en Notion y documentación para mantenimiento.
- Próximas mejoras: UI de dashboards y mejorar la app Detox (Catálogo + totales anuales).
