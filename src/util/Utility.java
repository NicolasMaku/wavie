package util;

public class Utility {
    public static String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static String getRedirectController(String redirectStr) {
//        String temp = redirectStr.split("redirect:")
//        System.out.println(redirectStr);
//        return redirectStr.substring(16);
        return redirectStr.replace("redirect:","");
    }
}
