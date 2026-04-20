package es.hack4rt10n.jmyl.controller;

import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import es.hack4rt10n.jmyl.repository.SolicitudRepository;
import es.hack4rt10n.jmyl.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API REST documentada con Swagger / OpenAPI 3.
 *
 * Todos los endpoints tienen vulnerabilidades INTENCIONALES para el curso.
 * Accede a la documentación en: http://localhost:8080/swagger-ui.html
 */
@RestController
@RequestMapping("/api")
@Tag(name = "API REST", description = "Endpoints REST de la aplicación — documentados con Swagger")
public class ApiRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    private static final String UPLOAD_DIR = "/uploads/";

    // =========================================================================
    // AUTENTICACIÓN
    // =========================================================================

    @Operation(
            summary = "Login de usuario",
            description = """
            ## ⚠️ VULNERABLE: Broken Authentication
            
            - Contraseñas almacenadas y comparadas en **texto plano** (sin hash).
            - Sin protección **CSRF**.
            - Sin límite de intentos (no hay **rate limiting** → fuerza bruta posible).
            - El ID de sesión no se renueva tras login (**Session Fixation**).
            
            **Exploit ejemplo:**
            ```
            POST /api/auth/login
            { "username": "admin", "password": "admin123" }
            ```
            """,
            tags = {"Autenticación"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto — devuelve datos del usuario"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de acceso",
                    content = @Content(examples = @ExampleObject(
                            value = """
                        {
                          "username": "usuario1",
                          "password": "pass123"
                        }
                        """
                    ))
            )
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        // VULNERABLE: comparación en texto plano
        if (usuario.isPresent() && usuario.get().getPassword().equals(password)) {
            session.setAttribute("usuarioId", usuario.get().getId());
            session.setAttribute("usuarioRol", usuario.get().getRol());
            session.setAttribute("username", username);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Login correcto");
            respuesta.put("usuarioId", usuario.get().getId());
            respuesta.put("username", usuario.get().getUsername());
            respuesta.put("rol", usuario.get().getRol());
            // VULNERABLE: devuelve la contraseña en texto plano en la respuesta
            respuesta.put("password_VULNERABLE", usuario.get().getPassword());

            return ResponseEntity.ok(respuesta);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
    }

    @Operation(
            summary = "Logout",
            description = "Invalida la sesión activa.",
            tags = {"Autenticación"}
    )
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada"));
    }

    @Operation(
            summary = "Registro de nuevo usuario",
            description = """
            ## ⚠️ VULNERABLE: Contraseña sin cifrar + sin validación
            
            - La contraseña se guarda en **texto plano** en la base de datos.
            - No valida formato de email, DNI ni teléfono.
            - El rol siempre es `USER`, pero un atacante podría intentar **Mass Assignment**
              si la API acepta el campo `rol` en el body.
            """,
            tags = {"Autenticación"}
    )
    @PostMapping("/auth/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        if (usuarioRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPassword(body.get("password")); // VULNERABLE: sin cifrar
        nuevo.setNombre(body.get("nombre"));
        nuevo.setApellidos(body.get("apellidos"));
        nuevo.setEmail(body.get("email"));
        nuevo.setTelefono(body.get("telefono"));
        nuevo.setDni(body.get("dni"));
        nuevo.setDireccion(body.get("direccion"));
        nuevo.setCiudad(body.get("ciudad"));
        nuevo.setCodigoPostal(body.get("codigoPostal"));
        nuevo.setRol("USER");

        usuarioRepository.save(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario creado", "id", nuevo.getId()));
    }

    // =========================================================================
    // USUARIOS
    // =========================================================================

    @Operation(
            summary = "Listar todos los usuarios",
            description = """
            ## ⚠️ VULNERABLE: Información sensible expuesta
            
            - Devuelve **todos** los usuarios incluyendo su contraseña en texto plano.
            - Sin control de acceso: cualquier usuario autenticado puede listar a todos.
            
            **Dato expuesto crítico:** campo `password` en la respuesta.
            """,
            tags = {"Usuarios"}
    )
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }
        // VULNERABLE: expone todos los datos incluida la contraseña
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = """
            ## ⚠️ VULNERABLE: Exposición de datos sensibles + sin autorización
            
            - Cualquier usuario autenticado puede ver los datos de cualquier otro usuario.
            - La respuesta incluye **contraseña en texto plano**, DNI, teléfono, etc.
            """,
            tags = {"Usuarios"}
    )
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // SOLICITUDES
    // =========================================================================

    @Operation(
            summary = "Listar solicitudes",
            description = """
            - Admin → devuelve **todas** las solicitudes.
            - Usuario → devuelve solo las suyas.
            """,
            tags = {"Solicitudes"}
    )
    @GetMapping("/solicitudes")
    public ResponseEntity<?> listarSolicitudes(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rol = (String) session.getAttribute("usuarioRol");

        if ("ADMIN".equals(rol)) {
            return ResponseEntity.ok(solicitudRepository.findAll());
        } else {
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
            return usuario
                    .<ResponseEntity<?>>map(u -> ResponseEntity.ok(solicitudRepository.findByUsuario(u)))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
    }

    @Operation(
            summary = "Ver detalle de una solicitud por ID",
            description = """
            ## ⚠️ VULNERABLE: IDOR (Insecure Direct Object Reference)
            
            El endpoint no comprueba si la solicitud pertenece al usuario que la solicita.
            Cualquier usuario autenticado puede incrementar el `{id}` en la URL y acceder
            a las solicitudes de otros usuarios, viendo datos personales (DNI, ingresos, etc.).
            
            **Exploit:**
            ```
            GET /api/solicitudes/1   ← solicitud propia
            GET /api/solicitudes/2   ← solicitud de otro usuario ✓ (no debería funcionar)
            GET /api/solicitudes/3   ← solicitud de otro usuario ✓
            ```
            """,
            tags = {"Solicitudes"}
    )
    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<?> detalleSolicitud(
            @Parameter(description = "ID de la solicitud — prueba con IDs de otros usuarios", example = "1")
            @PathVariable Long id,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // VULNERABLE IDOR: no valida que la solicitud pertenezca al usuario
        return solicitudRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Crear nueva solicitud",
            description = """
            ## ⚠️ VULNERABLE: File Upload sin validación
            
            - No valida el tipo MIME ni la extensión del archivo subido.
            - El nombre original del archivo se usa directamente → **Path Traversal**.
            - Se pueden subir ficheros `.jsp`, `.sh`, o cualquier ejecutable.
            
            **Exploit Path Traversal en nombre de fichero:**
            ```
            nombre de archivo: ../../../../etc/cron.d/backdoor
            ```
            """,
            tags = {"Solicitudes"}
    )
    @PostMapping(value = "/solicitudes/crear", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearSolicitud(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam Double montoSolicitado,
            @RequestParam String categoria,
            @RequestParam Double ingresoMensual,
            @RequestParam String situacionLaboral,
            @RequestParam Integer miembrosFamilia,
            @RequestParam(required = false) MultipartFile archivo,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setUsuario(usuario.get());
        solicitud.setTitulo(titulo);
        solicitud.setDescripcion(descripcion);
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setCategoria(categoria);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setIngresoMensual(ingresoMensual);
        solicitud.setSituacionLaboral(situacionLaboral);
        solicitud.setMiembrosFamilia(miembrosFamilia);

        if (archivo != null && !archivo.isEmpty()) {
            try {
                // VULNERABLE: nombre original sin saneamiento
                String nombreOriginal = archivo.getOriginalFilename();
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String rutaArchivo = UPLOAD_DIR + nombreOriginal;
                archivo.transferTo(new File(rutaArchivo));

                solicitud.setNombreArchivo(nombreOriginal);
                solicitud.setRutaArchivo(rutaArchivo);
                solicitud.setTipoArchivo(archivo.getContentType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        solicitudRepository.save(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Solicitud creada", "id", solicitud.getId()));
    }

    @Operation(
            summary = "Resolver / cambiar estado de una solicitud",
            description = """
            ## ⚠️ VULNERABLE: Broken Access Control
            
            - La comprobación de rol ADMIN está **comentada** en el código.
            - Cualquier usuario autenticado puede cambiar el estado de **cualquier** solicitud.
            - El estado viene del cliente sin validación → se puede poner cualquier valor arbitrario.
            - El `montoAprobado` viene del cliente sin límite superior → un usuario puede
              aprobarse a sí mismo cualquier cantidad.
            
            **Exploit:**
            ```
            POST /api/solicitudes/3/resolver
            { "nuevoEstado": "APROBADA", "montoAprobado": 999999 }
            → Cualquier usuario puede aprobar la solicitud de otro
            ```
            """,
            tags = {"Solicitudes"}
    )
    @PostMapping("/solicitudes/{id}/resolver")
    public ResponseEntity<?> resolverSolicitud(
            @Parameter(description = "ID de la solicitud a resolver", example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // VULNERABLE: la comprobación de ADMIN está omitida
        // Debería ser: if (!"ADMIN".equals(session.getAttribute("usuarioRol"))) { return 403; }

        Optional<Solicitud> solicitud = solicitudRepository.findById(id);
        if (solicitud.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // VULNERABLE: estado sin validar contra lista permitida
        solicitud.get().setEstado((String) body.get("nuevoEstado"));
        solicitud.get().setRazonRechazo((String) body.get("razonRechazo"));
        solicitud.get().setFechaResolucion(LocalDateTime.now());

        if (body.get("montoAprobado") != null) {
            // VULNERABLE: monto aprobado sin límite, viene del cliente
            solicitud.get().setMontoSolicitado(
                    Double.parseDouble(body.get("montoAprobado").toString())
            );
        }

        solicitudRepository.save(solicitud.get());
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud actualizada", "solicitud", solicitud.get()));
    }

    @Operation(
            summary = "Descargar archivo adjunto",
            description = """
            ## ⚠️ VULNERABLE: LFI — Local File Inclusion / Path Traversal
            
            El parámetro `archivo` se concatena directamente a la ruta base `/uploads/`
            sin ninguna validación ni normalización.
            
            **Exploits:**
            ```
            GET /api/solicitudes/descargar?archivo=../../../etc/passwd
            GET /api/solicitudes/descargar?archivo=../../application.properties
            GET /api/solicitudes/descargar?archivo=../../../proc/self/environ
            ```
            
            La última permite leer variables de entorno del proceso, que pueden
            contener contraseñas, tokens de API, etc.
            """,
            tags = {"Solicitudes"}
    )
    @GetMapping("/solicitudes/descargar")
    public ResponseEntity<Resource> descargarArchivo(
            @Parameter(
                    description = "Nombre del archivo — prueba con '../../../etc/passwd'",
                    example = "documento.pdf"
            )
            @RequestParam String archivo,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // VULNERABLE LFI: concatenación sin validación de ruta
            String rutaArchivo = UPLOAD_DIR + archivo;
            File file = new File(rutaArchivo);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Leer fichero del sistema (endpoint de diagnóstico)",
            description = """
            ## ⚠️ VULNERABLE: LFI directo — sin restricción de ruta ni de rol
            
            Permite leer **cualquier fichero del sistema** al que tenga acceso el proceso Java.
            No hay validación de rol ni de ruta.
            
            **Exploits:**
            ```
            GET /api/config?file=/etc/passwd
            GET /api/config?file=/etc/shadow
            GET /api/config?file=/proc/self/environ
            GET /api/config?file=/app/application.properties
            ```
            """,
            tags = {"Solicitudes"}
    )
    @GetMapping("/config")
    public ResponseEntity<?> leerConfig(
            @Parameter(description = "Ruta absoluta del fichero a leer", example = "/etc/passwd")
            @RequestParam String file,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // VULNERABLE: sin validación de rol ni de ruta
        try {
            String contenido = Files.readString(Paths.get(file));
            return ResponseEntity.ok(Map.of("file", file, "contenido", contenido));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se pudo leer: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Buscar solicitudes por estado y categoría",
            description = "Solo accesible para ADMIN. Filtra por estado y/o categoría.",
            tags = {"Solicitudes"}
    )
    @GetMapping("/solicitudes/buscar")
    public ResponseEntity<?> buscarSolicitudes(
            @Parameter(description = "Estado: PENDIENTE, APROBADA, DENEGADA")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Categoría: Educación, Vivienda, Empresa, Salud")
            @RequestParam(required = false) String categoria,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");

        if (usuarioId == null || !"ADMIN".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado"));
        }

        List<Solicitud> resultados = (estado != null && !estado.isEmpty())
                ? solicitudRepository.findByEstado(estado)
                : solicitudRepository.findAll();

        return ResponseEntity.ok(resultados);
    }
}

