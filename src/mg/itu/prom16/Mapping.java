package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Param;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class Mapping {
    String controller;
    String method;

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Mapping(String controller, String method) {
        this.controller = controller;
        this.method = method;
    }

    @SuppressWarnings("deprecation")
    public Object execMethod(HttpServletRequest req) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ServletException {
        try {
            Class<?> clazz = Class.forName(controller);
            Method[] methodes = clazz.getDeclaredMethods();
            Method oneMethod = null;

            for(Method meth : methodes) {
                if (meth.getName().equals(method))
                    oneMethod = meth;
            }
            Class<?>[] classes = oneMethod.getParameterTypes();

            try {
                assert oneMethod != null;
                Parameter[] parameters = oneMethod.getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i=0; i<parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(Param.class)) {
                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getAnnotation(Param.class).name()));
                    } else if (parameters[i].isAnnotationPresent(Model.class)) {

                        try {
                            arguments[i] = getMethodObjet(parameters[i], req);
                        } catch (Exception e) {
                            throw new ServletException(e.getMessage());
                        }
                    }
                    else {
                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
                    }
                }

                return oneMethod.invoke(clazz.newInstance(),arguments);
            } catch (Exception e) {
                throw new ServletException(e.getMessage());
            }


        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

    }

    @SuppressWarnings("deprecation")
    Object getMethodObjet(Parameter parameter, HttpServletRequest req) throws ServletException {
        Class<?> classeParametre = parameter.getType();
        Object objet = null;
        try {
            try { objet = classeParametre.newInstance(); } catch (Exception e) { throw new ServletException("Pas de constructeur par defaut"); }

            Map<String, String[]> parameterMap = req.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                Method[] listeMethode = classeParametre.getDeclaredMethods();
                Method setter = null;
                for(Method meth : listeMethode) {

                    if (meth.getName().equals("set" + capitalizeFirstLetter(entry.getKey()))) {
                        setter = meth;
                        break;
                    }
                }
                if (setter == null)
                    throw new ServletException("la methode " + "set" + capitalizeFirstLetter(entry.getKey() + "() n'existe pas dans la classe " + objet.getClass()));

                setter.invoke(objet, parse(setter.getParameterTypes()[0],entry.getValue()[0]));
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
        return objet;
    }

    public Object parse(Class<?> clazz, String value) {
        if (clazz.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (clazz.equals(String.class)) {
            return value;
        } else {
            return clazz.cast(value);
        }
    }

    public static String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
