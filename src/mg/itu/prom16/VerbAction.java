package mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mg.itu.prom16.affichage.Errors;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Param;
import mg.itu.prom16.annotations.Restapi;
import mg.itu.prom16.annotations.verification.*;
import mg.itu.prom16.annotations.verification.RequestWrapper.MethodChangingRequestWrapper;
import mg.itu.prom16.exceptions.BadValidationException;
import mg.itu.prom16.serializer.MyJson;
import util.CustomSession;
import util.MyFile;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@MultipartConfig
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
                    String argumentName = parameters[i].getAnnotation(Param.class).name();
                    if (classes[i] == MyFile.class) {
                        String path = req.getServletContext().getContextPath() + "/files";

                        MyFile file = new MyFile();
                        Part part = req.getPart(argumentName);
                        file.setFilename(extractFileName(part));
                        file.setInputStream(part.getInputStream());

                    } else arguments[i] = parse(classes[i] ,req.getParameter(argumentName));
                } else if (parameters[i].isAnnotationPresent(Model.class)) {

                    try {
                        arguments[i] = getMethodObjet(parameters[i], req);
                    } catch (BadValidationException e) {
                        System.out.println("ASKIP : " + e.getErreurs().size());
                        req.setAttribute("bad-validation", e.getErreurs());
                        req.setAttribute("formDataValidation", req.getParameterMap());
                        req.setAttribute("validationException", e);

                        System.out.println("LIMONADE");
//                        if (req.getMethod().equalsIgnoreCase("POST")) {
//                            HttpServletRequest wrappedRequest = new MethodChangingRequestWrapper(req, "GET");
//                            RequestDispatcher requestDispatcher = req.getRequestDispatcher(e.getReferer());
//                            requestDispatcher.forward(wrappedRequest, resp);
//                        }

//                        RequestDispatcher requestDispatcher = req.getRequestDispatcher(e.getReferer());
//                        System.out.println("Va vers : " + oneMethod.getAnnotation(ValidationError.class).errorPath());
//                        RequestDispatcher requestDispatcher = req.getRequestDispatcher(oneMethod.getAnnotation(ValidationError.class).errorPath());
//                        requestDispatcher.forward(req, resp);
//                        throw e;

//                            req.getSession().setAttribute("formDataValidation", req.getParameterMap());
//                            req.getSession().setAttribute("badValidation", e.getErreurs());
//                            resp.sendRedirect(e.getReferer());
                    } catch (Exception ex) {
//                        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
                        throw new ServletException(ex.getMessage());
                    }
                } else if (parameters[i].getType().equals(CustomSession.class)) {
                    customSession = new CustomSession(req.getSession());
//                        customSession.fromHttpSession(req.getSession());
                    arguments[i] = customSession;
                } else {
//                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
                    throw new Errors( 500, "ETU002554 existe un argument qui n'est pas annotee");
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
        } catch (Errors er) {
            throw er;
        }
        catch (Exception e) {
            System.out.println("tato");
            throw e;
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
        } else if (clazz.equals(LocalDate.class)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Format de la date

            System.out.println("Said you never hurt me");
            LocalDate localDate = LocalDate.parse(value, formatter);
            return  localDate;
        }
        else {
            return clazz.cast(value);
        }
    }

    @SuppressWarnings("deprecation")
    Object getMethodObjet(Parameter parameter, HttpServletRequest req) throws ServletException, BadValidationException {
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
//                        System.out.println("----------------------------------------------- " + entry.getKey() + " : : " + entry.getValue()[0]);
                        System.out.println("----------------------------------------------- " + prefix + "." + field.getName());
                    }
                }

                if (setter == null) {

                }
                else {
                    try {
                        setter.invoke(objet, parse(setter.getParameterTypes()[0], entry.getValue()[0]));
                    } catch (Exception e) {
                        System.out.println("erreur lors du cast de : " + entry.getKey());
                    }
                }

            }

            verifier(objet, prefix);

            Field[] attributs = classeParametre.getDeclaredFields();
            for (Field attribut : attributs) {
                if (attribut.getType().equals(MyFile.class)) {
                    Method setter = searchMethod(classeParametre, "set" + capitalizeFirstLetter(attribut.getName()));
                    if (setter!=null) {
                        MyFile file = new MyFile();
                        Part part = req.getPart(prefix + "." +attribut.getName());
                        file.setFilename(extractFileName(part));
                        file.setInputStream(part.getInputStream());
                        setter.invoke(objet, file);
                    }
                }

            }

        } catch (BadValidationException e) {
            String referer = req.getHeader("Referer");
            String projectName = req.getServletContext().getContextPath();
            String[] splits = referer.split(projectName);
            referer = splits[1];
//            referer = referer.replace("http://localhost:8081/wavie", "");
            e.setReferer(referer);
            throw e;
        } catch (Exception e) {
            System.out.println("Nicolus");
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

        if (this.verb.equals(v.verb))
            return true;

        if (this.action == v.action)
            return true;

        return false;
    }

    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf('=') + 2, item.length() - 1);
            }
        }
        return "";
    }

    private void verifier(Object obj, String formPrefix) throws BadValidationException, IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();

        Map<String, String> validationList = new HashMap<>();

        for (Field field: fields) {
            System.out.println(field.getName());
            Annotation[] annotations = field.getDeclaredAnnotations();
            field.setAccessible(true);

            List<String> erreurs = new ArrayList<>();

            for (Annotation annot : annotations) {
                System.out.println(annot.getClass().getSimpleName());
                if (annot instanceof DateFormat) {
                    String date = (String) field.get(obj);
                    String format = ((DateFormat) annot).format();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    try {
                        LocalDate daty = LocalDate.parse(date, formatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("Le format de la date est fausse");
                        erreurs.add("Le format de la date est fausse");
                    }
                } else if (annot instanceof Required) {
                    if (field.get(obj) == null) {
                        System.out.println("Le champs " + field.getName() + " est requis.");
                        erreurs.add("Le champs " + field.getName() + " est requis.");
                    }
                } else if (annot instanceof Numeric) {
                    try {
                        String number = (String) field.get(obj);
                        Double.parseDouble(number);
                    } catch (Exception e) {
                        System.out.println("Le champs " + field.getName() + " ne respcte pas sa nature Numeric.");
                        erreurs.add("Le champs " + field.getName() + " ne respcte pas sa nature Numeric.");
                    }
                } else if (annot instanceof Size) {
                    String texte = (String) field.get(obj);
                    Size size = ((Size) annot);
                    if (texte.length() < size.min() || texte.length() > size.max()) {
                        System.out.println("Le champs " + field.getName() + " ne respcte pas la taille imposee : " + size.min() + " < size <" + size.max() + ".");
                        erreurs.add("Le champs " + field.getName() + " ne respcte pas la taille imposee : " + size.min() + " < size <" + size.max() + ".");
                    }
                }
            }

            String inputName = formPrefix + "." + field.getName();
            if (!erreurs.isEmpty())
                validationList.put(inputName ,String.join("; ", erreurs));
        }

        if (!validationList.isEmpty())
            throw new BadValidationException(validationList);


    }
}
