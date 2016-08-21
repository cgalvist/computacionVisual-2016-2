package taller2;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

public class Taller2 extends PApplet{
    
    PImage img, zoom;
    
    int escala = 5;
    int ancho = 900, alto = 700;
    int anchoZoom = (ancho / escala), altoZoom = (alto / escala);
    float numZoom = 1.0f, rotacion = 0;
    
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
        
        rotate(rotacion);
        scale(numZoom);
        image(zoom, 0, 0, ancho, alto);
        
        rotate(-rotacion);
        scale(1 / numZoom);
        image(img, 0, 0, anchoZoom, altoZoom);
        
        if(mouseX > anchoZoom || mouseY > altoZoom || mouseX < 0 || mouseY < 0){
            numZoom = 1;
            rotacion = 0;
        } else {
            zoom = img.get(mouseX * (escala), mouseY * (escala),ancho,alto);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent event) {
        if(mouseButton == LEFT){
            rotacion += PI / 16.0f;
        }
        if(mouseButton == RIGHT){
            rotacion += - PI / 16.0f;
        }
    }
    
    @Override
    public void mouseWheel(MouseEvent event) {
        if(numZoom > 0.05f){
            if(event.getCount() == -1){
                numZoom += 0.05f;
            } else {
                numZoom -= 0.05f;
            }
        } else {
            numZoom = 0.051f;
        }
    }
    
    public static void main(String[] args) {
        String name = Taller2.class.getName();
        PApplet.main(name);
    }
    
}