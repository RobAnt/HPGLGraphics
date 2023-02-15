import hpglgraphics.*;

void setup() {
  size(1104, 772);
  
  stroke(0);
  background(255);
  noFill();
    
  // HPGL (Roland DXY-1350A) coordinate ranges:
  // A4 : 11040 x 7721 (297 x 210 mm)
  // A3 : 16158 x 11040 (420 x 297 mm)
  // HPGL (HP7475A) imperial coordinate ranges:
  // A : 10365 x 7962 (8.5 x 11 in)
  // B : 16640 x 10365 (11 x 17 in)
  
  HPGLGraphics hpgl = (HPGLGraphics) createGraphics(width, height, HPGLGraphics.HPGL);
  
  //hpgl.setPaperSize("A4");  //Still Supported with one parameter but new options:
  
  // First parameter is paper size (as listed above)
  // Second parameter is page orientation:
  //    "LANDSCAPE" (default), "PORTRAIT", "AUTO" (maximizes based on dimension of the HPGLGraphics declaration
  // Third parameter is the alignment on the page (Bottom/Center/Top Left/Center/Right):
  //    "BL","BC","BR","TL","TC","TR","CL","CC","CR"
  
  //hpgl.setPaperSize("A4","LANDSCAPE","BL");  //is the default settings
  
  hpgl.setPaperSize("A","AUTO","CC");
  
  hpgl.setPath("output.hpgl");
  
  // (most) things between begin- and endRecord are written to the .hpgl file
  beginRecord(hpgl);
  
  rect(200, 200, 200, 200);
  ellipse(800, 300, 200, 200);
  
  pushMatrix();
  translate(150, 100);
  int cellsize=64;
  rect(0, 0, cellsize, cellsize);
  
  for (int i=0; i<cellsize; i+=4) {
    line(0, i, cellsize, i); 
  }

  for (int j=0; j<cellsize; j+=4) {
    line(j,0,j,cellsize); 
  }
  translate(-100, 0);
  
  rect(0,0, cellsize, cellsize);
  
  for (int i=0; i<cellsize; i+=4) {
    line(0, i, cellsize, i); 
  }

  for (int j=0; j<cellsize; j+=4) {
    line(j,0,j,cellsize); 
  }
  
  popMatrix();



  pushMatrix();
  translate(50, 330);
  
  line(0,0,40,0);
  line(0,1,40,1);
  line(0,2,40,2);
  line(0,3,40,3);
  line(0,4,40,4);
  line(0,5,40,5);
  
  line(0,15,40,15);
  line(0,20,40,20);
  line(0,25,40,25);
  line(0,30,40,30);
  line(0,35,40,35);
    
  line(0,50,40,95);
  line(0,55,40,100);
  line(0,60,40,105);
  line(0,65,40,110);
  line(0,70,40,115);
  
  popMatrix();


  endRecord();
  
}

void draw() {

}
