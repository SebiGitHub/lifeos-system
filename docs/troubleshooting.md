# Troubleshooting (errores típicos)

## 1) No llegan notificaciones ntfy
- Verifica status 200 del HTTP.
- Comprueba topic correcto.
- Revisa permisos/notificaciones/batería en Android.

## 2) No puedo actualizar una propiedad desde Make
- Si la propiedad es **Rollup** o **Formula**, NO es editable desde Make.
- Solución: crear una propiedad “manual” (Date/Number/Text) si necesitas escribir.

## 3) Se ejecuta dos veces y duplica XP/HP/racha
- Falta o está mal el flag anti-duplicado (ej: Days.Rachas procesadas).
- Solución: comprobar al inicio y marcar al final.

## 4) Make no muestra campos de Notion
- Re-conecta el módulo (Refresh).
- Cambia a Data Source (no “Database Legacy”).
- Asegura permisos de integración en Notion.

## 5) Recovery Quest no se “procesa”
- Falta marcar `Procesada=true` al finalizar.
- Falta filtro Player correcto.
- La ruta HP<=0 se está saltando por condición.

## 6) Totales anuales en Detox no cuadran
- Apps sin categoría no se incluyen en el sumatorio.
- Doble conteo por día.
- CSV export distinto a lo que muestra UI.
