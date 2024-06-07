package mg.itu.prom16;

import com.sun.source.doctree.ThrowsTree;
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

        try {
            // Determiner la listes des controllers
            getControllerList();

            // construire le hashmap
            buildControllerMap();
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }


    }

    private void getControllerList() throws Exception {
        List<Class<?>> liste_controller = new ArrayList<>();
        controllerList = new ArrayList<>();
        String packageController = this.getInitParameter("package-controller");

        String packageName = packageController.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        File dossier = null;
        try {
            dossier = new File(cl.getResource(packageName).getFile().replace("%20"," "));
        } catch (Exception e) {
            throw new Exception("Le package de controller '" + packageController + "' n'existe pas!");
        }

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

    private void buildControllerMap() throws Exception {
        map = new HashMap<>();
        for (Class<?> clazz : controllerList) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Mapping element = new Mapping(clazz.getName(), method.getName()) ;
                    if (map.containsKey(method.getAnnotation(Get.class).value()))
                        throw new Exception("Doublons de url controller");

                    map.put(method.getAnnotation(Get.class).value(), element);

                }

            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req,resp);
    }

    @SuppressWarnings("deprecation")
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // Montrer l'url saisie
        String url = req.getRequestURI();
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Execution de la methode
        if (!get_method(url).isEmpty()){

            try {
                for (Mapping method : get_method(url)) {

                    Class<?> clazz = Class.forName(method.controller);
                    Method methode = clazz.getMethod(method.method);
                    Object reponse = methode.invoke(clazz.newInstance());
                    if (reponse instanceof ModelView) {
                        ModelView mv = (ModelView) reponse;
                        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(mv.url);

                        for(Map.Entry<String , Object> entry : mv.data.entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }

                        dispatcher.forward(req,resp);

                    } else if (reponse instanceof String) {
                        out.println("URL : " + url + "\n");
                        // Affichage du nom de la methode et nom du controller
                        out.println("Method: " + method.method + " ; Controller: " + method.controller);

                        out.println("Contenu : " + reponse);
                    } else {
                        throw new ServletException("Le type de retour est inconnu");
                    }
                }
            } catch (Exception e) {
                throw new ServletException(e.getMessage());
            }
        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucune methode GET n'est disponible dans l'url " + url );

        }

    }

    // Donne la liste des mapping(methodName, Controller) si elle trouve des methode correp au url
    protected List<Mapping> get_method(String url) {
        List<Mapping> methods = new ArrayList<>();

        for(Map.Entry<String, Mapping> entry : map.entrySet()) {
            String debut_url = getServletContext().getContextPath() + "/";
            if ((debut_url + entry.getKey()).equals(url)) {
                methods.add(entry.getValue());
            }
        }

        return methods;
    }
}
