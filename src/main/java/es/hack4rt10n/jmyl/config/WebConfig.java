package es.hack4rt10n.jmyl.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR);
    }

    /**
     * Permite ejecutar JSP subidos en /uploads/
     * VULNERABILIDAD INTENCIONAL EDUCATIVA: File Upload RCE via JSP webshell
     */
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(org.apache.catalina.Context context) {
                super.postProcessContext(context);
                context.setReloadable(true);

                // Habilita JSP en /uploads/ como parte del classpath
                java.nio.file.Path uploadsPath = java.nio.file.Paths.get(UPLOAD_DIR).toAbsolutePath();
                try {
                    String classpath = uploadsPath.toString();
                    String existingCP = context.getServletContext().getInitParameter("jsp.tagLibPackage");
                    System.out.println("Uploads JSP path configured: " + classpath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}