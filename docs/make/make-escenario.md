# Escenarios Make (1 activos por plan gratuito)

## Qué hace exactamente
### Ruta A — “Recovery completada” (cuando hay quest pendiente)

Objetivo: si el jugador ya estaba a HP <= 0 y existe una Recovery Quest completada y no procesada, entonces:

- Restaura HP del jugador a HP Max.
- Marca la Recovery Quest como Procesada.
- Crea un registro en XP Ledger (tipo “Recovery”).
- Notifica por ntfy.sh.

Módulos clave que aparecen en el blueprint:
- 15 — Notion Player (Search Objects): obtiene el Player (p.ej. por Nombre = Sebi).
- 16 — Router: decide ruta en función de HP.
- 17 — Notion Recovery Quests (Search Objects): busca recovery completada y no procesada.
- 19 — Notion Player (Update a Database Item): HP = HP Max.
- 20 — Notion Recovery Quests (Update a Data Source Item): Procesada = true.
- 374 — Notion XP Ledger (Create a Page): log de “Recovery”.
- 22 — HTTP (Make a request): notificación a ntfy.

### Ruta B — “Cierre del día” (rachas + bonus)

Objetivo: procesar un Day de Notion (el día que el usuario ha rellenado), y aplicar:
- Día completado si se han cumplido los hábitos mínimos (los que tienen Cuenta racha = true).
- Día incompleto si falta alguno.
- Actualización de racha general (Player) y HP.
- Actualización de racha por hábito (Habits), con anti-duplicado por fecha.
- XP Ledger:
  - “Hábito” por cada Habit Log del día.
  - “Bonus” si cumple mínimos.
  - “No Hábito”/“Penalización” si no cumple mínimos.
- Notificación por ntfy.

Validaciones (antes de tocar nada)
1. Existe el Day
- Si no existe: notifica y termina.
2. El Day está “relleno” y no se ha procesado ya
- Comprueba Fecha existe.
- Comprueba Dato importante (array) > 0.
- Comprueba Rachas procesadas = false.
- Si falla: notifica (p.ej. “faltan campos obligatorios”) y termina.

En el blueprint aparecen validadores de este estilo:
- 129 / 188 (cuando el Day se maneja como 182)
- 282 / 283 (cuando el Day se maneja como 24)
- 314 — HTTP (Make a request): notificación “LifeOS Dia fallado” (faltan campos obligatorios)

## Lógica de “mínimos” (Cuenta racha)
### Cómo se obtiene el “RequiredCount” (hábitos mínimos)
- Buscar en Habits: Activo = true y Cuenta racha = true.
  - Ejemplo en blueprint:
    - 239 — RequiredCount (Notion Habits - Search Objects)
    - 284 — Notion Habits (Search Objects) (otra parte equivalente)
- Contar cuántos hábitos devuelve esa búsqueda (agregadores tipo “BasicAggregator”):
  - 285 (para 284)

### Cómo se obtiene lo “hecho” (hoy)
Hay 2 ideas distintas en el escenario:
- Habit Logs del día (todos):
  - 247 (si el Day es 182)
  - 290 (si el Day es 24)

- Habit Logs del día que cuentan para racha (usando una fórmula tipo Cuenta racha (calc) en Habit Logs):
  - 286 — Notion Habit Logs (Search Objects) filtrando Cuenta racha (calc) = true
  - 287 — BasicAggregator para contar esos logs

## Rama ÉXITO (cumple mínimos)
Condición (concepto): “nº de hábitos mínimos == nº de hábitos (únicos) completados que cuentan para racha” y RequiredCount > 0.
Acciones típicas:

1. Marcar el Day como completado + marcar “Rachas procesadas”.
2. Subir racha general del Player (+ curación HP).
  - En el blueprint aparece:
    - 292 — Notion Player (Update a Database Item):
      - Racha +⬆ días completados = Racha + 1
      - HP = min(HP Max; HP + (nº Habit Logs del día * 2))
      - (en esa parte se usa el nº de bundles de 290)
3. Actualizar racha de cada hábito completado (independiente de “Cuenta racha”), con anti-duplicado por fecha:
- Se usa StreakUpdatedFor para evitar sumar 2 veces el mismo día.
- Ejemplo de filtro que aparece:
  - 294 — Notion Habits (Update) con condición StreakUpdatedFor != today
