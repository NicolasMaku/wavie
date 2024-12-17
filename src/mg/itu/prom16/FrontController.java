package mg.itu.prom16;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.affichage.Errors;
import mg.itu.prom16.annotations.*;
import mg.itu.prom16.annotations.verification.RequestWrapper.MethodChangingRequestWrapper;
import mg.itu.prom16.exceptions.BadValidationException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MultipartConfig
public class FrontController extends HttpServlet {
    protected static List<Class<?>> controllerList = null;
    protected HashMap<String, Mapping> map = null;
    protected Errors errors;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            // Determiner la listes des controllers
            getControllerList();
            // construire le hashmap
            buildControllerMap();
        } catch (Errors er) {
            errors = er;
        }
        catch (Exception e) {
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
                if (method.getAnnotations().length == 0) {
                    continue;
                }

                if (method.isAnnotationPresent(Url.class)) {
                    Mapping element = null ;
                    String urlValue = method.getAnnotation(Url.class).value();
//                    if (method.isAnnotationPresent(Restapi.class)) element.setRest(true);

                    Class<?> verb = getVerb(method);
                    VerbAction verbAction = new VerbAction(verb, method.getName());
                    if (method.isAnnotationPresent(Role.class)) {
                        verbAction.setRoles(method.getAnnotation(Role.class).autorisee());
                        verbAction.setAuthenticate(true);
                        System.out.println("RRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOOOOOOOOOOOLLLLLLLLLLLLLLLLLLLLLLLLLLLEEEEEEEEEEEEEEEEEEEEEE " + method.getName());
                    }

                    if (map.containsKey(urlValue)) {
                        element = map.get(urlValue);

                        if (element.getVerbAction(verbAction) == null) {
                            element.getVerbActions().add(verbAction);
                        }
                        else throw new Errors(500,"Doublons de methode(nom fonction) ou verb(GET,POST) dans le controller : " + urlValue);
                    }
                    else {
                        element = new Mapping(clazz.getName());
                        element.verbActions.add(verbAction);
                        map.put(urlValue, element);
                    }

                }



            }

        }
    }

    private Class<?> authentificationHandlerClass() {
        Class<?> handlerClass;
        String handlerClassName = this.getInitParameter("authentification-handler");
        try {
            handlerClass = Class.forName(handlerClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return handlerClass;
    }

//    protected boolean actionIsValid(String urlValue, Class<?> verb) {
//        if (!map.containsKey(urlValue))
//            return true;
//
//        Mapping mapping = map.get(urlValue);
//        if (mapping.getVerbAction().getVerb().equals(verb))
//            return false;
//        else return true;
//
//    }

    protected Class<?> getVerb(Method method) {
        if (method.isAnnotationPresent(Post.class))
            return Post.class;
        else
            return Get.class;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req,resp);
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (errors != null) {
            errors.returnError(req,resp);
            return;
        }
        Exception handlingException = null;

        // Montrer l'url saisie
        String url = req.getRequestURI();
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Execution de la methode
        try {
            if (get_method(url).isEmpty())
                throw new Errors(404,  "Aucune methode n'est disponible dans l'url " + url );
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucune methode GET n'est disponible dans l'url " + url );

            for (Mapping method : get_method(url)) {
                Object reponse = null;
                try {
                    reponse = method.execMethod(req, resp, authentificationHandlerClass());
                } catch (Errors er) {
                    throw er;
                }
                catch (Exception e) {
                    throw new ServletException(e.getMessage());
                }

                if (reponse instanceof ModelView) {
                    ModelView mv = (ModelView) reponse;

                    RequestDispatcher dispatcher;

                    if (mv.getErrorUrl() != null && req.getAttribute("validationException") != null) {
                        System.out.println("redirigé");
                        if (req.getMethod().equalsIgnoreCase("POST")) {
                            HttpServletRequest wrappedRequest = new MethodChangingRequestWrapper(req, "GET");
                            RequestDispatcher requestDispatcher = req.getRequestDispatcher(mv.getErrorUrl());
                            requestDispatcher.forward(wrappedRequest, resp);
                        }
//                        dispatcher = getServletContext().getRequestDispatcher(mv.getErrorUrl());
//                        dispatcher.forward(req,resp);
                    }
                    dispatcher = getServletContext().getRequestDispatcher(mv.url);

                    for(Map.Entry<String , Object> entry : mv.data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

//                    if (req.getAttribute("badValidation") != null) {
//                        req.setAttribute("badValidation", req.getAttribute("badValidation"));
//                        req.setAttribute("formDataValidation", req.getAttribute("formDataValidation"));
//                    }

                    dispatcher.forward(req,resp);

                } else if (reponse instanceof String) {
                    try {
                        out.println(reponse);
//                    out.println("URL : " + url + "\n");
                        // Affichage du nom de la methode et nom du controller
//                    out.println("Method: " + req.getMethod() + " ; Controller: " + method.controller);
//
//                    out.println("Contenu : " + reponse);
                        return;
                    } catch (Exception e) {
                        throw new ServletException(e.getMessage());
                    }
                } else {
                    throw new Errors(500, "Le type de retour est inconnu : " + reponse.getClass().getName());
                }
            }
        } catch (Errors er) {
            er.returnError(req, resp);
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
