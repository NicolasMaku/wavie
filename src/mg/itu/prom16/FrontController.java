package mg.itu.prom16;

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
    protected static boolean checked = false;
    protected static List<String> controllerList = null;

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
//        String url = req.getRequestURI();
        PrintWriter out = resp.getWriter();
//        out.println("URL : " + url);

        if (!checked) {
            List<Class<?>> controllerClass = new ArrayList<>();
            controllerList = new ArrayList<>();
            String packageController = this.getInitParameter("package-controller");

            String path = packageController.replace('.','/');
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            java.net.URL resource = cl.getResource(path);
            assert resource != null;

            File dossier = new File(resource.getFile());

            if (dossier.exists()) {
                String[] files = dossier.list();
                assert files != null;
                for (String file : files) {
                    if (file.endsWith(".java")) {
                        String className = packageController + file.substring(0, file.length() - 5);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Controller.class))
                            controllerClass.add(clazz);

                    }
                }
            }

            for (Class<?> clazz : controllerClass) {
                controllerList.add(clazz.getSimpleName());
            }

            checked = true;
        }

        for (String controller : controllerList)
            out.println(controller);

    }
}
