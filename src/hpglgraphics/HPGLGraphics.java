package hpglgraphics;

import java.io.*;

import processing.core.*;

/**
 * This is a library for writing HPGL (plotter) files using beginRecord()/endRecord()
 * Inspired by https://github.com/gregersn/HPGL
 * Borrows from http://phi.lho.free.fr/programming/Processing/P8gGraphicsSVG/
 * the OBJExport library and the Processing DXF library.
 * 
 * (the tag example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 * @example HPGL
 */

public class HPGLGraphics extends PGraphics {

  File file;
  PrintWriter writer;
  public static final String HPGL = "hpglgraphics.HPGLGraphics";
  
  /** Stores all the transformations */
  /* from HPGL by gsn */
  private PMatrix2D transformMatrix;

  private boolean matricesAllocated = false;
  private boolean BEZIER=false;
  
  private String size;  // paper size, A3 or A4 for now
  private int MATRIX_STACK_DEPTH = 32;
  private int transformCount = 0;
  private PMatrix2D transformStack[] = new PMatrix2D[MATRIX_STACK_DEPTH];
  
  private double chordangle = 5.0; // HPGL default is 5 degrees
  private int A4W = 11040;
  private int A4H =  7721;
  private int A3W = 16158;
  private int A3H = 11040;
  
  private double[][] shapeVertices;
	
  public final static String VERSION = "##library.prettyVersion##";

  /**
   * a Constructor, usually called in the setup() method in your sketch to
   * initialize and start the Library.
   * 
   * @example demo
   * 
   */

  public HPGLGraphics(){
	super();
    welcome();
    
    if (!matricesAllocated) {   
      // Init default matrix
      this.transformMatrix = new PMatrix2D();
      matricesAllocated = true;
     }
  }
  
  private void welcome() {
	   System.out.println("##library.name## ##library.prettyVersion## by ##author##");
  }
    
  /**
   * This method sets the plotter output size. Used to scale the Processing sketch to
   * match either A3 or A4 dimensions (only these supported now)
   * 
   * @example demo
   * @param size String: "A3" or "A4", depending on the intended plot size
   */
  
  public void setPaperSize(String size) {
    this.size=size;
  }
  
  /**
   * This method sets the path and filename for the HPGL output.
   * Must be called from the Processing sketch
   * 
   * @example demo
   * @param path String: name of file to save to
   */
  public void setPath(String path) {
    
    this.path = parent.sketchPath(path);
   	if (path != null) {
	     file = new File(this.path);

   	}
	   if (file == null) {
		    throw new RuntimeException("Something went wrong trying to create file "+this.path);
   	}
  }
  
  /**
   * This method selects plotter pen via the HPGL 'SP' instruction.
   * Called from the Processing sketch.
   * 
   * @example HPGL
   * @param pen : integer number of pen to select (depends on plotter)
   */
  public void selectPen(int pen) {
    writer.println("SP"+pen+";");    
  }
  
  public void beginDraw() {
	
   	if (this.path == null) {
   	  throw new RuntimeException("call setPath() before recording begins!");
  	 }
	
	   if (this.size == null) {
		    this.size="A4";
		    System.out.println("setPaperSize undefined: defaulting to A4");
   	}
	
    if (writer == null) {
	     try {
	       writer = new PrintWriter(new FileWriter(file));
	     } catch (IOException e) {
	       throw new RuntimeException(e);  // java 1.4+
	     }
	     writeHeader();
   	}
  }

  public void endDraw(String path) {

    writeFooter();
    writer.flush();
  }

  public void endRecord() {   
      endDraw();
      dispose(); 
  }

  public void endRecord(String filePath) {   
      setPath(filePath);
      endRecord();
  }
  
