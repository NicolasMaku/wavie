package mg.itu.prom16;

import jakarta.servlet.http.HttpServletRequest;

public interface RoleHandler {

    public boolean isAllowedUser(HttpServletRequest req , String[] roles);

    public boolean isAuthenticated(HttpServletRequest req);
    public String getRole(HttpServletRequest req);

}
