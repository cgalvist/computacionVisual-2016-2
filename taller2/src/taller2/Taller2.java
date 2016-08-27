package taller2;

import java.awt.Point;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

public class Taller2 extends PApplet {

	PImage img;
	PImage zoomImage;
	Float escalaRect;
	Float rotationRect;

	Point posicion;

	@Override
	public void settings() {
		size(800, 800);
	}

	@Override
	public void setup() {
		img = loadImage("mapa.jpg");
		posicion = new Point(0, 0);
		escalaRect = 1.0f;
		rotationRect = 0.0f;
	}

	@Override
	public void draw() {
		background(0);
		pushMatrix();
		translate(width/2, height/2);
		rotate(radians(rotationRect));
		zoomImage = img.get((int) posicion.getX(), (int) posicion.getY(), (int) ((40 * escalaRect) * 7),
				(int) ((40 * escalaRect) * 7));
		image(zoomImage, -width/2, -height/2, 800, 800);
		popMatrix();

		image(img, 0, 0, img.width / 7, img.height / 7);

		if ((mouseX < (img.width / 7) - (20 * escalaRect) && mouseX > 0 + (20 * escalaRect))
				&& (mouseY > 0 + (20 * escalaRect) && mouseY < (img.height / 7) - (20 * escalaRect))) {

			posicion.setLocation((mouseX - (20 * escalaRect)) * 7, (mouseY - (20 * escalaRect)) * 7);
			
			pushMatrix();
			fill(255, 255, 255, 150);
			translate(mouseX, mouseY);
			rotate(radians(rotationRect));
			rect(-20 * escalaRect, -20 * escalaRect, (40 * escalaRect), 40 * escalaRect);
			popMatrix();

		}

	}

	@Override
	public void mousePressed() {

		if (mouseButton == LEFT) {
			rotationRect=rotationRect+5.0f;
		} else if (mouseButton == RIGHT) {
			rotationRect=rotationRect-5.0f;
		}

	}

	@Override
	public void mouseWheel(MouseEvent event) {
		if ((mouseX < (img.width / 7) - (20 * escalaRect) && mouseX > 0 + (20 * escalaRect))
				&& (mouseY > 0 + (20 * escalaRect) && mouseY < (img.height / 7) - (20 * escalaRect))) {

			if (event.getCount() < 0 && (escalaRect * 40) < (img.height / 7) - 50) {
				escalaRect = escalaRect + 0.1f;
			} else if (event.getCount() > 0 && escalaRect > 0.2) {
				escalaRect = escalaRect - 0.1f;
			}

		}
	}

	public static void main(String[] args) {
		String name = Taller2.class.getName();
		PApplet.main(name);
	}

}