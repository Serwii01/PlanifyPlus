# 🌟 PlanifyPlus

**PlanifyPlus** es una aplicación de escritorio desarrollada en **Java (JavaFX + Maven)** orientada a la gestión de actividades dentro de una comunidad.  
Permite a los usuarios crear, descubrir e inscribirse en eventos de forma sencilla, incorporando funcionalidades de geolocalización y gestión avanzada mediante roles.

Este proyecto ha sido desarrollado como parte de la asignatura **Desarrollo de Interfaces**, aplicando principios de arquitectura limpia, buenas prácticas y testing.

---

## 🚀 Funcionalidades principales

- 🔐 **Sistema de autenticación**
  - Registro e inicio de sesión de usuarios
  - Validación de datos y control de acceso

- 👥 **Gestión de usuarios y roles**
  - Usuarios estándar y administradores
  - Panel de administración con herramientas de moderación

- 🗓️ **Gestión completa de actividades**
  - Creación, edición y eliminación de actividades
  - Información detallada (título, descripción, tipo, fecha, ubicación)

- 📍 **Geolocalización integrada**
  - Visualización de actividades en mapa mediante **Leaflet**
  - Gestión de coordenadas por ciudad

- 🧭 **Feed inteligente**
  - Actividades ordenadas por proximidad temporal
  - Indicadores de tiempo restante (cuenta atrás)

- ✅ **Sistema de inscripción**
  - Inscripción a actividades
  - Gestión de participación del usuario

- 🚨 **Sistema de denuncias**
  - Posibilidad de reportar actividades
  - Control de contenido por parte del administrador

- 👤 **Perfil de usuario**
  - Actividades creadas
  - Actividades inscritas
  - Información personal

- 🎨 **Interfaz gráfica moderna**
  - Construida con **JavaFX (FXML + CSS)**
  - Diseño modular y desacoplado

---

## 🧠 Arquitectura del proyecto

El proyecto sigue una arquitectura por capas claramente definida:

```text
Controller → Service → DAO → Base de datos
```

- **Controller** → lógica de interfaz y eventos (JavaFX)  
- **Service** → lógica de negocio  
- **DAO** → acceso a datos (Hibernate / JPA)  
- **DTO / Entities** → modelo de datos  

Esto permite:

- Separación de responsabilidades  
- Código mantenible y escalable  
- Facilidad para testing  

---

## 🧩 Tecnologías utilizadas

| Categoría | Herramienta / Librería |
|----------|------------------------|
| Lenguaje | Java 21 |
| Interfaz gráfica | JavaFX |
| Gestión de dependencias | Maven |
| Persistencia | Hibernate (JPA) |
| Base de datos | MySQL |
| Mapas | Leaflet |
| Testing | JUnit + Mockito |
| Arquitectura | MVC + capas |
| UI Design | Scene Builder + CSS |
| Control de versiones | Git / GitHub |

---

## 🗄️ Base de datos

El proyecto utiliza **MySQL** como sistema de almacenamiento, gestionado mediante **Hibernate (JPA)**.

Para ejecutar la aplicación es necesario crear previamente la base de datos:

```sql
CREATE DATABASE planifyplus;
```

---

## ▶️ Ejecución del proyecto

### Requisitos
- Java 21  
- MySQL activo  
- Maven (o Maven Wrapper)

### Ejecución

```bash
.\mvnw.cmd javafx:run
```

---

## 🧪 Testing

El proyecto incluye pruebas unitarias utilizando:

- **JUnit 5**
- **Mockito**

Enfocadas principalmente en:
- Controladores  
- Lógica de interacción  
- Comportamiento de la interfaz  

---

## 🎯 Objetivos del proyecto

- Aplicar desarrollo de interfaces con JavaFX  
- Implementar una arquitectura escalable  
- Integrar base de datos con ORM (Hibernate)  
- Trabajar con APIs externas (mapas)  
- Introducir testing en aplicaciones de escritorio  

---

## 👨‍💻 Autor

Proyecto desarrollado por **Sergio Fernández Morales, Iván Pastor López y Juan Carlos Moreno Moray**  
📍 Estudiantes de DAM
