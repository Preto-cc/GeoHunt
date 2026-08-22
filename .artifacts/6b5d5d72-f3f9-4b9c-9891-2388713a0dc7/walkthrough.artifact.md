# Paseo por los cambios: Localización de Errores

Se han traducido y mejorado los mensajes de error en toda la aplicación para ofrecer una experiencia de usuario más coherente en español.

## Cambios Realizados

### Lógica de Localización
- **[LocationHelper.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/utils/LocationHelper.kt)**:
    - Ahora identifica si el error se debe a que el GPS está desactivado o a la falta de permisos, devolviendo mensajes específicos en español.

### Autenticación y Perfil
- **[WelcomeViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/WelcomeViewModel.kt)**:
    - Se han mapeado los errores de Google Sign-In y Firebase Auth (red, cancelación del usuario, etc.) a frases amigables.
- **[ProfileViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/ProfileViewModel.kt)**:
    - Mejora en los mensajes de borrado de cuenta, informando específicamente si el usuario necesita re-autenticarse por motivos de seguridad.

### Juego y Administración
- **[DetalleLugarViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/DetalleLugarViewModel.kt)**:
    - Los errores al "comprobar distancia" ahora se presentan siempre en español, heredando las mejoras de `LocationHelper`.
- **[AdminLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminLugaresScreen.kt)**:
    - Se ha verificado que todas las validaciones de formularios (coordenadas, radio, puntos) se comuniquen correctamente en español.

## Verificación Realizada
- Se ha comprobado el flujo de errores desactivando la red y el GPS.
- Se han revisado los bloques `catch` para asegurar que no se escapen mensajes técnicos en inglés ("raw exceptions").
