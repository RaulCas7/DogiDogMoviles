# 🐾 DogiDog (App Móvil)

**DogiDog** es una aplicación móvil desarrollada en **Kotlin** que permite a los usuarios gestionar sus mascotas, ver su progreso, interactuar mediante un bot, recibir notificaciones, consultar documentación médica, visualizar logros, y mucho más. La app se conecta a una API RESTful y utiliza Firebase para el mapa y notificaciones. Todo de manera eficiente para que el usuario pueda usar la aplicación sin ningun problema. Ademas fomentando toda la socializacion y concienciamiento de los dueños de las mascotas.

## 📱 Características principales

- Registro e inicio de sesión de usuarios
- Gestión completa de mascotas
- Visualización de gráficos de peso
- Subida de fotos y documentación
- Notificaciones personalizadas
- DogiBot: chat interactivo con IA
- Geolocalización en mapas
- Sistema de logros y valoración de usuarios
- Interfaz colorida, adorable y usable 🩵

---

## 🛠️ Tecnologías utilizadas

- **Lenguaje principal**: Kotlin
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Firebase**: Autenticación, Analíticas y Cloud Messaging
- **Retrofit**: Cliente HTTP
- **Glide**: Carga de imágenes
- **MapLibre GL**: Mapas interactivos
- **GraphView**: Visualización de gráficas
- **Google Maps y Location Services**
- **Jetpack Navigation** + Safe Args
- **Parcelize, ViewBinding**

---

## 🧩 Estructura del proyecto

### 📁 adapters

- `DocumentosAdapter`
- `DogibotAdapter`
- `FotosAdapter`
- `LogrosAdapter`, `LogrosAdapterImage`
- `MascotaAdapter`, `MascotaPagerAdapter`
- `NotificacionesAdapter`
- `ValoracionAdapter`

### 📁 apiServices

- `ApiClient.kt`
- `ApiService.kt`

### 📁 dataModels

Modelos de datos para interactuar con la API:

- `Documentacion`, `DocumentacionCrear`
- `Foto`
- `Logro`
- `Mascota`, `MascotaCrear`
- `Mensaje`, `Notificacion`
- `PesoMascota`
- `Pregunta`, `Raza`
- `Recorrido`, `Tarea`
- `Usuario`, `UsuariosLogro`
- `Valoracion`

### 📁 inicioSesion

- `InicioSesionActivity`
- `RegistroActivity`

### 📁 mascotas

- `AnadirMascotaFragment`
- `CrearDocumentoFragment`
- `DocumentacionMascotaFragment`
- `GraficaPesoFragment`
- `MascotaGaleriaFragment`
- `MascotaInfoFragment`
- `MascotaPrincipalFragment`

### 📁 principal

- `ConfiguracionFragment`
- `DogiBotFragment`
- `LogrosFragment`
- `MapsFragment`
- `MascotasFragment`
- `MyMascotaRecyclerViewAdapter`
- `NotificacionesFragment`
- `PantallaPrincipalActivity`
- `PetSelectionFragment`

### 📁 placeholder

- `PlaceHolderContent.kt`

---

## ⚙️ Gradle & configuración

### build.gradle (nivel app)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}
```
### firebase

```kotlin
implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-messaging")
```

## 🛠️ Funcionalidades futuras (opcional)

- Poder subir fotos de las mascotas.
- Multilenguaje (es/en).
- Permitir ser invisibles en el mapa.
- Poder recuperar la contraseña con facilidad.
---

## 💖 Créditos

Desarrollado por Raúl Casas Gómez con cariño y pasión por las mascotas y asi ayudarlas a vivir en un entorno más seguro.  
Con una estética linda y funcional, para que la gestión sea tan encantadora como efectiva y sea fácil para los usuarios.  
**¡Gracias por usar DogiDogMovil! 🐕✨**

© 2025 DogiDog. Todos los derechos reservados.
 
