package taller6;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PShape;
import remixlab.proscene.Scene;

//HSR
public class HiddenSurfaceRemoval extends PApplet {

	private Scene scene, auxScene;
	private List<PShape> shapes;

	@Override
	public void settings() {
		size(640, 720, P3D);
	}

	public void setup() {
		frameRate(30);

		scene = new Scene(this, createGraphics(640, 360, P3D));
		scene.enableBoundaryEquations();

		auxScene = new Scene(this, createGraphics(640, 360, P3D), 0, 360);
		auxScene.setRadius(500);
		auxScene.showAll();

		shapes = buildComplexScene();
		// Vec p1 = new Vec(-500, -250, -250);
		// Vec p2 = new Vec(500, 250, 250);
		// TODO: To build Octree

		// TODO: Back-Face Culling
		// scene.camera().isFaceFrontFacing(arg0, arg1);

		// TODO: View Frustum Culling
		// scene.camera().boxVisibility(arg0, arg1);
	}

	public List<PShape> buildComplexScene() {
		List<PShape> shapes = new LinkedList<PShape>();

		Random random = new Random();
		int numx = 50, numy = 25;
		for (int i = 0; i < numx; i++) {
			for (int j = 0; j < numy; j++) {
				int x = 20 * (i - numx / 2);
				int y = 20 * (j - numy / 2);
				shapes.addAll(createBox(x, y, 0, 10, random.nextInt(20) + 5));
			}
		}

		return shapes;
	}

	public List<PShape> createBox(int x, int y, int z, int w, int h) {
		// For closed polygons, use normal vectors facing outward
		PShape quad1 = createShape();
		quad1.beginShape(QUAD);
		quad1.vertex(x, y + w, z + h);
		quad1.vertex(x, y, z + h);
		quad1.vertex(x + w, y, z + h);
		quad1.vertex(x + w, y + w, z + h);
		quad1.endShape();

		PShape quad2 = createShape();
		quad2.beginShape(QUAD);
		quad2.vertex(x + w, y, z + h);
		quad2.vertex(x + w, y, z);
		quad2.vertex(x + w, y + w, z);
		quad2.vertex(x + w, y + w, z + h);
		quad2.endShape();

		PShape quad3 = createShape();
		quad3.beginShape(QUAD);
		quad3.vertex(x + w, y + w, z);
		quad3.vertex(x, y + w, z);
		quad3.vertex(x, y + w, z + h);
		quad3.vertex(x + w, y + w, z + h);
		quad3.endShape();

		PShape quad4 = createShape();
		quad4.beginShape(QUAD);
		quad4.vertex(x, y + w, z);
		quad4.vertex(x + w, y + w, z);
		quad4.vertex(x + w, y, z);
		quad4.vertex(x, y, z);
		quad4.endShape();

		PShape quad5 = createShape();
		quad5.beginShape(QUAD);
		quad5.vertex(x, y + w, z + h);
		quad5.vertex(x, y + w, z);
		quad5.vertex(x, y, z);
		quad5.vertex(x, y, z + h);
		quad5.endShape();

		PShape quad6 = createShape();
		quad6.beginShape(QUAD);
		quad6.vertex(x, y, z);
		quad6.vertex(x + w, y, z);
		quad6.vertex(x + w, y, z + h);
		quad6.vertex(x, y, z + h);
		quad6.endShape();

		List<PShape> box = new LinkedList<PShape>();
		box.add(quad1);
		box.add(quad2);
		box.add(quad3);
		box.add(quad4);
		box.add(quad5);
		box.add(quad6);
		return box;
	}

	public void draw() {
		handleMouse();
		surface.setTitle("Frames: " + frameRate);

		scene.pg().beginDraw();
		scene.beginDraw();
		mainDrawing(scene);
		scene.endDraw();
		scene.pg().endDraw();
		image(scene.pg(), 0, 0);

		auxScene.pg().beginDraw();
		auxScene.beginDraw();
		mainDrawing(auxScene);

		auxScene.pg().pushStyle();
		auxScene.pg().stroke(255, 255, 0);
		auxScene.pg().fill(255, 255, 0, 160);
		auxScene.drawEye(scene.eye());
		auxScene.pg().popStyle();

		auxScene.endDraw();
		auxScene.pg().endDraw();
		image(auxScene.pg(), auxScene.originCorner().x(), auxScene.originCorner().y());
	}

	public void mainDrawing(Scene s) {
		s.pg().background(0);

		for (PShape shape : shapes) {
			s.pg().shape(shape);
		}
	}

	public void handleMouse() {
		if (mouseY < 360) {
			scene.enableMotionAgent();
			scene.enableKeyboardAgent();
			auxScene.disableMotionAgent();
			auxScene.disableKeyboardAgent();
		} else {
			scene.disableMotionAgent();
			scene.disableKeyboardAgent();
			auxScene.enableMotionAgent();
			auxScene.enableKeyboardAgent();
		}
	}

	public static void main(String[] args) {
		PApplet.main(HiddenSurfaceRemoval.class.getCanonicalName());
	}

}
