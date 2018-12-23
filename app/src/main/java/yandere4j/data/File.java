package yandere4j.data;

public class File extends Sample{

    private static final long serialVersionUID = 3275976360293037553L;

    private String ext;

    public File(String url, String ext, int size, int width, int height){
        super(url, size, width, height);
        this.ext = ext;
    }

    public String getExt(){
        return ext;
    }

}
