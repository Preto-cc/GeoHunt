# Plan de Implementación: Ranking Cercano

Este plan permitirá configurar eventos con un "Ranking Cercano". En esta modalidad, los participantes solo podrán ver su propia puntuación y la de los usuarios inmediatamente por encima y por debajo en la clasificación. Las puntuaciones de los demás usuarios aparecerán como "Oculto".

## Cambios Propuestos

### Componente: Modelos de Datos

#### [MODIFY] [Models.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/Models.kt)
- Añadir el campo `rankingCercano: Boolean = false` a la clase `Evento`.
- Actualizar `UsuarioRanking` para incluir el campo `uid: String` de forma explícita.

### Componente: Repositorio

#### [MODIFY] [GeoHuntRepository.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/repository/GeoHuntRepository.kt)
- Actualizar `actualizarEvento` para persistir el campo `rankingCercano`.
- Modificar `getRanking` para rellenar el nuevo campo `uid` en los objetos `UsuarioRanking`.

### Componente: ViewModels

#### [MODIFY] [AdminViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/AdminViewModel.kt)
- Actualizar `crearEvento` y `actualizarEvento` para manejar el parámetro `rankingCercano`.

#### [MODIFY] [RankingViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/RankingViewModel.kt)
- Modificar para obtener también los detalles del `Evento` (necesario para saber si el ranking cercano está activo).
- Adaptar la lógica de mapeo de nombres al nuevo campo `uid`.

### Componente: Interfaz de Usuario (Administración)

#### [MODIFY] [AdminScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminScreen.kt)
- Añadir un `Checkbox` "Ranking Cercano" en los diálogos de creación y edición de eventos.

### Componente: Interfaz de Usuario (Ranking)

#### [MODIFY] [RankingScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/RankingScreen.kt)
- Implementar la lógica de visibilidad:
    - Identificar si el usuario actual es administrador.
    - Identificar la posición del usuario actual en la lista.
    - Si `rankingCercano` es true **y el usuario NO es administrador**:
        - Mostrar los puntos solo si el usuario en esa fila es el usuario actual, el de arriba (i-1) o el de abajo (i+1).
        - Para el resto de filas, mostrar "Puntos: Oculto".
    - Si el usuario **ES administrador**, mostrar siempre todas las puntuaciones.

## Plan de Verificación

### Pruebas Manuales
1. **Admin**: Crear un evento con "Ranking Cercano" activado (colocado debajo de "Evento Activo").
2. **Jugador**: Entrar en el ranking y verificar que solo ve su puntuación y la de sus dos rivales más directos.
3. **Admin**: Entrar en el ranking del mismo evento y verificar que él sí puede ver TODAS las puntuaciones sin restricciones.
