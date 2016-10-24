package tallerPainterAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import static processing.javafx.PSurfaceFX.PApplicationFX.surface;


import remixlab.proscene.Scene;

public class PaintersAlgorithm extends PApplet {

	private Random random;
	private Scene scene;
	private TestComparator testComparator;
	private List<PShape> shapes;
	private boolean enableZBuffer;

	@Override
	public void settings() {
		size(640, 480, P3D);
	}

	@Override
	public void setup() {
		random = new Random();
		scene = new Scene(this);
		testComparator = new TestComparator(scene.camera());

		shapes = new ArrayList<>();
		int numx = 4, numy = 4, numz = 4;
		for (int i = 0; i < numx; i++) {
			for (int j = 0; j < numy; j++) {
				for (int k = 0; k < numz; k++) {
					int x = 20 * (i - numx / 2);
					int y = 20 * (j - numy / 2);
					int z = 20 * (k - numz / 2);
					createPyramid(x, y, z);
				}
			}
		}

		enableZBuffer = true;
		surface.setTitle("ENABLE_DEPTH_TEST");
		hint(ENABLE_DEPTH_TEST);

		noStroke();
	}

	public void createPyramid(int x, int y, int z) {
		PVector p1 = new PVector(x + random.nextInt(20), y + random.nextInt(10), z);
		PVector p2 = new PVector(x + 10 + random.nextInt(10), y + 10 + random.nextInt(10), z);
		PVector p3 = new PVector(x + random.nextInt(10), y + 10 + random.nextInt(10), z);
		PVector p4 = new PVector(x + random.nextInt(20), y + random.nextInt(20), z + 1 + random.nextInt(10));

		PShape face1 = createShape();
		face1.beginShape(TRIANGLE);
		face1.vertex(p1.x, p1.y, p1.z);
		face1.vertex(p2.x, p2.y, p2.z);
		face1.vertex(p3.x, p3.y, p3.z);
		face1.endShape();
		shapes.add(face1);

		PShape face2 = createShape();
		face2.beginShape(TRIANGLE);
		face2.vertex(p1.x, p1.y, p1.z);
		face2.vertex(p2.x, p2.y, p2.z);
		face2.vertex(p4.x, p4.y, p4.z);
		face2.endShape();
		shapes.add(face2);

		PShape face3 = createShape();
		face3.beginShape(TRIANGLE);
		face3.vertex(p1.x, p1.y, p1.z);
		face3.vertex(p3.x, p3.y, p3.z);
		face3.vertex(p4.x, p4.y, p4.z);
		face3.endShape();
		shapes.add(face3);

		PShape face4 = createShape();
		face4.beginShape(TRIANGLE);
		face4.vertex(p2.x, p2.y, p2.z);
		face4.vertex(p4.x, p4.y, p4.z);
		face4.vertex(p3.x, p3.y, p3.z);
		face4.endShape();
		shapes.add(face4);
	}

	@Override
	public void draw() {
		background(0);
		Collections.shuffle(shapes);
		Collections.sort(shapes, testComparator);

		for (PShape shape : shapes) {
			shape(shape);
		}
	}

	@Override
	public void keyPressed() {
		enableZBuffer = !enableZBuffer;
		if (enableZBuffer) {
			surface.setTitle("ENABLE_DEPTH_TEST");
			hint(ENABLE_DEPTH_TEST);
		} else {
			surface.setTitle("DISABLE_DEPTH_TEST");
			hint(DISABLE_DEPTH_TEST);
		}
	}

	public static void main(String args[]) {
            String name = PaintersAlgorithm.class.getName();
		PApplet.main(name);
	}

}