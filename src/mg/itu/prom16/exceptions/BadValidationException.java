package mg.itu.prom16.exceptions;

public class BadValidationException extends Exception {

    String referer;

    public BadValidationException(String message) {
        super(message);
    }

    public BadValidationException(String message, String referer) {
        super(message);
        this.referer = referer;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
