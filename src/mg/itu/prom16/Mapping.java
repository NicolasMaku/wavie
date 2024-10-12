package mg.itu.prom16;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.affichage.Errors;
import mg.itu.prom16.annotations.*;
import mg.itu.prom16.serializer.MyJson;
import util.CustomSession;

import javax.swing.text.DateFormatter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Mapping {
    String controller;
//    Boolean isRest;
    List<VerbAction> verbActions;

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

//    public Boolean getRest() {
//        return isRest;
//    }
//
//    public void setRest(Boolean rest) {
//        isRest = rest;
//    }

    public List<VerbAction> getVerbActions() {
        return verbActions;
    }

    public void setVerbActions(List<VerbAction> verbActions) {
        this.verbActions = verbActions;
    }

    public Mapping(String controller, String method, Class<?> verb) {
        this.controller = controller;
//        this.isRest = false;
        this.verbActions = new ArrayList<VerbAction>();
    }

    public Mapping(String controller) {
        this.controller = controller;
        this.verbActions = new ArrayList<VerbAction>();
    }

    @SuppressWarnings("deprecation")
    public Object execMethod(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//        if (!this.getVerbActions().getVerb().getSimpleName().equalsIgnoreCase(req.getMethod()))

        VerbAction action = this.isVerbAvalaible(req);
        if (action == null)
            throw new Errors(404,"La methode http est differente de celle du controller (methode)");

//        System.out.println("interne : " + this.getVerbAction().getVerb().getSimpleName());
//        System.out.println("http : " + req.getMethod());
//
//        try {
//            Class<?> clazz = Class.forName(controller);
//
//            Method[] methodes = clazz.getDeclaredMethods();
//            Method oneMethod = null;
//            CustomSession customSession = null;
//
//            for(Method meth : methodes) {
//                if (meth.getName().equals(getVerbAction().getAction()))
//                    oneMethod = meth;
//            }
//            Class<?>[] classes = oneMethod.getParameterTypes();
//
//            try {
//
//                assert oneMethod != null;
//                Parameter[] parameters = oneMethod.getParameters();
//                Object[] arguments = new Object[parameters.length];
//                for (int i=0; i<parameters.length; i++) {
//                    if (parameters[i].isAnnotationPresent(Param.class)) {
//                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getAnnotation(Param.class).name()));
//                    } else if (parameters[i].isAnnotationPresent(Model.class)) {
//
//                        try {
//                            arguments[i] = getMethodObjet(parameters[i], req);
//                        } catch (Exception e) {
//                            throw new ServletException(e.getMessage());
//                        }
//                    } else if (parameters[i].getType().equals(CustomSession.class)) {
//                        customSession = new CustomSession(req.getSession());
////                        customSession.fromHttpSession(req.getSession());
//                        arguments[i] = customSession;
//                    } else {
////                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
//                        throw new ServletException("ETU002554 existe un argument qui n'est pas annotee");
//                    }
//                }
//
//                Object controllerInstance = clazz.newInstance();
//
//                // tester si la classe controller possede un attribut customSession
//                Field[] fields = clazz.getDeclaredFields();
//                for (Field field : fields) {
//                    if (field.getType().equals(CustomSession.class)) {
//                        customSession = new CustomSession(req.getSession());
//                        field.setAccessible(true);
//                        field.set(controllerInstance ,customSession);
//                    }
//                }
//
//
//                Object retour = null;
//                try {
//                    retour =  oneMethod.invoke(controllerInstance,arguments);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//                if (oneMethod.isAnnotationPresent(Restapi.class)) {
//                    MyJson gson = new MyJson();
//                    retour = gson.getGson().toJson(retour);
//                }
//
//                return retour;
//            } catch (Exception e) {
//                System.out.println("tato");
//                throw new ServletException(e.getMessage());
//            }
//
//
//        } catch (Exception e) {
//            throw new ServletException(e.getMessage());
//        }

        return action.execMethod(req , resp, this.controller);

    }

//    @SuppressWarnings("deprecation")
//    Object getMethodObjet(Parameter parameter, HttpServletRequest req) throws ServletException {
//        Class<?> classeParametre = parameter.getType();
//        String prefix = parameter.getAnnotation(Model.class).value();
//        Field[] fields = classeParametre.getDeclaredFields();
//        Object objet = null;
//        try {
//            try { objet = classeParametre.newInstance(); } catch (Exception e) { throw new ServletException("Pas de constructeur par defaut"); }
//
//            Map<String, String[]> parameterMap = req.getParameterMap();
//            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//
//                Method setter = null;
//                for(Field field: fields) {
//                    if (( prefix + "." + field.getName()).equals(entry.getKey())) {
//                        setter = searchMethod(classeParametre, "set" + capitalizeFirstLetter(field.getName()));
//                    }
//                }
//
//                if (setter == null) {
//
//                }
//                else
//                    setter.invoke(objet, parse(setter.getParameterTypes()[0],entry.getValue()[0]));
//            }
//
//        } catch (Exception e) {
//            throw new ServletException(e);
//        }
//        return objet;
//    }

//    Method searchMethod(Class<?> clazz, String methodName) {
//        Method[] listeMethode = clazz.getDeclaredMethods();
//        for(Method meth : listeMethode) {
//
//            if (meth.getName().equals(methodName)) {
//                return meth;
//            }
//        }
//
//        return null;
//    }

    protected VerbAction isVerbAvalaible(HttpServletRequest req) throws Exception {
        String httpVerb = req.getMethod();
        Class<?> verb = findVerb(httpVerb);

        for (VerbAction va : verbActions) {
            if (va.verb.equals(verb))
                return va;
        }

        return null;
    }

    protected VerbAction getVerbAction(VerbAction verbAction) throws Exception {

        for (VerbAction va : verbActions) {
            System.out.println(va.verb.getSimpleName() + " vs " + verbAction.verb.getSimpleName());
            System.out.println("and " + va.action + " vs " + verbAction.action);
//            if (verb.equals(va.verb))
//                return va;
            if (va.equals(verbAction))
                return va;
        }

        return null;
    }

    protected Class<?> findVerb(String httpVerb) throws Exception {
        if (httpVerb.equalsIgnoreCase("get"))
            return Get.class;
        else if (httpVerb.equalsIgnoreCase("post"))
            return Post.class;

        System.out.println("LE IZY : " + httpVerb);
        throw new Exception("Verb inconnu");
    }

//    public static String capitalizeFirstLetter(String word) {
//        if (word == null || word.isEmpty()) {
//            return word;
//        }
//        return word.substring(0, 1).toUpperCase() + word.substring(1);
//    }
}
