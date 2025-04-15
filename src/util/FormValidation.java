package util;

import mg.itu.prom16.annotations.verification.DateFormat;
import mg.itu.prom16.annotations.verification.Numeric;
import mg.itu.prom16.annotations.verification.Required;
import mg.itu.prom16.annotations.verification.Size;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormValidation {
    Map<String, String> errors;
    Map<String, String[]> formData;

    public static boolean isValid(Object o) throws IllegalAccessException {
        if (o == null) return false;
        Field[] fields = o.getClass().getDeclaredFields();

        Map<String, String> validationList = new HashMap<>();

        for (Field field: fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            field.setAccessible(true);

            List<String> erreurs = new ArrayList<>();

            for (Annotation annot : annotations) {
                System.out.println(annot.getClass().getSimpleName());
                if (annot instanceof DateFormat) {
                    String date = (String) field.get(o);
                    String format = ((DateFormat) annot).format();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    try {
                        LocalDate daty = LocalDate.parse(date, formatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("Le format de la date est fausse");
                        erreurs.add("Le format de la date est fausse");
                    }
                } else if (annot instanceof Required) {
                    if (field.get(o) == null || (field.getType().equals(String.class) && field.get(o) == "")) {
                        System.out.println("Le champs " + field.getName() + " est requis.");
                        erreurs.add("Le champs " + field.getName() + " est requis.");
                    }
                } else if (annot instanceof Numeric) {
                    try {
                        String number = (String) field.get(o);
                        Double.parseDouble(number);
                    } catch (Exception e) {
                        System.out.println("Le champs " + field.getName() + " ne respcte pas sa nature Numeric.");
                        erreurs.add("Le champs " + field.getName() + " ne respcte pas sa nature Numeric.");
                    }
                } else if (annot instanceof Size) {
                    String texte = (String) field.get(o);
                    Size size = ((Size) annot);
                    System.out.println("Chhhhhhhhhhhhhhhhhhhhhhhheckkkkkkkkkkkkkkkkkkkkkkkk");
                    if (texte.length() < size.min() || texte.length() > size.max()) {
                        System.out.println("Le champs " + field.getName() + " ne respcte pas la taille imposee : " + size.min() + " < size <" + size.max() + ".");
                        erreurs.add("Le champs " + field.getName() + " ne respcte pas la taille imposee : " + size.min() + " < size <" + size.max() + ".");
                    }
                }
            }

            String inputName = field.getName();
            if (!erreurs.isEmpty())
                validationList.put(inputName ,String.join("; ", erreurs));
        }

        if (validationList.isEmpty()) return true;
        else return false;
    }
}
