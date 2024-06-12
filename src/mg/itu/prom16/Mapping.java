package mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.Param;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.http.HttpRequest;
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
        Class<?> clazz = Class.forName(controller);
        Method[] methodes = clazz.getDeclaredMethods();
        Method oneMethod = null;

        for(Method meth : methodes) {
            if (meth.getName().equals(method))
                oneMethod = meth;
        }
        Class<?>[] classes = oneMethod.getParameterTypes();

        assert oneMethod != null;
        Parameter[] parameters = oneMethod.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Param.class)) {
                arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getAnnotation(Param.class).name()));
            } else {
                arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
            }
        }

        return oneMethod.invoke(clazz.newInstance(),arguments);

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
}
