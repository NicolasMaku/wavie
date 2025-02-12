package mg.itu.prom16;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.affichage.Errors;
import mg.itu.prom16.annotations.*;
import mg.itu.prom16.exceptions.BadValidationException;
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
    public Object execMethod(HttpServletRequest req, HttpServletResponse resp, Class<?> authClass) throws Exception {
//        if (!this.getVerbActions().getVerb().getSimpleName().equalsIgnoreCase(req.getMethod()))

        VerbAction action = this.isVerbAvalaible(req);
        if (action == null)
            throw new Errors(404,"La methode http est differente de celle du controller (methode) : " + req.getMethod());

        try {
            return action.execMethod(req , resp, this.controller, authClass);
        }
        catch (BadValidationException ex) {
            throw ex;
        } catch (ServletException e) {
//            System.out.println("OOPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP : " + e.getClass().getName());
            throw new Errors(500,e.getMessage());
        }

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

    /**
     * Solution au sprint 14
     * @param req
     * @return
     * @throws Exception
     */
    protected VerbAction isVerbAvalaible(HttpServletRequest req) throws Exception {
        String httpVerb = req.getMethod();
        return isVerbAvalaible(httpVerb);
    }

    protected VerbAction isVerbAvalaible(String httpVerb) throws Exception {
        Class<?> verb = findVerb(httpVerb);

        for (VerbAction va : verbActions) {
            if (va.verb.equals(verb))
                return va;
        }
//        if (httpVerb.equalsIgnoreCase("get"))
//            verb = Post.class;
//        else
//            verb = Get.class;
//
//        for (VerbAction va : verbActions) {
//            if (va.verb.equals(verb))
//                return va;
//        }

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

}
