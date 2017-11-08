/**
 *
 * @author eduardo
 */
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class Concentric_Circles implements PlugIn, DialogListener {
    private static int nCircles = 3;
    private static double lineWidth = 5;
    private static double centerX, centerY;
    private static double outerCircleRadius;
    private boolean hide;
    private ImagePlus img;

    @Override
    public void run(String arg) {
        if(IJ.versionLessThan("1.47v")) {
            return;
        }
        
        img = IJ.getImage();
        Roi roi = img.getRoi();
        Rectangle b = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        boolean isRoi = roi != null && roi.isArea();
        boolean isLine = roi != null && roi.getType() == Roi.LINE;
        
        if(isRoi||isLine) {
            b = roi.getBounds();
        }
        if(outerCircleRadius == 0f || isRoi || isLine) {
            setup(b, isLine);
        }
        
        if(!IJ.isMacro()) {
            drawCirclesAndLines();
        }
        
        showDialog();
    }

    void drawCirclesAndLines() {
        if(hide || nCircles <= 0) {
            img.setOverlay(null);
        }
        else {
            Overlay overlay = new Overlay();
            
            Line originalLine = new Line(centerX, centerY - outerCircleRadius, centerX, centerY + outerCircleRadius);
            originalLine.setRotationCenter(centerX, centerY);

            double inc = outerCircleRadius / nCircles;
            double r = inc;
            for(int i = 0; i < nCircles; i++) {
                Roi circle = new OvalRoi(centerX-r, centerY-r, r*2, r*2);
                circle.setStrokeColor(Color.red);
                
                if(lineWidth > 1) {
                    circle.setStrokeWidth(lineWidth);
                }
                overlay.add(circle);
                
                r += inc;
            }
            
            double angleInDegrees = 22.5;
            for(int i = 0; i < 4; i++) {
                Line rotatedLine = (Line) RoiRotator.rotate(originalLine, angleInDegrees);

                rotatedLine.setStrokeColor(Color.red);
                if(lineWidth > 1) {
                    rotatedLine.setStrokeWidth(lineWidth);
                }
                overlay.add(rotatedLine);
                
                angleInDegrees += 45;
            }

            img.setOverlay(overlay);
        }
    }

    void setup(Rectangle b, boolean isLine) {
        centerX = b.x + b.width/2;
        centerY = b.y + b.height/2;
        float size = Math.min(b.width, b.height);
        
        if(isLine) {
            size = (float)Math.sqrt(b.width*b.width+b.height*b.height);
        }
        
        outerCircleRadius = size/2f;
    }

    void showDialog() {
        GenericDialog gd = new GenericDialog("Circles");

        gd.addNumericField("Circles:", nCircles, 0);
        gd.addNumericField("Line width:", lineWidth, 1);
        gd.addNumericField("X center:", centerX, 1);
        gd.addNumericField("Y center:", centerY, 1);
        gd.addNumericField("Outer radius:", outerCircleRadius, 1);
        gd.addCheckbox("Hide", false);
        gd.addDialogListener(this);
        gd.showDialog();
        
        if(gd.wasCanceled()) {
            img.setOverlay(null);
        }
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        if(gd.wasCanceled()) {
            return false;
        }
        
        nCircles = (int)gd.getNextNumber();
        lineWidth = (float)gd.getNextNumber();
        centerX = (float)gd.getNextNumber();
        centerY = (float)gd.getNextNumber();
        outerCircleRadius = (float)gd.getNextNumber();
        hide = gd.getNextBoolean();
        drawCirclesAndLines();
        
        return true;
    }
}