╔══════════════════════════════════════════════════════════════════════════════╗
║         APLICACIÓN VULNERABLE - PORTAL DE SUBVENCIONES                       ║
║    Herramienta Educativa para Enseñanza de Vulnerabilidades Web             ║
╚══════════════════════════════════════════════════════════════════════════════╝

📦 ARCHIVOS DISPONIBLES PARA DESCARGAR:

1. 📥 ARCHIVO PRINCIPAL:
   └─ subvenciones-vulnerable.zip  (38 KB)
      └─ Contiene TODO el proyecto listo para usar
      └─ OPCIÓN RECOMENDADA: Descargar esto y descomprimir

2. 📖 DOCUMENTACIÓN (Leer en este orden):

   a) QUICKSTART.md
      ├─ Guía de inicio rápido (5 minutos)
      ├─ Requisitos: Java 17+, Maven
      └─ 3 pasos para ejecutar la aplicación

   b) ESTRUCTURA_PROYECTO.md
      ├─ Explicación de carpetas y archivos
      ├─ Mapa de vulnerabilidades
      └─ Dónde buscar el código vulnerable

   c) README.md
      ├─ Explicación teórica de 4 vulnerabilidades
      ├─ IDOR, LFI, Manipulación de Parámetros, File Upload
      ├─ Código vulnerable comentado
      └─ Soluciones propuestas

   d) GUIA_EXPLOTACION.md
      ├─ 10+ métodos para explotar cada vulnerabilidad
      ├─ Ejemplos con navegador, Burp Suite, Python
      ├─ Scripts listos para ejecutar
      └─ Impacto de cada exploit

3. 🔧 CÓDIGO FUENTE JAVA (Controller con Vulnerabilidades):

   a) SolicitudController.java ⭐⭐⭐ MÁS IMPORTANTE
      ├─ Contiene TODAS las 4 vulnerabilidades
      ├─ IDOR en detalleSolicitud()
      ├─ LFI en descargarArchivo()
      ├─ Manipulación en resolverSolicitud()
      └─ File Upload en crearSolicitud()

   b) LoginController.java
      ├─ Sin encriptación de contraseñas
      ├─ Sin validación CSRF
      └─ Autenticación débil

   c) DashboardController.java
      └─ Redirección simple

   d) DataInitializer.java
      ├─ Carga usuarios y solicitudes de prueba
      └─ Usuarios: admin, usuario1, usuario2

4. 📦 ENTIDADES JPA (Modelos de Datos):

   a) Usuario.java
      └─ Campos: username, password, nombre, email, DNI, rol

   b) Solicitud.java
      └─ Campos: título, descripción, monto, estado, archivo

5. 🗄️ REPOSITORIOS (Acceso a BD):

   a) UsuarioRepository.java
      └─ Acceso a usuarios en base de datos

   b) SolicitudRepository.java
      └─ Acceso a solicitudes en base de datos

6. ⚙️ CONFIGURACIÓN:

   a) pom.xml
      ├─ Dependencias Maven
      ├─ Spring Boot 3.1.5
      ├─ H2 Database
      └─ Thymeleaf

   b) application.properties
      ├─ Puerto: 8080
      ├─ BD H2 en memoria
      └─ Configuración de uploads

7. 🌐 VISTAS HTML (7 páginas):

   a) login.html
      └─ Página de login con credenciales de prueba

   b) registro.html
      └─ Formulario de registro

   c) dashboard.html
      ├─ Panel principal
      ├─ Diferente para admin vs usuario
      └─ Tabla de solicitudes

   d) nueva_solicitud.html
      ├─ Formulario de creación
      └─ Incluye subida de archivo (VULNERABLE)

   e) detalle_solicitud.html
      ├─ Ver detalles de solicitud (VULNERABLE a IDOR)
      ├─ Panel admin para resolver (VULNERABLE a manipulación)
      └─ Descargar archivo (VULNERABLE a LFI)

   f) buscar_solicitudes.html
      └─ Búsqueda y filtrado (admin)

   g) error.html
      └─ Página de error

