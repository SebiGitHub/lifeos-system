# Escenario Make — Rachas + bonus (diario)

> Estado documentado: 8 de septiembre de 2026.

## Escenario activo

| Campo | Valor |
|---|---|
| Nombre | `Rachas + bonus (diario) Sebi (copy) CHatgpt` |
| Scenario ID | `7309406` |
| Estado | Activo |
| Horario | Todos los días a las `00:10`, zona `Europe/Madrid` |
| Día procesado | El día natural anterior |
| Conexión Notion | `Conexion Make-Notion (Sebi)` |
| Workspace | `LifeOS 2026 - Sebi` |
| Escenario anterior | `4804265`, inactivo |

El escenario procesa una sola vez el cierre diario de LifeOS, actualiza rachas, HP y XP, y gestiona Recovery Quests. La propiedad `Rachas procesadas` del Day actúa como protección contra ejecuciones duplicadas.

## Reglas actuales

- Solo los hábitos con `Activo = true` y `Cuenta racha = true` son obligatorios.
- Actualmente, solo **Gym** cuenta para la racha. Read, Study y Proyecto/Trabajo no cuentan.
- Un día se completa cuando todos los hábitos obligatorios tienen al menos un Habit Log único relacionado con ese Day.
- Los identificadores nulos de Habit Logs no cuentan como hábitos completados.
- Si falta algún hábito obligatorio:
  - el Day pasa a `Incompleto`;
  - la racha general pasa a 0;
  - se restan 5 HP y 5 XP por cada hábito obligatorio ausente;
  - se resetea la racha del hábito obligatorio no realizado;
  - si el HP llega a 0, se crea una Recovery Quest.
- Si se cumplen todos:
  - el Day pasa a `Completado`;
  - la racha general aumenta en 1;
  - se recuperan 3 HP por hábito obligatorio completado, sin superar `HP Max`;
  - se crea XP por cada Habit Log y un bonus de 5 XP por hábito obligatorio.
- Las rachas por hábito usan `StreakUpdatedFor` para no actualizarse dos veces para la misma fecha.

## Flujo principal

### 1. Player y Recovery Quest

- `15` — Busca el Player `Sebi`.
- `434` — If/Else según `HP <= 0`.
- `17` — Busca una Recovery Quest completada y no procesada.
- `439` — If/Else según exista una Recovery Quest.
- `19` — Recupera HP hasta `HP Max`.
- `20` — Marca la Recovery Quest como procesada.
- `374` — Crea el movimiento de XP de tipo `Recovery`.
- `449` y `446` — Reúnen las ramas If/Else.
- `454`, `443`, `435` y `457` — Controlan la variable `canProcess`.

Si el jugador sigue en Recovery y no hay una misión válida, `canProcess = false` y no se procesa el cierre diario.

### 2. Selección y validación del Day

- `182` — Busca el Day del día anterior por `DayKey` y Player.
- `129` — Separa “Day encontrado” y “Day inexistente”.
- `188` — Separa Day válido, ya procesado o incompleto en campos obligatorios.
- `189` y `427` — Marcan el Day como fallado y aplican la penalización de campos obligatorios.
- `181`, `238` y `237` — Envían las notificaciones correspondientes.

Un Day válido necesita:

1. `Fecha`.
2. `Dato importante` no vacío.
3. `Rachas procesadas = false`.

### 3. Conteo de mínimos

- `239` — Busca Habits con `Activo = true` y `Cuenta racha = true`.
- `275` — Agrega los hábitos obligatorios.
- `240` — Busca Habit Logs del Day cuyo `Cuenta racha (calc) = true`.
- `277` — Agrega `id` y `habitId`.
- `241` — Separa día completo e incompleto.

Conteo robusto de hábitos realizados:

```make
length(remove(distinct(map(277.array; "habitId")); null))
```

Cuando una búsqueda de Notion devuelve cero resultados, Make produce un paquete con `habitId = null`. Sin eliminar ese valor, se contabilizaba falsamente un hábito completado.

### 4. Día completado

- `245` — Marca el Day como `Completado`.
- `253` — Crea el bonus.
- `248` — Aumenta racha general y cura HP.
- `331` — Marca `Rachas procesadas = true`.
- `247` y `252` — Crean XP por cada Habit Log.
- `281` y `250` — Actualizan la racha de cada hábito.
- `254` — Notificación de éxito.

