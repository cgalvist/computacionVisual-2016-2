/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taller1;

/**
 *
 * @author cisco
 */
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Estudiante
 */
public class Taller1 extends PApplet{
    
    PImage img;
    int redLinea;
    int greenLinea;
    int blueLinea;
    
    boolean mouseDrag = false;
    
    ArrayList<Point> rayones;
    ArrayList<Color> rayonesColor;
    
    Color colorAux;
    Point pointAux;
    
    @Override
    public void settings(){
        size(1000,750);
    }

    @Override
    public void setup(){
        rayones = new ArrayList<Point>();
        rayonesColor = new ArrayList<Color>();
        img = loadImage("paisaje.jpg");
    }
    
    @Override
    public void draw (){
        image(img, 0, 0);
        
        for(int i=0;i<rayones.size();i++){
            colorAux = rayonesColor.get(i);
            pointAux = rayones.get(i);
            strokeWeight(10);
            stroke(colorAux.getRed(),colorAux.getGreen(),colorAux.getBlue());
            point((float)rayones.get(i).getX(),(float)rayones.get(i).getY());
        }
        
        stroke(0,0,0);
        strokeWeight(1);
        fill(255,255,255,200);
        rect(mouseX,mouseY,60,140,10);
        fill(get(mouseX,mouseY),255);
        rect(mouseX+5,mouseY+5,50,40,10);
        
        fill(red(get(mouseX,mouseY)),0,0);
        rect(mouseX+5,mouseY+50,30,20,10);
        fill(0,green(get(mouseX,mouseY)),0);
        rect(mouseX+5,mouseY+80,30,20,10);
        fill(0,0,blue(get(mouseX,mouseY)));
        rect(mouseX+5,mouseY+110,30,20,10);
        
    }
    
    @Override
    public void mousePressed() {
        redLinea = (int)red(get(mouseX,mouseY));
        greenLinea = (int)green(get(mouseX,mouseY));
        blueLinea = (int)blue(get(mouseX,mouseY));
    }
    
    @Override
    public void mouseDragged() 
    {
        rayonesColor.add(new Color(redLinea, greenLinea, blueLinea));
        rayones.add(new Point(mouseX, mouseY));
    }
    
    public static void main(String[] args) {
        String name = Taller1.class.getName();
        PApplet.main(name);
    }
    
}

