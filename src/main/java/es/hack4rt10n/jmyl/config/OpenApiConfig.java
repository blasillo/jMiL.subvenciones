package es.hack4rt10n.jmyl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger / OpenAPI 3.
 *
 * Acceso:
 *   - Swagger UI  → http://localhost:8080/swagger-ui.html
 *   - OpenAPI JSON → http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portal de Subvenciones — API Vulnerable")
                        .version("1.0.0")
                        .description("""
                                ## ⚠️ Aplicación educativa con vulnerabilidades INTENCIONALES
                                
                                """)
                        .contact(new Contact()
                                .name("Curso Seguridad Web")
                                .email("hack4rt10n@example.com"))
                        .license(new License()
                                .name("Solo uso educativo")
                                .url("https://example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desarrollo")))
                .tags(List.of(
                        new Tag().name("Autenticación")
                                .description("Login, logout y registro — SIN cifrado, SIN CSRF"),
                        new Tag().name("Solicitudes")
                                .description("CRUD de solicitudes — IDOR, Broken Access Control, LFI"),
                        new Tag().name("Usuarios")
                                .description("Gestión de usuarios — información sensible expuesta"),
                        new Tag().name("Actuator")
                                .description("Endpoints de monitorización de Spring Boot — todos abiertos (VULNERABLE)")
                ));
    }
}