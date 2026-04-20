package es.hack4rt10n.jmyl;

import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import es.hack4rt10n.jmyl.repository.SolicitudRepository;
import es.hack4rt10n.jmyl.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Override
    public void run(String... args) throws Exception {

        // =====================================================================
        // USUARIOS
        // =====================================================================

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword("admin123");
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

        Usuario u1 = usuario("usuario1","pass123","Juan","García López","juan@example.com","612345678","12345678A","Calle Principal 10","Barcelona","08002");
        Usuario u2 = usuario("usuario2","pass123","María","Rodríguez Martín","maria@example.com","623456789","87654321B","Avenida Central 25","Valencia","46001");
        Usuario u3 = usuario("usuario3","pass123","Carlos","Fernández Pérez","carlos@example.com","634567890","11111111C","Calle Mayor 3","Sevilla","41001");
        Usuario u4 = usuario("usuario4","pass123","Ana","López Sánchez","ana@example.com","645678901","22222222D","Paseo del Prado 5","Madrid","28014");
        Usuario u5 = usuario("usuario5","pass123","Luis","Martínez García","luis@example.com","656789012","33333333E","Gran Vía 10","Bilbao","48001");
        Usuario u6 = usuario("usuario6","pass123","Elena","Sánchez Torres","elena@example.com","667890123","44444444F","Rambla Catalunya 20","Barcelona","08007");
        Usuario u7 = usuario("usuario7","pass123","Pedro","Gómez Ruiz","pedro@example.com","678901234","55555555G","Calle Larios 8","Málaga","29005");
        Usuario u8 = usuario("usuario8","pass123","Laura","Díaz Moreno","laura@example.com","689012345","66666666H","Calle Sierpes 15","Sevilla","41004");
        Usuario u9 = usuario("usuario9","pass123","Miguel","Hernández Vega","miguel@example.com","690123456","77777777I","Avenida Blasco Ibáñez 30","Valencia","46021");
        Usuario u10 = usuario("usuario10","pass123","Sara","Jiménez Castro","sara@example.com","601234567","88888888J","Calle Fuencarral 50","Madrid","28004");

        List<Usuario> users = List.of(u1,u2,u3,u4,u5,u6,u7,u8,u9,u10);
        for (Usuario u : users) usuarioRepository.save(u);

        // =====================================================================
        // SOLICITUDES  (~100 registros, distribuidos en 2024, 2025 y 2026)
        // =====================================================================

        String[] categorias      = {"Educación","Vivienda","Empresa","Salud","Otro"};
        String[] estados         = {"PENDIENTE","APROBADA","DENEGADA"};
        String[] situaciones     = {"Empleado","Desempleado","Autónomo","Jubilado","Estudiante"};
        String[] razonesRechazo  = {
                "Ingresos superiores al umbral permitido",
                "Documentación incompleta",
                "No cumple los requisitos de la convocatoria",
                "Solicitud duplicada"
        };

        String[][] titulos = {
                {"Ayuda para matrícula universitaria","Beca de posgrado","Material escolar","Formación profesional","Curso de idiomas"},
                {"Reforma del hogar","Instalación de calefacción","Accesibilidad en vivienda","Alquiler social","Rehabilitación de fachada"},
                {"Inicio de negocio local","Digitalización de pyme","Contratación de personal","Compra de maquinaria","Apertura de franquicia"},
                {"Tratamiento médico prolongado","Ortopedia y prótesis","Atención domiciliaria","Medicamentos crónicos","Rehabilitación física"},
                {"Apoyo a familia numerosa","Ayuda de emergencia social","Integración laboral","Transporte adaptado","Comedor social"}
        };

        int count = 0;

        // 2024 — 35 registros
        for (int mes = 1; mes <= 12 && count < 35; mes++) {
            for (Usuario u : users) {
                if (count >= 35) break;
                int catIdx  = count % 5;
                int estIdx  = count % 3;
                int sitIdx  = count % 5;
                int titIdx  = count % 5;
                Solicitud s = solicitud(
                        u,
                        titulos[catIdx][titIdx],
                        categorias[catIdx],
                        estados[estIdx],
                        situaciones[sitIdx],
                        1000.0 + (count * 150),
                        800.0  + (count * 50),
                        (count % 4) + 1,
                        LocalDateTime.of(2024, mes, (count % 28) + 1, 10, 0),
                        estados[estIdx].equals("DENEGADA") ? razonesRechazo[count % 4] : null,
                        estados[estIdx].equals("PENDIENTE") ? null : LocalDateTime.of(2024, Math.min(mes + 1, 12), 15, 10, 0)
                );
                solicitudRepository.save(s);
                count++;
            }
        }

        // 2025 — 35 registros
        count = 0;
        for (int mes = 1; mes <= 12 && count < 35; mes++) {
            for (Usuario u : users) {
                if (count >= 35) break;
                int catIdx = (count + 2) % 5;
                int estIdx = (count + 1) % 3;
                int sitIdx = (count + 3) % 5;
                int titIdx = (count + 1) % 5;
                Solicitud s = solicitud(
                        u,
                        titulos[catIdx][titIdx],
                        categorias[catIdx],
                        estados[estIdx],
                        situaciones[sitIdx],
                        1200.0 + (count * 200),
                        900.0  + (count * 60),
                        (count % 5) + 1,
                        LocalDateTime.of(2025, mes, (count % 28) + 1, 11, 0),
                        estados[estIdx].equals("DENEGADA") ? razonesRechazo[(count + 1) % 4] : null,
                        estados[estIdx].equals("PENDIENTE") ? null : LocalDateTime.of(2025, Math.min(mes + 1, 12), 20, 10, 0)
                );
                solicitudRepository.save(s);
                count++;
            }
        }

        // 2026 — 30 registros
        count = 0;
        for (int mes = 1; mes <= 4 && count < 30; mes++) {
            for (Usuario u : users) {
                if (count >= 30) break;
                int catIdx = (count + 4) % 5;
                int estIdx = (count + 2) % 3;
                int sitIdx = (count + 1) % 5;
                int titIdx = (count + 3) % 5;
                Solicitud s = solicitud(
                        u,
                        titulos[catIdx][titIdx],
                        categorias[catIdx],
                        estados[estIdx],
                        situaciones[sitIdx],
                        1500.0 + (count * 250),
                        1000.0 + (count * 70),
                        (count % 6) + 1,
                        LocalDateTime.of(2026, mes, (count % 28) + 1, 9, 0),
                        estados[estIdx].equals("DENEGADA") ? razonesRechazo[(count + 2) % 4] : null,
                        estados[estIdx].equals("PENDIENTE") ? null : LocalDateTime.of(2026, Math.min(mes + 1, 4), 10, 10, 0)
                );
                solicitudRepository.save(s);
                count++;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Usuario usuario(String username, String password, String nombre, String apellidos,
                            String email, String telefono, String dni,
                            String direccion, String ciudad, String codigoPostal) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(password);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setDni(dni);
        u.setRol("USER");
        u.setDireccion(direccion);
        u.setCiudad(ciudad);
        u.setCodigoPostal(codigoPostal);
        return u;
    }

    private Solicitud solicitud(Usuario usuario, String titulo, String categoria,
                                String estado, String situacionLaboral,
                                Double monto, Double ingreso, Integer miembros,
                                LocalDateTime fechaCreacion, String razonRechazo,
                                LocalDateTime fechaResolucion) {
        Solicitud s = new Solicitud();
        s.setUsuario(usuario);
        s.setTitulo(titulo);
        s.setDescripcion("Solicitud de " + categoria.toLowerCase() + " presentada por " + usuario.getNombre());
        s.setMontoSolicitado(monto);
        s.setCategoria(categoria);
        s.setEstado(estado);
        s.setSituacionLaboral(situacionLaboral);
        s.setIngresoMensual(ingreso);
        s.setMiembrosFamilia(miembros);
        s.setFechaCreacion(fechaCreacion);
        s.setRazonRechazo(razonRechazo);
        s.setFechaResolucion(fechaResolucion);
        return s;
    }
}