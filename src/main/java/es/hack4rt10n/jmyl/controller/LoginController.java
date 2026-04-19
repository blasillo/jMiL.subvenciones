package es.hack4rt10n.jmyl.controller;

import es.hack4rt10n.jmyl.model.Usuario;
import es.hack4rt10n.jmyl.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // VULNERABILIDAD: Sin protección CSRF, sin validación de contraseña
    // Se compara la contraseña en texto plano (educativo, nunca en producción)
    @PostMapping("/login")
    public String loginPost(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        if (usuario.isPresent() && usuario.get().getPassword().equals(password)) {
            // Vulnerabilidad: ID de usuario en sesión sin cifrar
            session.setAttribute("usuarioId", usuario.get().getId());
            session.setAttribute("usuarioNombre", usuario.get().getNombre());
            session.setAttribute("usuarioRol", usuario.get().getRol());
            session.setAttribute("username", username);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    // VULNERABILIDAD: Crear usuario sin validación adecuada
    @PostMapping("/registro")
    public String registroPost(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String nombre,
                               @RequestParam String apellidos,
                               @RequestParam String email,
                               @RequestParam String telefono,
                               @RequestParam String dni,
                               @RequestParam String direccion,
                               @RequestParam String ciudad,
                               @RequestParam String codigoPostal,
                               Model model) {

        // Vulnerabilidad: No valida si el usuario ya existe adecuadamente
        if (usuarioRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El usuario ya existe");
            return "registro";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password); // SIN ENCRIPTACIÓN - VULNERABLE
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellidos(apellidos);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setTelefono(telefono);
        nuevoUsuario.setDni(dni);
        nuevoUsuario.setDireccion(direccion);
        nuevoUsuario.setCiudad(ciudad);
        nuevoUsuario.setCodigoPostal(codigoPostal);
        nuevoUsuario.setRol("USER");

        usuarioRepository.save(nuevoUsuario);

        return "redirect:/login?registroExitoso=true";
    }
}