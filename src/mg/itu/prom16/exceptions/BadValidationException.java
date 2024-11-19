package mg.itu.prom16.exceptions;

import java.util.Map;

public class BadValidationException extends Exception {

    String referer;
    Map<String, String> erreurs;

    public BadValidationException(String message) {
        super(message);
    }

    public BadValidationException(String message, String referer) {
        super(message);
        this.referer = referer;
    }

    public BadValidationException(Map<String, String> erreurs) {
        this.referer = referer;
        this.erreurs = erreurs;
    }

    public Map<String, String> getErreurs() {
        return erreurs;
    }

    public void setErreurs(Map<String, String> erreurs) {
        this.erreurs = erreurs;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