  public void beginShape() {
	   shapeVertices = new double[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
	   vertexCount = 0;
  }
  
  /**
   * This method sets the chord angle (in degrees) used for drawing arcs, circles and ellipses.
   * 
   * @example shapes
   * @param int kind
   */
  public void beginShape(int kind) {
    shapeVertices = new double[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
    shape = kind;
    vertexCount = 0;
  }
  
  public void endShape() {
    endShape(OPEN);
  }
  
  public void endShape(int mode){
    if (!BEZIER) { // in this case we've handled vertices in bezierVertex()
      int stop = vertexCount - 1;
    
      writer.println("PU" + shapeVertices[0][0] + "," + shapeVertices[0][1] + ";");
    
      for (int i=1; i<=stop; i++){
        writer.println("PD" + shapeVertices[i][0] + "," + shapeVertices[i][1] + ";");
      }
    
      if (mode==CLOSE) {
        // Pen down to starting point
        writer.println("PD" + shapeVertices[0][0] + "," + shapeVertices[0][1] + ";");
      }
    
      writer.println("PU;");
      vertexCount = 0;
      shapeVertices = null;
    }
    BEZIER=false;
  }
  
  /*public void endShape(int mode) {
    System.out.println("endShape, mode: " + mode);
    
   	switch(shape) {
      case TRIANGLE:
        {
          System.out.println("TRIANGLE " + vertexCount);
          for (int i=0; i<vertexCount; i++) {
        	   System.out.println(vertices[i][0]);
        	   System.out.println(vertices[i][1]);
          }
        }
        break;
        
      case QUAD:
	       {
	         System.out.println("QUAD " + vertexCount);
	       }
	       break;
	       
   	} // switch
  }
 */ 
  /*public void shape(PShape s){

    System.out.println("got a shape(): " + s);
  }
  */

  public void vertex(float x, float y) {
	   
    vertexCount++;
    
    // check if shapeVertices is big enough, extend if necessary.
    // via OBJExport (MeshExport.java)
    if(vertexCount >= shapeVertices.length) {
      double newVertices[][] = new double[shapeVertices.length*2][VERTEX_FIELD_COUNT];
      System.arraycopy(shapeVertices,0,newVertices,0,shapeVertices.length);
      shapeVertices = newVertices;
    }
    
    double[] xy = new double[2];
          
    // get the transformed/scaled point
    xy = scaleXY(x, y);
    shapeVertices[vertexCount-1][0] = xy[0];
    shapeVertices[vertexCount-1][1] = xy[1];

  }
  
  public void curveVertex(float x, float y){
    //System.out.println("in curveVertex()");
    
  }
  
  public void bezierVertex(float x2, float y2, float x3, float y3, float x4, float y4){
    // TODO: replace this with interpolation: BZ command is not an HPGL command (at least on Roland)
	  BEZIER=true;
	  double[] xy  = new double[2];
	  double[] xy1 = new double[2];
	  double[] xy2 = new double[2];
	  double[] xy3 = new double[2];
	  double[] xy4 = new double[2];
	  
	  // initial control point comes from the existing vertices (must be at least one)
	  xy1[0] = shapeVertices[vertexCount-1][0];
	  xy1[1] = shapeVertices[vertexCount-1][1];
	  
	  xy2 = scaleXY(x2, y2);
	  xy3 = scaleXY(x3, y3);
	  xy4 = scaleXY(x4, y4);
	  	  
	  for (int i=0; i<=2; i++) {
	    vertexCount++;
	    if (i==0) xy=xy2;
	    if (i==1) xy=xy3;
	    if (i==2) xy=xy4;
	    
	    if(vertexCount >= shapeVertices.length) {
  	      double newVertices[][] = new double[shapeVertices.length*2][VERTEX_FIELD_COUNT];
	      System.arraycopy(shapeVertices,0,newVertices,0,shapeVertices.length);
	      shapeVertices = newVertices;
	    }
	    shapeVertices[vertexCount-1][0] = xy[0];
	    shapeVertices[vertexCount-1][1] = xy[1];
	  }
	  
	  writer.println("PU" + xy1[0] + "," + xy1[1] + ";");
	  writer.println("BZ" + (int)xy2[0] + "," + (int)xy2[1] + "," +  (int)xy3[0] + "," + (int)xy3[1] + "," + (int)xy4[0] + "," + (int)xy4[1] + ";");
	  //writer.println("PU;");
	
  }
  
  // UTILITIES
  
  /**
   * This method returns x,y coordinates converted to plotter coordinates
   * It also flips the y-coordinate to match Processing axis orientation.
   * 
   * @example demo
   * @param float[] xy: A 2-array with the x and y parameters
   */
  private double[] scaleToPaper(double[] xy) {
    
   	double[] xy1 =  new double[xy.length];
    
   	double W=0.0;
   	double H=0.0;
    
    if (this.size == "A3") {
      W=A3W; H=A3H;
    } else if (this.size == "A4"){
      W=A4W; H=A4H;
    }

    // Assume width>height, i.e. always plotting in landscape orientation
    double ratio = H/this.height;
    xy1[0] = ratio * xy[0];
    xy1[1] = ratio * xy[1];
    
    return xy1;
    
  }
  
//  private double map(double val, double minval, double maxval, double minrange, double maxrange) {
//    return (maxrange-minrange)*val / (maxval-minval);
//  }

  private double[] scaleXY(float x, float y){
 	  float[] xy = new float[2];
	   double[] ret = new double[2];
	  
	   double W=0.0;
	   double H=0.0;
	    
	   if (this.size == "A3") {
	     W=A3W; H=A3H;
	   } else if (this.size == "A4"){
	     W=A4W; H=A4H;
	   }
	  
	   this.transformMatrix.mult(new float[]{x,y}, xy);
	  
	   for (int i = 0 ; i < xy.length; i++) {
	     ret[i] = (double) xy[i];
	   }
	  
	   ret = scaleToPaper(ret);
	   ret[1] = H-ret[1];
	   
	   return ret;
  }

  private double[] scaleWH(double w, double h){
   	double[] wh = {w,h};
   	wh = scaleToPaper(wh);
   	return wh;
  }
  
  /**
   * 
   * @return chord angle in degrees
   */
  private double getChordAngle() {
	  return chordangle;
  }
  
  /**
   * This method sets the chord angle (in degrees) used for drawing arcs, circles and ellipses.
   * 
   * @example arc_test
   * @param float ca: chord angle (degrees)
   */
  public void setChordAngle(float ca) {
	   chordangle=ca;
  }
  
  // END UTILITIES
  
  // DRAWING METHODS
  
  public void line(float x1, float y1, float x2, float y2) {

    double[] x1y1 = new double[2];
    double[] x2y2 = new double[2];
      
    x1y1 = scaleXY(x1, y1); // get the transformed/scaled points
    x2y2 = scaleXY(x2, y2); // get the transformed/scaled points
      
    writer.println("PU" + x1y1[0] + "," + x1y1[1] + ";");
    writer.println("PD" + x2y2[0] + "," + x2y2[1] + ";");
    writer.println("PU;");
  }
  
  public void ellipseImpl(float x, float y, float w, float h) {
    
	double[] xy = new double[2];
	double[] initxy = new double[2];
	double[] wh = new double[2];
	double   ca = getChordAngle();
	
    xy = scaleXY(x, y); // get the transformed/scaled points
    wh = scaleWH(w, h); // scaled width and height
    
    if (Math.abs(w-h) < 0.1) {
    	
      // draw a circle
      writer.println("PU" + xy[0] + "," + xy[1] + ";");  
      writer.println("CI" + wh[0]/2.0 + "," + ca + ";");
      writer.println("PU;");
      
    } else {
    	
      // draw an ellipse
      double initx = x + w/2.0 * Math.cos(0.0);
      double inity = y + h/2.0 * Math.sin(0.0);
      initxy = scaleXY((float)initx, (float)inity);
     
      double _x, _y;
      
      writer.println("PU" + initxy[0] + "," + initxy[1] + ";");
      
      for (double t=ca; t<360.0; t+=ca) {

    	_x = x + w/2.0 * Math.cos(Math.toRadians(t));
        _y = y + h/2.0 * Math.sin(Math.toRadians(t));
    	
    	if (Math.abs(_x) < 0.01) _x=0.01;
    	if (Math.abs(_y) < 0.01) _y=0.01;
    	   
    	xy = scaleXY((float)_x, (float)_y);
        
        writer.println("PD" + xy[0] + "," + xy[1] + ";");
        
      }
      
      writer.println("PD" + initxy[0] + "," + initxy[1] + ";");
      writer.println("PU;");
    }
  }
  
  // arcs
  
  public void arc(float x, float y, float w, float h, float start, float stop) {
   	arc(x,y,w,h,start,stop,OPEN);
  }
  
  public void arc(float x, float y, float w, float h, float start, float stop, int mode) {
	
	  double[] xy   = new double[2];
	  double[] x1y1 = new double[2];
	  double[] x2y2 = new double[2];
	  double   x1, y1, x2, y2;
    
	  x1 = x + w/2 * Math.cos(start);
	  y1 = y + w/2 * Math.sin(start);
    
	  x2 = x + w/2 * Math.cos(stop);
	  y2 = y + w/2 * Math.sin(stop);
    
	  xy = scaleXY(x, y); // get the transformations:
	  x1y1 = scaleXY((float)x1, (float) y1);
	  x2y2 = scaleXY((float)x2, (float) y2);
    
	  // convert radians to degrees, swap clockwise to anti-clockwise
	  double startd = 360 - start*180.0/PI;
	  double stopd  = 360 - stop*180.0/PI;
    
	  if (Math.abs(w-h) < 0.1) {

		  // draw the arc
		  writer.println("SP1;");
		  writer.println("PU" + x1y1[0] + "," + x1y1[1] + ";");
		  writer.println("PD;AA"+xy[0]+","+xy[1]+","+(stopd-startd)+","+getChordAngle()+";");
      
		  if (mode == CHORD) {
			  writer.println("PD" + x1y1[0] + "," + x1y1[1] + ";");
		  }
      
		  if (mode == PIE){
			  writer.println("PD" + xy[0] + "," + xy[1] + ";");
			  writer.println("PD" + x1y1[0] + "," + x1y1[1] + ";");
		  }

		  writer.println("PU;");
	  }
    
  }
  
  public void rectImpl(float x1, float y1, float x2, float y2) {
    // x2,y2 are opposite corner points, not width and height
    // see PGraphics, line 2578 
      
    double[] x1y1 = new double[2];
   	double[] x2y1 = new double[2];
   	double[] x2y2 = new double[2];
   	double[] x1y2 = new double[2];

    // get the transformed/scaled points    
    x1y1 = scaleXY(x1,y1);
    x2y1 = scaleXY(x2,y1);
    x1y2 = scaleXY(x1,y2);
    x2y2 = scaleXY(x2,y2);
    
    writer.println("PU" + x1y1[0] + "," + x1y1[1] + ";");
    writer.println("PD" + x2y1[0] + "," + x2y1[1] +
                    "," + x2y2[0] + "," + x2y2[1] +
                    "," + x1y2[0] + "," + x1y2[1] +
                    "," + x1y1[0] + "," + x1y1[1] + ";");
    writer.println("PU;");
           
  }

  // MATRIX STACK - from GraphicsHPGL.java, gsn/src

  public void pushMatrix() {
    if (transformCount == transformStack.length) {   
      throw new RuntimeException("pushMatrix() overflow");
    }
      
    transformStack[transformCount] = this.transformMatrix.get();
    transformCount++;
  }

  public void popMatrix() {
	   //System.out.println("popMatrix");

    if (transformCount == 0) {   
      throw new RuntimeException("HPGL: matrix stack underrun");
    }
    transformCount--;
    this.transformMatrix = new PMatrix2D();
    for (int i = 0; i <= transformCount; i++) {   
      this.transformMatrix.apply(transformStack[i]);
    }
  }

  public void translate(float x, float y) {
    // PenUp, move by x, y.
    //this.modelView.print();
    this.transformMatrix.translate(x, y);
    //this.modelView.print();
  }
  
  public void rotate(float angle) { 
    this.transformMatrix.rotate(angle);
  }
  
  public void dispose() {
    writer.flush();
    writer.close();
    writer = null;
  }
  
  // WRITER METHODS

  /**
   * Write a command on one line (as a String), then start a new line
   * and write out a formatted float. Available for anyone who wants to
   * insert additional commands into the HPGL stream.
   * @param cmd HPGL command
   * @param val HPGL parameter
   */
  public void write(String cmd, float val) {
    writer.println(cmd);
    // Don't number format, will cause trouble on systems that aren't en-US
    // http://dev.processing.org/bugs/show_bug.cgi?id=495
    writer.println(val);
  }

  /**
   * Write a line to the HPGL file. Available for anyone who wants to
   * insert additional commands into the HPGL stream.
   * @param what String to write
   */
  public void println(String what) {
    writer.println(what);
  }
  
  private void writeHeader() {
	writer.println("IN;SP1;");
  }
  
  private void writeFooter() {
    writer.println("PU0,0;");
  }

  // GENERAL METHODS

  public boolean displayable() { return false; }
  public boolean is2D() { return true; }
  public boolean is3D() { return true; }
  public void resetMatrix(){ }
  public void blendMode(int mode){ }
  
}


