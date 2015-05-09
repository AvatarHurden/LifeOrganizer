package io.github.avatarhurden.lifeorganizer.objects;

import javafx.scene.image.Image;

public enum Status {
	ACTIVE("/style/active.png", "Active"), 
	COMPLETED("/style/completed.png", "Completed"),
	CANCELED("/style/canceled.png", "Canceled");
	
	private Image image;
	private String name;
	
	private Status(String imageUrl, String name) {
		this.image = new Image(imageUrl);
		this.name = name;
	}
	
	public Image getImage() {
		return image;
	}
	
	public String getName() {
		return name;
	}
	
}
