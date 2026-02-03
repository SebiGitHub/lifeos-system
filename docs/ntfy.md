# ntfy (notificaciones)

## Config rápida en el móvil
1. Instala ntfy (Android).
2. Suscríbete al topic: `YOUR_TOPIC`, si quieres el mio, contáctame.
3. Permite notificaciones y desactiva ahorro agresivo de batería para ntfy.

## Publicar desde Make
En Make, usa módulo **HTTP / Make a request**:

- URL: `https://ntfy.sh/YOUR_TOPIC`
- Method: POST (o PUT)
- Headers (ejemplo):
  - `Title: LifeOS`
  - `Priority: 3` (o 1–5)
  - `Tags: check,calendar` (opcional)
- Body: texto del mensaje

## Checklist si no llega la notificación
1) ¿El módulo HTTP da **Status 200**?
2) ¿El mensaje aparece en el **historial del topic**?
3) En Android:
   - ¿Notificaciones permitidas para ntfy?
   - ¿Canal silenciado?
   - ¿Ahorro de batería bloqueando push?
4) ¿Topic correcto (sin espacios, mismo host)?
5) Si usas “priority” alto, ¿la app lo respeta?

## Seguridad básica
- Trata el topic como “semi-secreto” si el repo es público.
- No subas capturas donde salga el topic completo si no quieres que sea público.
