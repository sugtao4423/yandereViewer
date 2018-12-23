package yandere4j.data;

import java.io.Serializable;

public class Jpeg extends Sample implements Serializable{

	private static final long serialVersionUID = 897264894844057780L;

	public Jpeg(String url, int size, int width, int height){
		super(url, size, width, height);
	}
}
