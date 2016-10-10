// bandera usando splines

/*
int[] coords = {
  100, 10, 125, 17, 150, 20, 175, 17, 200, 10
};
*/
int[] coords = {
  100, 10, 
  110, 12, 
  120, 14, 
  130, 16, 
  140, 17,
  150, 18,
  160, 20,
  170, 23,
  180, 26,
  190, 23, 
  200, 20, 
  210, 18, 
  220, 17, 
  230, 16,
  240, 14,
  250, 12,
  260, 10,
};

int[] coordsTemp, coords2;

float temp = PI/5.0;
int numLineas = 4, distLineas = 50;
int indX = -1, indY = -1;
int pixelesInterval = 5;
int corrimientoY = 100;
boolean movimiento = true;


void setup() {
  size(400, 400);
  background(255);
  smooth();
  noFill();
  
  coordsTemp = coords.clone();
  coords2 = coords.clone();
}

void draw(){
  clear();
  background(255);
  
  
  // calcular el cambio del eje Y con el paso del tiempo
  if(movimiento){
    for (int i = 1; i < coords.length; i+=2) {
      coordsTemp[i] = Math.round((float) coords2[i] * (sin(temp + (i/4.0))) + corrimientoY);
      //System.out.print(coordsTemp[i] + ", ");
    }
  }
  //System.out.println();
  
  /*
  for (int i = 0; i < coords.length; i++) {
    System.out.print(coordsTemp[i] + ", ");
  }
  System.out.println();
  */
  
  noFill();
  stroke(0);
  dibujarCuadros(coordsTemp);
  stroke(0, 200, 0);
  dibujarSpline(coordsTemp);
      
  fill(255, 0, 0);
  noStroke();
  dibujarPuntos(coordsTemp);
  
  temp = (temp + PI/10.0) % TWO_PI;
  
  delay(100);
}

void dibujarSpline(int[] coords){
  for (int j = 0; j < numLineas; j++) {
    beginShape();
    for (int i = 0; i < coords.length; i += 2) {
      if(i == 0 || i == coords.length - 2){
        curveVertex(coords[i], coords[i + 1] + (j * distLineas));
      }
      curveVertex(coords[i], coords[i + 1] + (j * distLineas));
    }
    endShape();
  }
}

void dibujarPuntos(int[] coords){
  for (int j = 0; j < numLineas; j++) {
    for (int i = 0; i < coords.length; i += 2) {
      ellipse(coords[i], coords[i + 1] + (j * distLineas), 3, 3);
      //System.out.print("(" + coords[i] + "," + (coords[i + 1] + (j * distLineas)) + ")");
    }
    //System.out.println();
  }
}

void dibujarCuadros(int[] coords){
  
  int x, y, xAncho, yAncho;
  
  for (int j = 0; j < numLineas - 1; j++) {
    for (int i = 0; i < coords.length; i += 2) {
      
      if((i + 3) <= coords.length){
        x = coords[i];
        y = coords[i + 1] + (j * distLineas);
        xAncho = coords[i + 2] - x;
        yAncho = coords[i + 1] + ((j + 1) * distLineas) - y;
        
        rect(x, y, xAncho, yAncho);
      }
    }
  }
}

@Override
public void mousePressed() {
  
  if (mouseButton == LEFT){
    for(int i = 0; i < coordsTemp.length; i += 2){
      //System.out.println(i + ", " + coordsTemp[i]);
      if(coordsTemp[i] > (mouseX - pixelesInterval) && 
          coordsTemp[i] < (mouseX + pixelesInterval)){
        indX = i;
      }
      if(coordsTemp[i + 1] > (mouseY - pixelesInterval) && 
          coordsTemp[i + 1] < (mouseY + pixelesInterval) &&
          i == indX){
        indY = i + 1; 
      }
      //System.out.println(indX + ", " + indY);
      /*
      if((coordsTemp[i + 1] % distLineas) > ((mouseY - pixelesInterval) % distLineas) && 
          (coordsTemp[i + 1] % distLineas) < ((mouseY + pixelesInterval) % distLineas)){
        indY = i + 1; 
      }
      */
    }
  }
  
  // reiniciar bandera
  if (mouseButton == RIGHT){
    coordsTemp = coords.clone();
    coords2 = coords.clone();
  }
  
  // iniciar/detener movimiento
  if (mouseButton == CENTER){
    movimiento = !movimiento;
  }
}

@Override
public void mouseDragged() {
  if (mouseButton == LEFT){
    if(indX >= 0 && indY >= 0){
      coordsTemp[indX] = mouseX;
      coordsTemp[indY] = mouseY;
      coords2[indY] = mouseY - corrimientoY;
    }
  }  
}

@Override
public void mouseReleased(){
  indX = indY = -1; 
}