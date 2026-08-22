# Plan de Implementación: Localización de Mensajes de Error

El objetivo de este plan es identificar y traducir al español los mensajes de error que actualmente se muestran en inglés al usuario. Muchos de estos errores provienen directamente de las excepciones capturadas de Firebase o del sistema Android.

## Cambios Propuestos

### Componente: Utilidades de Localización

#### [MODIFY] [LocationHelper.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/utils/LocationHelper.kt)
- Refinar el bloque `catch` en `getCurrentLocation` para interceptar excepciones comunes de Google Play Services y devolver mensajes en español (ej. "Permiso de ubicación denegado", "El servicio de ubicación no está disponible").

### Componente: ViewModels

#### [MODIFY] [WelcomeViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/WelcomeViewModel.kt)
- Añadir un mapeo para los errores de inicio de sesión de Firebase y Google Sign-In (ej. "La red no está disponible", "El usuario canceló el inicio de sesión").

#### [MODIFY] [ProfileViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/ProfileViewModel.kt)
- Traducir errores relacionados con la actualización del perfil y el borrado de cuenta (ej. "Debes iniciar sesión de nuevo para borrar tu cuenta").

#### [MODIFY] [DetalleLugarViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/DetalleLugarViewModel.kt)
- Asegurar que los errores de registro de visita y ubicación se presenten siempre en español.

### Componente: Interfaz de Usuario

#### [MODIFY] [AdminLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminLugaresScreen.kt)
- Revisar y asegurar que todas las validaciones del formulario de lugares estén en español.

## Plan de Verificación

### Pruebas Manuales
1. **Error de Ubicación**: Desactivar el GPS e intentar comprobar la distancia. Verificar que el mensaje sea descriptivo y en español.
2. **Error de Red**: Desactivar el Wi-Fi/Datos e intentar iniciar sesión. Verificar que el error no sea "Network error".
3. **Cancelación**: Iniciar el proceso de login con Google y cerrarlo sin elegir cuenta. Verificar el mensaje de feedback.
4. **Borrado de cuenta**: Intentar borrar la cuenta tras mucho tiempo de sesión (para forzar re-autenticación) y verificar el mensaje de error.
