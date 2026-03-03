# Modelo Notion (BDs, propiedades clave y reglas)

## BDs principales
- **Days**
- **Habit Logs**
- **Habits**
- **Player**
- **Recovery Quests**
- **XP Ledger** (si se usa como histórico)
- **Ocio** -> (Libros, Media, Viajes, etc.)

> Nota: los nombres exactos de propiedades pueden variar; este documento describe las “propiedades clave” que usa Make.

## Days (día)
Propiedades clave (mínimas para “día completado”):
- **Nombre** (Title)
- **Fecha** (Date)
- **Dato importante** (Text)
- **¿Por qué no?** (Text) (obligatorio si el día queda incompleto)
- **Estado** (Select) (ej: Completado / Incompleto / Pendiente)
- **Player** (Relation)
- **DayKey** (Formula) (ej: YYYY-MM-DD)
- **WeekKey** (Formula) (ej: YYYY-Www)
- **MonthKey** (Formula) (ej: YYYY-MM)
- **Rachas procesadas** (Checkbox) (anti-duplicado para escenario 2)

## Habits (catálogo de hábitos)
Propiedades clave:
- **Nombre** (Title) (Gym/Study/Read/Improve/Pino/Proyecto)
- **Activo** (Checkbox) (si lo usas para ocultar hábitos que ya no aplican)
- **Cuenta racha** (Checkbox) (hábito obligatorio para que cuente la racha del día)
- **XP base** (Number)
- **Cap XP** (Number) (si limitas XP por log)
- **Streak** (Number) (racha individual del hábito)
- **MAX Streak** (Number) (máximo histórico)
- **StreakUpdatedFor** (Formula/Text/Date) (evita doble actualización del mismo día)

## Habit Logs (registro del día)
Regla: **si existe log, cuenta como hecho**.
Propiedades clave:
- **Nombre** (Title)
- **Fecha** (Date)
- **Minutos** (Number) (si aplica)
- **Hábito** (Relation → Habits)
- **Habit Log-Day / Day** (Relation → Days)
- **XP calculado** (Formula) (si calculas XP desde minutos/base/cap)

## Player (estado del jugador)
Propiedades clave:
- **HP** (Number)
- **HP Max** (Number)
- **Racha +⬆ días completados** (Number)
- **XP bonus hoy** (Number) (si lo usas en Make)
- **XP Total** (Rollup/Formula) (sumatorio de ledger + bonus + logros)
- **nº Perdido** (Number) (contador de “HP llegó a 0”)

## Recovery Quests
Propiedades clave:
- **Player** (Relation)
- **Tipo** (Select) (Leer / Correr / Estudiar / Detox móvil)
- **Objetivo (min)** (Number) (o horas)
- **Completada** (Checkbox)
- **Procesada** (Checkbox) (para no procesar dos veces)
- (Opcional) **Día creado** / **Día completado** (Relation → Days)

## Qué significa “día completado”
Un Day se considera **Completado** si:
- `Fecha` tiene valor
- `Dato importante` tiene valor
- Habitos mínimos completados (Entrenar, estudiar, leer y avanzar en algún proyecto)

Un Day se considera → **Incompleto** si:
- `Fecha` tiene valor
- `Dato importante` tiene valor
- Habitos mínimos **no** completados (Entrenar, estudiar, leer y avanzar en algún proyecto)

Un Day se considera → **Fallado** si al menos uno de estos campos no tiene valor:
- `Fecha`
- `Dato importante`

## Export y backup (procedimiento recomendado)
1) En Notion, en la página raíz del sistema: `⋯` → **Exportar**.
2) Formato: **Markdown & CSV**.
3) Activar: **Incluir subpáginas**.
4) Descargar el ZIP.

### Caso “ZIP dentro de ZIP”
A veces el export genera un ZIP principal que contiene un ZIP interno (ej: `ExportBlock-...-Part-1.zip`).
En ese caso:
1) Descomprimir el ZIP externo.
2) Descomprimir el ZIP interno.
3) Subir al repo la carpeta resultante (la que contiene el contenido exportado).

### Dónde se guarda en el repo
- `notion/exports/YYYY-MM-DD/` → export extraído listo para revisar.
- (Opcional) `notion/exports/YYYY-MM-DD-notion-export.zip` → ZIP original.

### Frecuencia recomendada
- Export semanal o cuando hagas cambios grandes en BDs o escenarios.
