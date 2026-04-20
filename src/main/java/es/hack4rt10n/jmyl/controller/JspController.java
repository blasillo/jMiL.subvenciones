package es.hack4rt10n.jmyl.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * VULNERABILIDAD: Ejecuta JSP subidos en /uploads/ como webshell
 * Permite RCE (Remote Code Execution) vía file upload
 */
@Controller
@RequestMapping("/uploads")
public class JspController {
    /**
     * POST /uploads/shell.jsp → ejecuta el JSP
     * Permite acceder a variables de sesión, ejecutar comandos, etc.
     */
    @GetMapping("/**")
    public void serveUpload(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {

        String path = request.getRequestURI();
        String filename = path.substring(path.lastIndexOf("/") + 1);

        if (filename.endsWith(".jsp")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            dispatcher.forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

