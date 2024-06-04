package mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
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
import java.lang.reflect.InvocationTargetException;
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
        getControllerList();

        // construire le hashmap
        buildControllerMap();

    }

    private void getControllerList() {
        List<Class<?>> liste_controller = new ArrayList<>();
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

    private void buildControllerMap() {
        map = new HashMap<>();
        for (Class<?> clazz : controllerList) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Mapping element = new Mapping(clazz.getName(), method.getName()) ;
                    map.put(method.getAnnotation(Get.class).value(), element);
                }

            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req,resp);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req,resp);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ServletException {
        // Montrer l'url saisie
        String url = req.getRequestURI();
        PrintWriter out = resp.getWriter();
        out.println("URL : " + url);

        // Execution de la methode
        for (Mapping method : get_method(url)) {
            // Affichage du nom de la methode et nom du controller
            out.println("Method: " + method.method + " ; Controller: " + method.controller);
            Class<?> clazz = Class.forName(method.controller);
            Method methode = clazz.getMethod(method.method);
            Object reponse = methode.invoke(clazz.newInstance());
            if (reponse instanceof ModelView) {
                ModelView mv = (ModelView) reponse;
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(mv.url);

                try {
                    for(Map.Entry<String , Object> entry : mv.data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    out.println(e.getMessage());
                }
                dispatcher.forward(req,resp);
            } else {
                out.println("Contenu : " + reponse);
            }
        }
        if (get_method(url).isEmpty())
            out.println("Aucune methode GET n'est disponible ici: 404 not found");

    }

    // Donne la liste des mapping(methodName, Controller) si elle trouve des methode correp au url
    protected List<Mapping> get_method(String url) {
        List<Mapping> methods = new ArrayList<>();

        for(Map.Entry<String, Mapping> entry : map.entrySet()) {
            String debut_url = "/"+ this.getInitParameter("project-name") + "/";
            if ((debut_url + entry.getKey()).equals(url)) {
                methods.add(entry.getValue());
            }
        }

        return methods;
    }
}
