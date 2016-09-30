package yandere4j.data;

public class Sample extends Preview{

	private int size;

	public Sample(String url, int size, int width, int height){
		super(url, width, height);
		this.size = size;
	}

	public int getSize(){
		return size;
	}
}
