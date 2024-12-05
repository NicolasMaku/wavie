package mg.itu.prom16.annotations.verification.RequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class MethodChangingRequestWrapper extends HttpServletRequestWrapper {
    private final String newMethod;

    public MethodChangingRequestWrapper(HttpServletRequest request, String newMethod) {
        super(request);
        this.newMethod = newMethod;
    }

    @Override
    public String getMethod() {
        return newMethod;
    }
}
