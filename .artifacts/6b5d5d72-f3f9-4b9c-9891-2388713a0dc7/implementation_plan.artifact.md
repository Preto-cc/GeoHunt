# Plan de Implementación: Dependencias entre Lugares

Este plan permitirá crear una jerarquía de descubrimiento de lugares, donde un lugar puede estar bloqueado hasta que el usuario haya visitado otro lugar específico (dependencia).

## Cambios Propuestos

### Componente: Modelos de Datos

#### [MODIFY] [Models.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/Models.kt)
- **`Evento`**: Añadir el campo `mostrarLugaresBloqueados: Boolean = true`.
- **`Lugar`**: Añadir el campo `desbloqueadoPorLugarId: String? = null`.

### Componente: Repositorio

#### [MODIFY] [GeoHuntRepository.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/repository/GeoHuntRepository.kt)
- **`actualizarEvento`**: Persistir el nuevo campo `mostrarLugaresBloqueados`.
- **`actualizarLugar`**: Persistir el nuevo campo `desbloqueadoPorLugarId`.

### Componente: ViewModels de Administración

#### [MODIFY] [AdminViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/AdminViewModel.kt)
- Adaptar las funciones de creación y actualización para manejar los nuevos campos de dependencias y visibilidad.

### Componente: Interfaz de Administración

#### [MODIFY] [AdminScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminScreen.kt)
- Añadir el Checkbox **"Mostrar bloqueados"** en los diálogos de creación y edición de eventos (debajo de "Ranking Cercano").

#### [MODIFY] [AdminLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminLugaresScreen.kt)
- **`LugarDialog`**: Añadir un selector desplegable (`ExposedDropdownMenu`) llamado **"Desbloqueado por"** que muestre la lista de lugares del evento actual.

### Componente: Lógica de Juego (Participante)

#### [MODIFY] [LugaresViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/LugaresViewModel.kt)
- **`LugarUiState`**: Añadir el campo `bloqueado: Boolean`.
- Modificar el `combine` para calcular el estado de bloqueo: un lugar está bloqueado si tiene dependencia y el usuario aún no ha visitado el lugar del que depende.
- Obtener la configuración del `Evento` para aplicar la lógica de filtrado o cambio de etiqueta.

#### [MODIFY] [ListadoLugaresScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/ListadoLugaresScreen.kt)
- Filtrar la lista si `evento.mostrarLugaresBloqueados` es false y el lugar está bloqueado.
- Actualizar `StatusBadge` para mostrar la etiqueta **"Bloqueado"** cuando corresponda.
- Desactivar el click en lugares bloqueados.

#### [MODIFY] [DetalleLugarScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/DetalleLugarScreen.kt)
- Impedir el acceso o mostrar un mensaje de advertencia si un usuario intenta entrar a un lugar bloqueado vía URL/Navegación directa (por seguridad).

## Plan de Verificación

### Pruebas Manuales
1. **Admin**: Crear un Evento A con "Mostrar bloqueados" desactivado.
2. **Admin**: Crear Lugar 1 y Lugar 2 (Lugar 2 depende de Lugar 1).
3. **Jugador**: Entrar al Evento A y verificar que SOLO ve el Lugar 1.
4. **Jugador**: Visitar el Lugar 1. Verificar que el Lugar 2 aparece automáticamente en la lista.
5. **Admin**: Cambiar el Evento A a "Mostrar bloqueados" activado.
6. **Jugador (con otro usuario)**: Verificar que ve el Lugar 2 pero con el texto "Bloqueado".
7. **Jugador**: Intentar pulsar en el Lugar 2 bloqueado. El click debe estar desactivado.
