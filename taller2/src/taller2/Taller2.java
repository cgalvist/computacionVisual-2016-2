package taller2;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

public class Taller2 extends PApplet{
    
    PImage img, zoom;
    
    int ancho = 1000, alto = 800;
    final int anchoZoom = (ancho / 5), altoZoom = (alto / 5);
    float numZoom = 1.0f;
    
    @Override
    public void settings(){
        size(ancho,alto);
    }

    @Override
    public void setup(){
        img = loadImage("mapa.jpg");
        zoom = loadImage("mapa.jpg");
    }
    
    @Override
    public void draw() {
        background(0);
        
        image(zoom, 0, 0);
        image(img, 0, 0, anchoZoom, altoZoom);
        
        if(mouseX > anchoZoom || mouseY > altoZoom){
            numZoom = 1;
        } else {
//            zoom = img.get(mouseX,mouseY,Math.round(ancho * numZoom),Math.round(alto * numZoom));
            zoom = img.get(mouseX * (ancho / anchoZoom),mouseY * (alto / altoZoom),ancho,alto);
//            scale(numZoom);
            zoom.resize(Math.round(ancho * numZoom),Math.round(alto * numZoom));
        }
        
        image(zoom, 0, 0);
        image(img, 0, 0, anchoZoom, altoZoom);
    }
    
//    @Override
//    public void mousePressed() {
//    }
    
    @Override
    public void mouseWheel(MouseEvent event) {
        if(numZoom >= 1){
            if(event.getCount() == -1){
                numZoom += 0.5;
            } else {
                numZoom -= 0.5;
            }
        } else {
            numZoom = 1;
        }
    }
    
    public static void main(String[] args) {
        String name = Taller2.class.getName();
        PApplet.main(name);
    }
    
}