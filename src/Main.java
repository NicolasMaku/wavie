import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        File dir = new File(cl.getResource("mg/itu/prom16/").getPath().replace("%20"," "));
        File[] files = dir.listFiles();

        assert files != null;
        for(File file : files) {
            if(file.isFile() && file.getName().endsWith(".class")) {
                System.out.println(file.getName());
            }
            System.out.println(file.getName());
        }
    }
}