# Resumen de Cambios: Ranking Cercano

Se ha implementado la funcionalidad de "Ranking Cercano" para los eventos, permitiendo un modo de juego más misterioso donde los jugadores solo ven su puntuación y la de sus competidores más directos.

## Cambios Realizados

### Backend y Lógica
- **[Models.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/Models.kt)**:
    - Añadido el campo `rankingCercano` a la clase `Evento`.
    - Añadido el campo `uid` a `UsuarioRanking` para permitir identificaciones precisas en la interfaz.
- **[GeoHuntRepository.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/data/repository/GeoHuntRepository.kt)**: Actualizada la lógica de persistencia y generación de ranking para incluir los nuevos campos.
- **[RankingViewModel.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/viewmodel/RankingViewModel.kt)**: Ahora obtiene la configuración del evento para saber si debe aplicar la restricción de visibilidad.

### Panel de Administración
- **[AdminScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/AdminScreen.kt)**:
    - Se ha añadido el checkbox **"Ranking Cercano"** en los diálogos de creación y edición de eventos, situado justo debajo de "Evento Activo".

### Experiencia del Usuario (Ranking)
- **[RankingScreen.kt](file:///C:/Users/santi/AndroidStudioProjects/GeoHunt/app/src/main/java/com/example/geohunt/ui/screens/RankingScreen.kt)**:
    - **Lógica de Visibilidad**:
        - Si el modo está activo, los jugadores normales solo ven los puntos del usuario inmediatamente superior e inferior a ellos en la tabla.
        - El resto de puntuaciones se muestran como **"Puntos: Oculto"**.
    - **Excepción de Admin**: Los administradores siguen viendo el ranking completo (todas las puntuaciones visibles) incluso si el modo cercano está activado.

## Verificación Realizada
- Se ha comprobado que el estado del checkbox se guarda correctamente.
- Se ha verificado que un administrador ve todos los datos.
- Se ha verificado que un usuario ve sus puntos y los de sus dos vecinos, pero no el resto.
