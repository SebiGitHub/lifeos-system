# Escenarios Make (2 activos por plan gratuito)

## Convenciones
- Se usa `DayKey` para buscar el día (más robusto que el título).
- Se usa `Player` en Days para soportar multiusuario en el futuro.
- Se usa un flag en Days (`Rachas procesadas`) como anti-duplicado.

---

## Escenario 1: Cierre del día
**Objetivo:** asegurar que existe el Day de hoy y que está “Completado” o “Incompleto”, y notificar.

**Trigger:** programado diario (recomendado 23:30 Europe/Madrid) [AJUSTA a tu horario real en Make]

**Entradas:**
- Fecha actual
- Player actual

**Salidas:**
- Day actualizado o creado
- Notificación por ntfy

**Reglas:**
- Si no existe Day: se crea con Estado=Incompleto + campos mínimos.
- Si existe Day:
  - si mínimos completos → Estado=Completado y notifica “bien”
  - si mínimos incompletos → Estado=Incompleto y notifica “rellena”

---

## Escenario 2: Rachas + bonus (diario)
**Objetivo:** calcular racha diaria + rachas individuales por hábito, HP, bonus XP y recovery quest.

**Trigger:** programado diario (recomendado 23:45 Europe/Madrid) [AJUSTA a tu horario real en Make]

**Entradas:**
- Player (HP, HP Max, racha días)
- Day de hoy (DayKey + Player)
- Habits (Cuenta racha)
- Habit Logs del día

**Salidas:**
- Player actualizado (HP, racha, bonus XP, nº Perdido si aplica)
- Habits actualizados (Streak, MAX Streak, StreakUpdatedFor)
- Recovery Quest creada/actualizada si HP llega a 0
- Flag en Days `Rachas procesadas` para anti-duplicado
- Notificación por ntfy

**Anti-duplicado (idempotencia):**
- Si Day.Rachas procesadas = true → el escenario termina sin cambiar nada.
- Si no → procesa y al final marca Day.Rachas procesadas = true.
