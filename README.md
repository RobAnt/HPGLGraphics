## HPGLGraphics Library for Processing

This library implements [HPGL](https://en.wikipedia.org/wiki/HPGL) (Hewlett-Packard Graphics Language) file creation from Processing sketches. It works in much the same way as the PDF Export and DXF Export libraries bundled with Processing.

### Getting started 

Download the library [here](http://ciaron.net/hpglgraphics/download/hpglgraphics.zip). Unzip into your sketchbook/libraries folder.
    
    import hpglgraphics.*;
    HPGLGraphics hpgl = (HPGLGraphics) createGraphics(width, height, HPGLGraphics.HPGL);
    
    beginRecord(hpgl);
    // do some drawing
    endRecord();

Have a look at the examples included with the library. These demonstrate:

    line()
    ellipse()
    arc()     // PIE, CHORD and OPEN
    rect()
    vertex()
    curveVertex()
    bezierVertex()

### Additional methods

Some additional methods are available for controlling the HPGL output. This include:

    selectPen(int pen);         // choose another pen (e.g. colour)
    setPaperSize(String size);  // "A3", "A4", "A" or "B". Landscape orientation, Left Aligned. (Defaults to A4)
    setPaperSize(String size, String orientation, String alignment);  // Orientation can LANDSCAPE, PORTRAIT or AUTO (will depend based on the createGraphics width and height)
                                // Alignment is one of "BL","BC","BR","TL","TC","TR","CL","CC","CR" - Bottom/Top/Center Left/Center/Right 
    setPath(String filename);   // optional. HPGL is output to this file in the sketch directory. 
                                // The default is "output.hpgl".

### Tips:
  * line() always finishes with Pen Up. To draw continuous joined lines without lifting the pen, use vertex() instead.
  * text() requires a fill to be plotted.  Text with stroke but no fill will render in the sketch but not plot. 
  * To check your HPGL files, I recommend [hp2xx](https://www.gnu.org/software/hp2xx/). If using Windows, another option is the [CERN HPGL viewer](https://service-hpglview.web.cern.ch/service-hpglview/) 

This library was inspired by, and builds upon, these libraries:
  * [HPGL-Plotter](http://sjunnesson.github.io/HPGL-Plotter/)
  * [HPGL](https://github.com/gregersn/HPGL)

The website for the library is [here](https://ciaron.github.io/HPGLGraphics).
Source code is [here](https://github.com/ciaron/HPGLGraphics)
