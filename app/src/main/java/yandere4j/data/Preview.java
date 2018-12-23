package yandere4j.data;

import java.io.Serializable;

public class Preview implements Serializable{

    private static final long serialVersionUID = -7395446019135027666L;

    private String url;
    private int width;
    private int height;

    public Preview(String url, int width, int height){
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl(){
        return url;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

}
