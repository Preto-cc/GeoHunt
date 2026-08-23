# Resumen de Cambios: Dependencias entre Lugares

Se ha implementado un sistema de prerrequisitos para los lugares de un evento. Ahora es posible condicionar la visibilidad o el acceso a un lugar en función de si otro lugar ha sido visitado previamente.

## Cambios Realizados

### Backend y Lógica
- **[Models.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/Models.kt)**:
    - `Evento`: Añadido `mostrarLugaresBloqueados`.
    - `Lugar`: Añadido `desbloqueadoPorLugarId`.
- **[LugaresViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/LugaresViewModel.kt)**: Implementada la lógica que calcula si un lugar está bloqueado comparando sus dependencias con el historial de visitas del usuario.

### Panel de Administración
- **[AdminScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminScreen.kt)**: Nuevo check **"Mostrar bloqueados"** para controlar la visibilidad global de la jerarquía.
- **[AdminLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminLugaresScreen.kt)**: Integrado un **menú desplegable** en el diálogo de edición de lugares para seleccionar fácilmente de qué otro lugar depende el actual.

### Experiencia del Usuario
- **[ListadoLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/ListadoLugaresScreen.kt)**:
    - Si el evento oculta bloqueados, los lugares no aparecerán hasta ser desbloqueados.
    - Si el evento muestra bloqueados, aparecerán con un badge rojo de **"Bloqueado"** y el clic estará inhabilitado.
- **[DetalleLugarScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/DetalleLugarScreen.kt)**: Añadida una pantalla de seguridad que informa al usuario si intenta acceder a un lugar bloqueado.

## Verificación Realizada
- Se ha comprobado que un lugar dependiente desaparece/aparece según la configuración de "Mostrar bloqueados".
- Se ha verificado que al visitar el lugar "padre", el lugar "hijo" se desbloquea instantáneamente.
- La navegación directa a un ID de lugar bloqueado muestra ahora el mensaje de advertencia correspondiente.
