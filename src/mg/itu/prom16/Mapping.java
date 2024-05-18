package mg.itu.prom16;

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
}
