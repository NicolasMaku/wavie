package mg.itu.prom16;

import mg.itu.prom16.affichage.Errors;
import mg.itu.prom16.exceptions.BadValidationException;

import java.util.HashMap;

public class ModelView {
    String url;
    HashMap<String, Object> data = new HashMap<>();

    String ErrorUrl;
    BadValidationException validationException = null;

    public String getErrorUrl() {
        return ErrorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        ErrorUrl = errorUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ModelView(String url) {
        this.url = url;
    }

    public ModelView() {
    }

    public void addObject(String cle, Object value) {
        data.put(cle,value);
    }

    public Object getAttribute(String cle) {
        return data.get(cle);
    }

    public boolean estValide() {
        return validationException == null;
    };

}
