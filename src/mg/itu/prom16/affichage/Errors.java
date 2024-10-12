package mg.itu.prom16.affichage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class Errors extends Exception {
    int statusCode;
    String errorMessage;

    public Errors(int statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public void returnError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(statusCode);
        PrintWriter out = resp.getWriter();

        out.println("Status code : " + statusCode);
        out.println("Error : " + errorMessage);
    }


}