Condición:

```make
length(remove(distinct(map(277.array; "habitId")); null))
= 275.__IMTAGGLENGTH__
```

Además, `275.__IMTAGGLENGTH__ > 0`.

### 5. Día incompleto

- `246` — Marca el Day como `Incompleto`.
- `334` — Obtiene los hábitos obligatorios activos.
- `257` — Comprueba si existe un Habit Log del hábito en el Day.
- `338` y `339` — Separan hábitos hechos y no hechos.
- `333` — Resetea la racha del hábito no realizado.
- `423`, `425` y `426` — Conceden XP y actualizan la racha del hábito realizado.
- `258` — Calcula `missingCount`.
- `261` — Crea la penalización.
- `260` — Reduce HP y pone la racha general a 0.
- `271` — Marca `Rachas procesadas = true`.
- `264`–`269` — Gestionan HP, Recovery Quest y notificaciones.

Cálculo actual:

```make
max(
  0;
  275.__IMTAGGLENGTH__
  - length(remove(distinct(map(277.array; "habitId")); null))
)
```

## IDs de Notion actuales

| Base de datos | Data source ID |
|---|---|
| Player (Sebi) | `bd29a682-2540-8241-b253-872641c6304e` |
| Days (Sebi) | `4979a682-2540-8346-9d18-07b56160b3ff` |
| Habits (Sebi) | `feb9a682-2540-82df-a0e6-87373bf3f27e` |
| Habit Logs (Sebi) | `7ba9a682-2540-8313-b6dc-8734f2862c49` |
| XP Ledger (Sebi) | `c219a682-2540-83d2-abe9-07c6b9e285f1` |
| Recovery Quests (Sebi) | `7a99a682-2540-8270-9c49-8712df60509c` |

Los módulos `374`, `253`, `252`, `423`, `261` y `266` relacionan los registros con el Player localizado al comienzo mediante `{{15.id}}`, evitando IDs rígidos heredados del workspace anterior.

## Correcciones aplicadas el 08/09/2026

1. Sustitución de ramas duplicadas por If/Else con Merge.
2. Corrección de los Merge `449` y `446`.
3. Variable `canProcess` para bloquear el cierre durante una Recovery Quest pendiente.
4. Procesamiento del día anterior con zona `Europe/Madrid`.
5. Eliminación del límite de 10 resultados en búsquedas relevantes.
6. Conteo de hábitos únicos y exclusión explícita de `habitId = null`.
7. Corrección de filtros de día completo/incompleto y `missingCount`.
8. Sustitución de IDs antiguos del Player por `{{15.id}}`.
9. Solo Gym conserva `Cuenta racha = true`.
10. Neutralización de tres movimientos de XP incorrectos del 07/09, conservando trazabilidad.
11. Comprobación de conexiones, módulos huérfanos y ejecuciones incompletas.

## Pruebas verificadas

### Caso sin Gym

Fecha procesada: `07/09/2026`.

- Ejecución `d986c8cd362744f0aba2e008d4051a02`: correcta.
- Day: `Incompleto`.
- `missingCount = 1`.
- Gym: `Streak = 0`, `MAX Streak = 24`.
- Player: `HP = 20`, racha general `0`.
- Penalización: `-5 XP`, relacionada con Player y Day.

### Anti-duplicado

- Ejecución `794d4938e0094ad5b7b34c14b5da6c8b`: correcta.
- Detectó `Rachas procesadas = true`.
- No repitió penalización, cambios de HP ni actualizaciones de racha.

## Habit Logs que desaparecen

No existe ningún módulo en este escenario que elimine, archive o quite relaciones de Habit Logs. El escenario original está inactivo y la aplicación Android de este repositorio no contiene llamadas a Notion.

Con la evidencia disponible, la desaparición no puede atribuirse a este escenario. Las causas posibles restantes son una eliminación o archivo manual, otra integración externa o una vista filtrada de Notion.

## Archivos de esta carpeta

- `make-escenario.md`: documentación funcional vigente.
- `scenario-7309406-current.json`: snapshot técnico legible de la configuración verificada.
- `Rachas + bonus (diario).blueprint.json`: exportación histórica anterior. **No representa el escenario activo actual y no debe importarse como copia vigente.**

Para obtener un blueprint importable actualizado hay que exportar el escenario `7309406` desde el editor de Make y reemplazar manualmente el archivo histórico.
