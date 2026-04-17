# 💙 FamilyHealth

![FamilyHealth Logo](./assets/logo.png)

FamilyHealth es una aplicación Android moderna diseñada para asistir en la gestión de la salud familiar. Proporciona una plataforma centralizada para hacer un seguimiento de la medicación, gestionar citas médicas, registrar síntomas y administrar el perfil del usuario, garantizando que la información de salud importante esté siempre accesible.


## ✨ Características Principales

En base a la estructura de la base de datos y la arquitectura del sistema, la aplicación ofrece:

*   **Gestión de Usuarios:** Creación y administración de perfiles (registro, inicio de sesión seguro y persistencia local).
*   **Control de Medicación:** Registro de medicamentos, dosis requeridas y frecuencias. Sistema automatizado de notificaciones y recordatorios.
*   **Agenda de Citas Médicas:** Planificación, registro y seguimiento de citas médicas para los integrantes de la familia.
*   **Registro de Síntomas:** Funcionalidad para documentar y llevar un historial temporal de los síntomas.
*   **Exportación de Informes (PDF):** Capacidad de extraer y generar reportes en formato PDF con la información de salud compilada.


## 🛠 Tecnologías Utilizadas

El proyecto está construido sobre el ecosistema moderno de desarrollo Android:

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Interfaz de Usuario (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) con Material Design 3 para crear una experiencia declarativa, elegante y reactiva.
*   **Base de Datos Local:** [Room Database](https://developer.android.com/training/data-storage/room) para persistir datos como caché local (SQlite) con `MedicationEntity`, `AppointmentEntity`, `SymptomEntity` y `UserEntity`.
*   **Backend & Autenticación:** [Firebase](https://firebase.google.com/) (Auth y Firestore) para la gestión de acceso, sincronización y almacenamiento en la nube.
*   **Procesos en Segundo Plano:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) para la planificación e invocación de notificaciones de medicación de manera fiable.


## 🏗 Arquitectura

La aplicación adopta un patrón **MVVM (Model-View-ViewModel)** dentro de una **arquitectura en capas**.

*   **Patrón de Repositorio (Repository Pattern):** Abstracción de la fuente de datos mediante repositorios (ej. `AppointmentRepository`, `MedicationRepository`), combinando base de datos Room local y sincronización con Firebase para proveer una única fuente de la verdad (Single Source of Truth).
*   **Navegación:** Gestión de rutas y composición de pantallas a través de Jetpack Navigation Compose (`NavHost`).
*   **Corrutinas y Flow:** Manejo moderno y eficiente de tareas asíncronas para proveer flujos de datos reactivos a la UI.


## 🚀 Instalación y Despliegue

1. Clona el repositorio:
   ```bash
   git clone https://github.com/JavierAG8/FamilyHealth.git
   ```
2. Asegúrate de tener Android Studio instalado y actualizado.
3. Importa o abre el proyecto desde la raíz (`FamilyHealth/`).
4. Configura el archivo `google-services.json` de tu proyecto de Firebase introduciéndolo en la carpeta `app/`.
5. Ejecuta la aplicación en un emulador o un dispositivo físico.

---
*Desarrollado aplicando buenas prácticas de desarrollo móvil y demostrando competencias técnicas en el ecosistema nativo de Android.*
