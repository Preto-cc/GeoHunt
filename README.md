# GeoHunt 📍

**GeoHunt** es una aplicación móvil de "búsqueda del tesoro" basada en la ubicación, diseñada para transformar la exploración urbana en una experiencia interactiva, misteriosa y competitiva. Los usuarios participan en eventos, localizan puntos de interés ocultos mediante GPS y compiten por los primeros puestos en el ranking.

---

## ✨ Características Principales

### 🗺️ Exploración y Eventos
- **Listado de Eventos:** Visualiza eventos activos organizados por la comunidad o administradores.
- **Progreso Visual:** Cada evento muestra un indicador circular interactivo con el progreso de lugares visitados.
- **Mecanismos de Desbloqueo de Lugares:** Los lugares pueden requerir visitar previamente otro punto del evento para desbloquearse.
- **Coordenadas Ocultas Opcionales:** Los organizadores pueden ocultar las coordenadas exactas de un lugar para basar la búsqueda puramente en pistas.

### 🎯 Mecánica de Juego
- **Validación por GPS:** Utiliza el sensor de ubicación para comprobar si el usuario está dentro del radio de validación del objetivo.
- **Sistema de Puntos y Bonificación:** Gana puntos por cada descubrimiento. ¡El primero en llegar recibe un **bonus especial de puntos**!
- **Mensajes Ocultos:** Al validar un lugar, se desbloquea información exclusiva o mensajes secretos del organizador.

### 🏆 Competición Social y Ranking Optimizado
- **Ranking Atómico en Tiempo Real:** Clasificación por evento agregada de forma eficiente en Firestore.
- **Modo Ranking Cercano:** Opción de evento para ocultar las puntuaciones exactas de rivales lejanos, manteniendo la tensión de la competición.
- **Perfiles Personalizables:** Edita tu nombre mostrado y gestiona tu identidad.

### 🔐 Seguridad y Adaptabilidad
- **Soporte Completo para Android 15:** Interfaz de usuario adaptada a la vista de **extremo a extremo (*Edge-to-Edge*)** con insets de seguridad.
- **Seguridad en Firestore Rules:** Reglas de seguridad atómicas que validan en el servidor que el incremento de puntos coincida exactamente con los puntos del lugar visitado.
- **Múltiples Modos de Acceso:** Soporta Google Sign-In y acceso Anónimo (con borrado automático de datos al cerrar sesión).
- **Múltiples Entornos (`dev` / `prod`):** Configuración nativa de variantes con sabores de Gradle para desarrollar sin afectar a producción.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **SDK Objetivo:** Android 15 (API level 35/36)
- **Interfaz de Usuario:** Jetpack Compose (Material 3) + Edge-to-Edge
- **Arquitectura:** MVVM (Model-View-ViewModel) + Flow
- **Backend:** Firebase (Authentication & Cloud Firestore)
- **Localización:** Google Play Services Location
- **Navegación:** Compose Navigation
- **Credenciales:** Android Credential Manager API

---

## 🚀 Instalación y Configuración

### Requisitos previos
1. Tener instalado [Android Studio Jellyfish](https://developer.android.com/studio) o superior.
2. Un proyecto en [Firebase Console](https://console.firebase.google.com/).

### Pasos
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Preto-cc/GeoHunt.git
   ```
2. **Configurar Firebase por Entorno:**
   - Para Desarrollo (`dev`): Descarga tu `google-services.json` de desarrollo y colócalo en `app/src/dev/google-services.json`.
   - Para Producción (`prod`): Coloca tu `google-services.json` de producción en `app/src/prod/google-services.json`.
3. **Reglas de Firestore:**
   - Publica el contenido del archivo `firestore.rules` en tu consola de Firebase.
4. **Compilar y Ejecutar:**
   - Abre el proyecto en Android Studio.
   - En la pestaña **Build Variants**, selecciona **`devDebug`** (para pruebas en desarrollo) o **`prodDebug`** (para producción).
   - Sincroniza Gradle y ejecuta la app en tu emulador o dispositivo físico.

---

## 📄 Licencia

Este proyecto es de código abierto y se distribuye bajo la licencia **MIT**. Es una aplicación gratuita y sin ánimo de lucro diseñada para fomentar la exploración local.

---

**Desarrollado por Santiago Prego** — [santiagoprego@gmail.com](mailto:santiagoprego@gmail.com)
