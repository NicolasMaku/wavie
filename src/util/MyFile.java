package util;

import jakarta.servlet.annotation.MultipartConfig;

import java.io.*;

@MultipartConfig
public class MyFile {
    String filename;
    InputStream inputStream;

    public void sauvegarder(String filepath) {
        String filePos = filepath + "/" + filename;
        File saveFile = new File(filePos);
        if (!saveFile.exists()) {
            try {
                System.out.println("Creer file ---------------------------------------------");
                saveFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (InputStream fileContent = inputStream; FileOutputStream outputStream = new FileOutputStream(filePos)){

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

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
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