4. XP Ledger:
- “Hábito completado!” por cada log:
  - 295 — Notion XP Ledger (Create a Page) (en la rama que usa 290)
  - 252 — Notion XP Ledger (Create a Page) (en la rama que usa 247)
- “Mínimos superados!” (Bonus):
  - 293 — Notion XP Ledger (Create a Page) (bonus con 5 * nº de logs)
  - 253 — Notion XP Ledger (Create a Page) (otra variante equivalente)
5. Notificación de éxito:
- Suele existir un HTTP MakeRequest dedicado (p.ej. “Racha mantenida”).

## Rama FALLO (NO cumple mínimos)
Condición (concepto): “faltan hábitos mínimos”.
Acciones típicas:
1. Marcar el Day como incompleto + marcar “Rachas procesadas”.
2. Reset de racha general (Player) + penalización de HP.
3. Reset de racha SOLO de hábitos NO completados ese día (no “los que no cuentan para racha”).
  - La forma correcta en Make (sin magia) es:
    - Iterar hábitos activos y, por cada hábito, comprobar si existe al menos 1 Habit Log del día con ese hábito.
    - Si NO existe → resetear ese hábito.
En tu escenario esto aparece con este patrón:
- 334 — Notion Habits (Search Objects): lista de hábitos a evaluar (normalmente Activo = true)
- Iterator sobre esos hábitos
- 257 — “Todos los hábitos hechos ese día” (Notion Habit Logs - Search Objects):
  - filtro: Habit Logs-Days contains DayID AND Hábito contains HabitID
  - limit = 1 (solo queremos saber si existe)
- Router para separar:
  - HECHO (existe log)
  - NO HECHO (no existe log)
- 359 — Notion Habits (Update): reset (Streak = 0 + StreakUpdatedFor = today) para el NO HECHO
4. Recovery Quest (solo si tras la penalización el Player queda con HP <= 0)
- En el blueprint aparece una parte específica:
  - 264 — Router
  - 265 — Notion Habits (Search Objects) filtrado por HP <= 0 (condición basada en el Player ya actualizado)
  - (y módulos posteriores de creación/notificación)
5. Notificación de fallo:
- 304 — HTTP (Make a request) (en tu blueprint hay una notificación tipo “Perdiste la racha!”)
- 314 — HTTP (Make a request) se usa para “Día fallado” por validación (faltan campos), no por “no cumplir mínimos”.

## Requisitos en Notion (mínimo viable)
### Bases de datos implicadas (según IDs vistos en el blueprint)
- Player (Data source): 2f69a682-2540-8055-9c64-000bb2dc4a32
  - Campos usados en Make: HP, HP Max, Racha +⬆ días completados
- Days (Data source): 2f69a682-2540-8081-a866-000b822c2fef
  - Campos usados: Fecha, Dato importante, Rachas procesadas, DayKey, relación con Player
- Habits (Data source): 2f69a682-2540-80f0-9dfe-000b29abbaca
  - Campos usados: Activo, Cuenta racha, Streak, MAX Streak, StreakUpdatedFor
- Habit Logs (Data source): 2f69a682-2540-8032-91ff-000b6f640689
  - Campos usados: relación Habit Logs-Days, relación Hábito, y (si existe) fórmula Cuenta racha (calc)
- XP Ledger (Database): 2f69a682-2540-80f5-95de-ff9903177c8a
- Recovery Quests (Data source): 2f69a682-2540-807a-8816-000bb2720ffb
Importante: en Make usa módulos Notion de tipo Data Source (no “Database (Legacy)”). Si cambias el Data source seleccionado, hay que revisar y remapear campos.

## Notificaciones (ntfy.sh)
- El escenario envía notificaciones mediante HTTP POST.
- En el blueprint aparece el topic:
  - https://ntfy.sh/sebi-lifeos-9f3k2x (ej. módulo 314)
En Android:
1. Instala la app de ntfy.
2. Suscríbete al topic sebi-lifeos-9f3k2x.
3. Verás notificaciones con Title, Click y Priority según el caso.

## Cómo probar (sin romper nada)
1. Crea/elige un Day de prueba en Notion y rellena:
- Fecha
- Dato importante (que no quede vacío)
- Rachas procesadas = false
2. Crea Habit Logs para ese Day:
- Asegúrate de que cada log tiene relación al Day (Habit Logs-Days) y al Habit