8. 📋 CLASE PRINCIPAL:

   a) SubvencionesVulnerableApplication.java
      └─ Punto de entrada de Spring Boot

═══════════════════════════════════════════════════════════════════════════════

🚀 PASOS PARA COMENZAR:

1. Descarga: subvenciones-vulnerable.zip
2. Extrae el ZIP
3. Lee: QUICKSTART.md (5 minutos)
4. Ejecuta: mvn spring-boot:run
5. Accede: http://localhost:8080/login
6. Estudia: README.md
7. Practica: GUIA_EXPLOTACION.md

═══════════════════════════════════════════════════════════════════════════════

👥 CUENTAS DE PRUEBA:

   Usuario: admin
   Contraseña: admin123
   Rol: Administrador

   Usuario: usuario1
   Contraseña: pass123
   Rol: Usuario normal

   Usuario: usuario2
   Contraseña: pass123
   Rol: Usuario normal

═══════════════════════════════════════════════════════════════════════════════

🔓 LAS 4 VULNERABILIDADES:

1. ⚠️ IDOR (Insecure Direct Object References)
   └─ Acceso a solicitudes de otros usuarios modificando ID en URL

2. ⚠️ LFI (Local File Inclusion)
   └─ Descarga de archivos del servidor usando ../../../etc/passwd

3. ⚠️ Manipulación de Parámetros
   └─ Cambio de montos y estados sin validación

4. ⚠️ Subida de Archivos Sin Validación
   └─ Subida de ejecutables y path traversal

═══════════════════════════════════════════════════════════════════════════════

📚 ORDEN RECOMENDADO DE LECTURA:

1. QUICKSTART.md           (5 min)  - Instalar y ejecutar
2. ESTRUCTURA_PROYECTO.md  (10 min) - Entender carpetas
3. README.md               (20 min) - Teoría de vulnerabilidades
4. GUIA_EXPLOTACION.md     (30 min) - Cómo explotar
5. Código Java             (30 min) - Analizar el código

═══════════════════════════════════════════════════════════════════════════════

✅ REQUISITOS:

- Java 17 o superior
- Maven 3.6+
- Navegador web
- (Opcional) Burp Suite Community para avanzado

═══════════════════════════════════════════════════════════════════════════════

⚠️ ADVERTENCIAS:

- Esta aplicación contiene vulnerabilidades INTENCIONALES
- Solo para uso educativo en ambiente aislado
- NO USAR EN PRODUCCIÓN
- NO USAR CONTRA SISTEMAS QUE NO SEAN DE TU PROPIEDAD
- Violará leyes si se usa sin autorización

═══════════════════════════════════════════════════════════════════════════════

🎓 ESTRUCTURA SUGERIDA DE CLASE:

SESIÓN 1 - Introducción (2 horas)
  ├─ Explicar OWASP Top 10
  ├─ Leer README.md
  └─ Ver aplicación funcionando

SESIÓN 2 - Explotación (3 horas)
  ├─ Seguir GUIA_EXPLOTACION.md
  ├─ Estudiantes exploran manualmente
  └─ Usar Burp Suite

SESIÓN 3 - Código (3 horas)
  ├─ Analizar SolicitudController.java
  ├─ Identificar vulnerabilidades
  └─ Proponer soluciones

SESIÓN 4 - Remediación (2 horas)
  ├─ Implementar fixes
  ├─ Verificar que exploits no funcionan
  └─ Discusión de mejores prácticas

═══════════════════════════════════════════════════════════════════════════════

¿TIENES DUDAS?

1. Lee README.md - Explicación completa
2. Lee GUIA_EXPLOTACION.md - Guías paso a paso
3. Busca "VULNERABLE" en el código Java
4. Los comentarios en el código explican qué está mal

═══════════════════════════════════════════════════════════════════════════════

Versión: 1.0.0
Última actualización: Abril 2024
Licencia: Educativa (No usar en producción)

═══════════════════════════════════════════════════════════════════════════════