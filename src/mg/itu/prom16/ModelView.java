package mg.itu.prom16;

import java.util.HashMap;

public class ModelView {
    String url;
    HashMap<String, Object> data;
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ModelView(String url) {
        this.url = url;
    }

    public ModelView() {
    }

    public void addObject(String cle, Object value) {
        data.put(cle,value);
    }

    public Object getAttribute(String cle) {
        return data.get(cle);
    }

}
