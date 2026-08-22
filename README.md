# GeoHunt 📍

**GeoHunt** es una aplicación móvil de "búsqueda del tesoro" basada en la ubicación, diseñada para transformar la exploración urbana en una experiencia interactiva y competitiva. Los usuarios participan en eventos, localizan puntos de interés ocultos y compiten por el mejor puesto en el ranking.

---

## ✨ Características Principales

### 🗺️ Exploración y Eventos
- **Listado de Eventos:** Visualiza eventos activos organizados por la comunidad o administradores.
- **Progreso Visual:** Cada evento muestra un indicador circular con el progreso de lugares visitados.
- **Lugares Ocultos:** Cada lugar tiene pistas, coordenadas y un radio de validación.

### 🎯 Mecánica de Juego
- **Validación por GPS:** Utiliza el sensor de ubicación para comprobar si estás lo suficientemente cerca del objetivo.
- **Sistema de Puntos:** Gana puntos por cada descubrimiento. ¡Los primeros en llegar reciben un **bonus especial**!
- **Mensajes Ocultos:** Al validar un lugar, desbloqueas información exclusiva o mensajes del organizador.

### 🏆 Competición Social
- **Ranking en Tiempo Real:** Clasificación actualizada al instante por evento, basada en puntos y tiempo de llegada.
- **Perfiles Personalizables:** Edita tu nombre mostrado y gestiona tu identidad.

### 🔐 Seguridad y Privacidad
- **Múltiples Modos de Acceso:** Soporta Google Sign-In y acceso Anónimo (con borrado automático de datos al salir).
- **Control de Datos:** Funcionalidad integrada para eliminar permanentemente la cuenta y todos los datos asociados.
- **Optimización Profesional:** Configurada con R8/ProGuard para un rendimiento máximo y protección de código.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Authentication & Cloud Firestore)
- **Localización:** Google Play Services Location
- **Navegación:** Compose Navigation

---

## 🚀 Instalación y Configuración

### Requisitos previos
1. Tener instalado [Android Studio Jellyfish](https://developer.android.com/studio) o superior.
2. Un proyecto en [Firebase Console](https://console.firebase.google.com/).

### Pasos
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/GeoHunt.git
   ```
2. **Configurar Firebase:**
   - Descarga tu archivo `google-services.json` desde Firebase Console.
   - Colócalo en la carpeta `app/` del proyecto.
3. **Reglas de Firestore:**
   - Sube el contenido de `firestore.rules` a tu consola de Firebase para asegurar los datos.
4. **Compilar y Ejecutar:**
   - Abre el proyecto en Android Studio, sincroniza Gradle y lánzalo en un dispositivo físico o emulador con Play Services.

---

## 📸 Capturas de Pantalla

| Inicio | Lista de Eventos | Detalle de Lugar | Ranking |
| :---: | :---: | :---: | :---: |
| ![Welcome](https://via.placeholder.com/200x400?text=Bienvenida) | ![Events](https://via.placeholder.com/200x400?text=Eventos) | ![Detail](https://via.placeholder.com/200x400?text=Detalle) | ![Ranking](https://via.placeholder.com/200x400?text=Ranking) |

---

## 📄 Licencia

Este proyecto es de código abierto y se distribuye bajo la licencia **MIT**. Es una aplicación gratuita y sin ánimo de lucro diseñada para fomentar la exploración local.

---

**Desarrollado por Santiago Prego** — [santiagoprego@gmail.com](mailto:santiagoprego@gmail.com)
