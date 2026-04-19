package es.hack4rt10n.jmyl;
import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import es.hack4rt10n.jmyl.repository.SolicitudRepository;
import es.hack4rt10n.jmyl.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuarios de prueba

        // Admin
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword("admin123"); // Sin encriptación - VULNERABLE
        admin.setNombre("Administrador");
        admin.setApellidos("Sistema");
        admin.setEmail("admin@subvenciones.es");
        admin.setTelefono("600000000");
        admin.setDni("00000000A");
        admin.setRol("ADMIN");
        admin.setDireccion("Calle Admin 1");
        admin.setCiudad("Madrid");
        admin.setCodigoPostal("28001");
        usuarioRepository.save(admin);

        // Usuario 1
        Usuario usuario1 = new Usuario();
        usuario1.setUsername("usuario1");
        usuario1.setPassword("pass123");
        usuario1.setNombre("Juan");
        usuario1.setApellidos("García López");
        usuario1.setEmail("juan@example.com");
        usuario1.setTelefono("612345678");
        usuario1.setDni("12345678A");
        usuario1.setRol("USER");
        usuario1.setDireccion("Calle Principal 10");
        usuario1.setCiudad("Barcelona");
        usuario1.setCodigoPostal("08002");
        usuarioRepository.save(usuario1);

        // Usuario 2
        Usuario usuario2 = new Usuario();
        usuario2.setUsername("usuario2");
        usuario2.setPassword("pass123");
        usuario2.setNombre("María");
        usuario2.setApellidos("Rodríguez Martín");
        usuario2.setEmail("maria@example.com");
        usuario2.setTelefono("623456789");
        usuario2.setDni("87654321B");
        usuario2.setRol("USER");
        usuario2.setDireccion("Avenida Central 25");
        usuario2.setCiudad("Valencia");
        usuario2.setCodigoPostal("46001");
        usuarioRepository.save(usuario2);

        // Crear solicitudes de prueba

        // Solicitud 1 - Usuario 1
        Solicitud solicitud1 = new Solicitud();
        solicitud1.setUsuario(usuario1);
        solicitud1.setTitulo("Ayuda para educación superior");
        solicitud1.setDescripcion("Solicito ayuda para continuar mis estudios de máster en ingeniería");
        solicitud1.setMontoSolicitado(6000.0);
        solicitud1.setCategoria("Educación");
        solicitud1.setEstado("PENDIENTE");
        solicitud1.setFechaCreacion(LocalDateTime.now());
        solicitud1.setIngresoMensual(1200.0);
        solicitud1.setSituacionLaboral("Desempleado");
        solicitud1.setMiembrosFamilia(3);
        solicitudRepository.save(solicitud1);

        // Solicitud 2 - Usuario 1
        Solicitud solicitud2 = new Solicitud();
        solicitud2.setUsuario(usuario1);
        solicitud2.setTitulo("Mejora de vivienda");
        solicitud2.setDescripcion("Necesito ayuda para reparaciones en mi vivienda");
        solicitud2.setMontoSolicitado(3500.0);
        solicitud2.setCategoria("Vivienda");
        solicitud2.setEstado("APROBADA");
        solicitud2.setFechaCreacion(LocalDateTime.now().minusDays(10));
        solicitud2.setFechaResolucion(LocalDateTime.now().minusDays(5));
        solicitud2.setIngresoMensual(1200.0);
        solicitud2.setSituacionLaboral("Desempleado");
        solicitud2.setMiembrosFamilia(3);
        solicitudRepository.save(solicitud2);

        // Solicitud 3 - Usuario 2
        Solicitud solicitud3 = new Solicitud();
        solicitud3.setUsuario(usuario2);
        solicitud3.setTitulo("Ayuda para emprendimiento");
        solicitud3.setDescripcion("Quiero iniciar mi propio negocio de consultoría");
        solicitud3.setMontoSolicitado(10000.0);
        solicitud3.setCategoria("Empresa");
        solicitud3.setEstado("PENDIENTE");
        solicitud3.setFechaCreacion(LocalDateTime.now());
        solicitud3.setIngresoMensual(0.0);
        solicitud3.setSituacionLaboral("Desempleado");
        solicitud3.setMiembrosFamilia(2);
        solicitudRepository.save(solicitud3);

        // Solicitud 4 - Usuario 2
        Solicitud solicitud4 = new Solicitud();
        solicitud4.setUsuario(usuario2);
        solicitud4.setTitulo("Formación y reciclaje profesional");
        solicitud4.setDescripcion("Curso de especialización en desarrollo web");
        solicitud4.setMontoSolicitado(2000.0);
        solicitud4.setCategoria("Educación");
        solicitud4.setEstado("DENEGADA");
        solicitud4.setRazonRechazo("Ingresos superiores al umbral permitido");
        solicitud4.setFechaCreacion(LocalDateTime.now().minusDays(20));
        solicitud4.setFechaResolucion(LocalDateTime.now().minusDays(15));
        solicitud4.setIngresoMensual(2500.0);
        solicitud4.setSituacionLaboral("Empleado");
        solicitud4.setMiembrosFamilia(1);
        solicitudRepository.save(solicitud4);
    }
}
