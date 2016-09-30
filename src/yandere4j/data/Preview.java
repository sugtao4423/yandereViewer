package yandere4j.data;

public class Preview{

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
