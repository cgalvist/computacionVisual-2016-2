// bandera usando splines

int[] coords = {
  100, 10, 125, 17, 150, 20, 175, 17, 200, 10
};

int[] coordsTemp = {
  100, 10, 125, 17, 150, 20, 175, 17, 200, 10
};

float temp = PI/5.0;
int numLineas = 5, distLineas = 20;

void setup() {
  size(300, 300);
  background(255);
  smooth();
  noFill();
}

void draw(){
  clear();
  background(255);
  
  for (int i = 1; i < coords.length; i+=2) {
    coordsTemp[i] = Math.round((float) coords[i] * (sin(temp + (PI/4.0) + i)) + 100);
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
  dibujarSpline(coordsTemp);
      
  fill(255, 0, 0);
  noStroke();
  dibujarPuntos(coordsTemp);
    
  noFill();
  stroke(0);
  dibujarCuadros(coordsTemp);
  
  temp = (temp + PI/5.0) % TWO_PI;
  
  delay(200);
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
      System.out.print("(" + coords[i] + "," + (coords[i + 1] + (j * distLineas)) + ")");
    }
    System.out.println();
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
        yAncho = coords[i + 3] + ((j + 1) * distLineas) - y;
        
        rect(x, y, xAncho, yAncho);
      }
    }
  }
}