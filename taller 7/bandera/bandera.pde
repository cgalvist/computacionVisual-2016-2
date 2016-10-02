// bandera usando splines

/*
int[] coords = {
  100, 10, 125, 17, 150, 20, 175, 17, 200, 10
};
*/
int[] coords = {
  100, 10, 
  125, 12, 
  150, 14, 
  175, 16, 
  200, 17,
  225, 18,
  250, 20,
  275, 23,
  300, 26,
  325, 23, 
  350, 20, 
  375, 18, 
  400, 17, 
  425, 16,
  450, 14,
  475, 12,
  500, 10,
};
int[] coordsTemp;

float temp = PI/5.0;
int numLineas = 5, distLineas = 50;

void setup() {
  size(600, 400);
  background(255);
  smooth();
  noFill();
  
  coordsTemp = coords.clone();
}

void draw(){
  clear();
  background(255);
  
  
  // calcular el cambio del eje y con el paso del tiempo
  for (int i = 1; i < coords.length; i+=2) {
    coordsTemp[i] = Math.round((float) coords[i] * (sin(temp + (i/4))) + 100);
    //System.out.print(coordsTemp[i] + ", ");
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
  fill(255, 0, 0);
  dibujarSpline(coordsTemp);
      
  fill(255, 0, 0);
  noStroke();
  dibujarPuntos(coordsTemp);
  
  temp = (temp + PI/5.0) % TWO_PI;
  
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