package mg.itu.prom16.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class MyJson {
    private Gson gson;

    public MyJson() {
        GsonBuilder gbuilder = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateJsonController());
        this.gson = gbuilder.create();
    }

    public Gson getGson() {
        return gson;
    }
}
