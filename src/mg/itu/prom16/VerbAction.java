package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Param;
import mg.itu.prom16.annotations.Restapi;
import mg.itu.prom16.serializer.MyJson;
import util.CustomSession;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VerbAction extends HashMap<Class<?>, String> {
    Class<?> verb;
    String action;

    public VerbAction(Class<?> verb, String action) {
        this.verb = verb;
        this.action = action;
    }

    public Class<?> getVerb() {
        return verb;
    }

    public void setVerb(Class<?> verb) {
        this.verb = verb;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @SuppressWarnings("deprecation")
    public Object execMethod(HttpServletRequest req, HttpServletResponse resp, String controller) throws Exception {

        System.out.println("interne : " + this.getVerb().getSimpleName());
        System.out.println("http : " + req.getMethod());

        try {
            Class<?> clazz = Class.forName(controller);

            Method[] methodes = clazz.getDeclaredMethods();
            Method oneMethod = null;
            CustomSession customSession = null;

            for(Method meth : methodes) {
                if (meth.getName().equals(getAction()))
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
                    } else if (parameters[i].getType().equals(CustomSession.class)) {
                        customSession = new CustomSession(req.getSession());
//                        customSession.fromHttpSession(req.getSession());
                        arguments[i] = customSession;
                    } else {
//                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
                        throw new ServletException("ETU002554 existe un argument qui n'est pas annotee");
                    }
                }

                Object controllerInstance = clazz.newInstance();

                // tester si la classe controller possede un attribut customSession
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(CustomSession.class)) {
                        customSession = new CustomSession(req.getSession());
                        field.setAccessible(true);
                        field.set(controllerInstance ,customSession);
                    }
                }


                Object retour = null;
                try {
                    retour =  oneMethod.invoke(controllerInstance,arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (oneMethod.isAnnotationPresent(Restapi.class)) {
                    MyJson gson = new MyJson();
                    retour = gson.getGson().toJson(retour);
                    resp.setContentType("application/json");
                }

                return retour;
            } catch (Exception e) {
                System.out.println("tato");
                throw new ServletException(e.getMessage());
            }


        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

    }

    public Object parse(Class<?> clazz, String value) throws ServletException {
        if (clazz.equals(int.class)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ServletException("Veuiller entrer un nombre valide");
            }

        } else if (clazz.equals(String.class)) {
            if (value.equals(""))
                return "null";
            return value;
        } else if (clazz.equals(Date.class)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = sdf.parse(value);
            } catch (ParseException e) {
                throw new ServletException("Le format de la date est fausse");
            }
            return date;
        } else {
            return clazz.cast(value);
        }
    }

    @SuppressWarnings("deprecation")
    Object getMethodObjet(Parameter parameter, HttpServletRequest req) throws ServletException {
        Class<?> classeParametre = parameter.getType();
        String prefix = parameter.getAnnotation(Model.class).value();
        Field[] fields = classeParametre.getDeclaredFields();
        Object objet = null;
        try {
            try { objet = classeParametre.newInstance(); } catch (Exception e) { throw new ServletException("Pas de constructeur par defaut"); }

            Map<String, String[]> parameterMap = req.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {

                Method setter = null;
                for(Field field: fields) {
                    if (( prefix + "." + field.getName()).equals(entry.getKey())) {
                        setter = searchMethod(classeParametre, "set" + capitalizeFirstLetter(field.getName()));
                    }
                }

                if (setter == null) {

                }
                else
                    setter.invoke(objet, parse(setter.getParameterTypes()[0],entry.getValue()[0]));
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
        return objet;
    }

    Method searchMethod(Class<?> clazz, String methodName) {
        Method[] listeMethode = clazz.getDeclaredMethods();
        for(Method meth : listeMethode) {

            if (meth.getName().equals(methodName)) {
                return meth;
            }
        }

        return null;
    }

    public static String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), verb, action);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VerbAction v))
            return false;

        if (this.verb.equals(v.verb) && Objects.equals(this.action, v.action))
            return true;

        if (this.action.equals(v.action))
            return true;

        return false;
    }
}
