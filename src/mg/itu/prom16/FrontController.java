package mg.itu.prom16;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FrontController extends HttpServlet {
    protected static List<Class<?>> controllerList = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        controllerList = new ArrayList<>();
        String packageController = this.getInitParameter("package-controller");

        String packageName = packageController.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        File dossier = new File(cl.getResource(packageName).getFile().replace("%20"," "));

        if (dossier.exists()) {
            String[] files = dossier.list();
            assert files != null;
            for (String file : files) {
                if (file.endsWith(".class")) {
                    String className = packageController + "." + file.substring(0, file.length() - 6);
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (clazz.isAnnotationPresent(Controller.class))
                        controllerList.add(clazz);

                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req,resp);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req,resp);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ClassNotFoundException {
//        Montrer l'url saisie
        String url = req.getRequestURI();
        PrintWriter out = resp.getWriter();
        out.println("URL : " + url);

//        lister les controller dispo
        for (Class<?> clazz : controllerList)
            out.println(clazz.getSimpleName());


    }
}
