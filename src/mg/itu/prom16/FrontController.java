package mg.itu.prom16;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.Get;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontController extends HttpServlet {
    protected static List<Class<?>> controllerList = null;
    protected HashMap<String, Mapping> map = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Determiner la listes des controllers
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

        // construire le hashmap
        map = new HashMap<>();
        for (Class<?> clazz : controllerList) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Mapping element = new Mapping(clazz.getName(), method.getName()) ;
                    map.put(method.getAnnotation(Get.class).url(), element);
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

        // lister les controller dispo
//        for (Class<?> clazz : controllerList)
//            out.println(clazz.getSimpleName());

        for (String method : get_method(url))
            out.println(method);

    }

    protected List<String> get_method(String url) {
        List<String> methods = new ArrayList<>();

        for(Map.Entry<String, Mapping> entry : map.entrySet()) {
            if (entry.getKey().equals(url)) {
                methods.add(entry.getValue().method);
            }
        }

        return methods;
    }
}
