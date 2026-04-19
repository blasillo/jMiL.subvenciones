package es.hack4rt10n.jmyl.controller;

import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import es.hack4rt10n.jmyl.repository.SolicitudRepository;
import es.hack4rt10n.jmyl.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Directorio de cargas (VULNERABLE: sin validación de ruta)
    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

        if (usuario.isEmpty()) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(rol)) {
            // Admin ve todas las solicitudes
            List<Solicitud> solicitudes = solicitudRepository.findAll();
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("esAdmin", true);
        } else {
            // Usuario ve solo sus solicitudes
            List<Solicitud> solicitudes = solicitudRepository.findByUsuario(usuario.get());
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("esAdmin", false);
        }

        model.addAttribute("usuario", usuario.get());
        return "dashboard";
    }

    @GetMapping("/nueva")
    public String nuevaSolicitud(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        return "nueva_solicitud";
    }

    @PostMapping("/crear")
    public String crearSolicitud(@RequestParam String titulo,
                                 @RequestParam String descripcion,
                                 @RequestParam Double montoSolicitado,
                                 @RequestParam String categoria,
                                 @RequestParam Double ingresoMensual,
                                 @RequestParam String situacionLaboral,
                                 @RequestParam Integer miembrosFamilia,
                                 @RequestParam(required = false) MultipartFile archivo,
                                 HttpSession session,
                                 Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

        if (usuario.isEmpty()) {
            return "redirect:/login";
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

        // VULNERABILIDAD: Subida de archivos sin validación
        if (archivo != null && !archivo.isEmpty()) {
            try {
                // VULNERABLE: No valida tipo de archivo
                String nombreOriginal = archivo.getOriginalFilename();

                // VULNERABLE: Crea la carpeta uploads sin validar permisos
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // VULNERABLE: Guarda el archivo con su nombre original (path traversal)
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

        return "redirect:/solicitudes/dashboard";
    }

    // VULNERABILIDAD IDOR: No valida que el usuario acceda solo a sus solicitudes
    @GetMapping("/detalle/{id}")
    public String detalleSolicitud(@PathVariable Long id,
                                   HttpSession session,
                                   Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        // VULNERABILIDAD IDOR: Busca la solicitud solo por ID, sin validar propiedad
        Optional<Solicitud> solicitud = solicitudRepository.findById(id);

        if (solicitud.isEmpty()) {
            return "error";
        }

        // VULNERABLE: No comprueba si el usuario es propietario de la solicitud
        // Un usuario puede cambiar el ID en la URL y ver solicitudes de otros
        // Solo los ADMIN pueden ver todas, pero esto se debería validar mejor

        if (!"ADMIN".equals(rol) && !solicitud.get().getUsuario().getId().equals(usuarioId)) {
            // Esta validación existe pero es débil - el objetivo es que el estudiante la encuentre
            // En una explotación real, podrían incrementar el ID y acceder a otras solicitudes
        }

        model.addAttribute("solicitud", solicitud.get());
        model.addAttribute("esAdmin", "ADMIN".equals(rol));

        return "detalle_solicitud";
    }

    // VULNERABILIDAD: Endpoint administrativo sin protección adecuada
    @PostMapping("/resolver/{id}")
    public String resolverSolicitud(@PathVariable Long id,
                                    @RequestParam String nuevoEstado,
                                    @RequestParam(required = false) String razonRechazo,
                                    @RequestParam(required = false) Double montoAprobado,
                                    HttpSession session,
                                    Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");

        if (usuarioId == null ) /* || !"ADMIN".equals(rol)) */ {
            return "redirect:/login";
        }

        Optional<Solicitud> solicitud = solicitudRepository.findById(id);

        if (solicitud.isEmpty()) {
            return "error";
        }

        // VULNERABILIDAD: Manipulación de parámetros
        // El cliente puede enviar cualquier estado, no valida contra lista permitida
        // Debería validar contra: APROBADA, DENEGADA, PENDIENTE
        solicitud.get().setEstado(nuevoEstado);

        // VULNERABLE: El monto aprobado viene del cliente sin validación
        if (montoAprobado != null && montoAprobado > 0) {
            solicitud.get().setMontoSolicitado(montoAprobado);
        }

        solicitud.get().setRazonRechazo(razonRechazo);
        solicitud.get().setFechaResolucion(LocalDateTime.now());

        solicitudRepository.save(solicitud.get());

        return "redirect:/solicitudes/dashboard";
    }

    // VULNERABILIDAD: LFI - Local File Inclusion
    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarArchivo(@RequestParam String archivo,
                                                     HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // VULNERABILIDAD LFI: No valida la ruta del archivo
            // Un atacante puede usar "../" para acceder a archivos del sistema
            // Ejemplos de explotación:
            // archivo=../../../etc/passwd
            // archivo=../../application.properties
            // archivo=../../../../windows/win.ini

            String rutaArchivo = UPLOAD_DIR + archivo;

            Path path = Paths.get(rutaArchivo).normalize();
            File file = new File(rutaArchivo);

            // VULNERABLE: No comprueba si el archivo está dentro de UPLOAD_DIR
            // Debería hacer: if (!path.startsWith(Paths.get(UPLOAD_DIR).normalize()))

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Buscar solicitudes por criterios (para admin)
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String estado,
                         @RequestParam(required = false) String categoria,
                         HttpSession session,
                         Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");

        if (usuarioId == null || !"ADMIN".equals(rol)) {
            return "redirect:/login";
        }

        List<Solicitud> resultados;

        if (estado != null && !estado.isEmpty()) {
            resultados = solicitudRepository.findByEstado(estado);
        } else {
            resultados = solicitudRepository.findAll();
        }

        model.addAttribute("solicitudes", resultados);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroCategoria", categoria);

        return "buscar_solicitudes";
    }

    // Endpoint vulnerable para ver archivos de configuración (educativo)
    @GetMapping("/config")
    public ResponseEntity<?> config(@RequestParam String file, HttpSession session) {

        String rol = (String) session.getAttribute("usuarioRol");

        // VULNERABLE: Sin validación de rol - debería ser solo ADMIN
        // Cualquiera puede acceder a archivos de configuración

        try {
            // Leer archivo (VULNERABLE a LFI)
            String contenido = Files.readString(Paths.get(file));
            return ResponseEntity.ok(contenido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al leer archivo");
        }
    }
}