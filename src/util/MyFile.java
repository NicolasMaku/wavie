package util;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@MultipartConfig
public class MyFile {
    String filename;
    InputStream inputStream;

    public void sauvegarder(String filepath) {
        File saveFile = new File(filepath);

        try (InputStream fileContent = inputStream; FileOutputStream outputStream = new FileOutputStream(filepath)){

                // Lire les données du fichier et les écrire dans le fichier de destination
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileContent.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
