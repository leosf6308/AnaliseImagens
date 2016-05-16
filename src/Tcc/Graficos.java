/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.border.LineBorder;
/**
 *
 * @author leonardo.fiorentini
 */
class DrawingPanel extends JPanel{
    private float[] xAxisData;
    private float[] yAxisData;
    private float maxValue;
    public static final float PADDING = 5.0f;
    public static final float EPISLON = 0.00000001f;
    public DrawingPanel(float[] xAxis, float[] yAxis) {
        super();
        xAxisData = xAxis;
        yAxisData = yAxis;
        maxValue = -1;
        int i;
        for(i = 0; i < yAxisData.length;i++)
            if(yAxisData[i] > maxValue)
                maxValue = yAxisData[i];
        setBorder(new LineBorder(Color.BLACK));
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(534, 534);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        java.awt.FontMetrics fontMetrics = g.getFontMetrics();
        float x = fontMetrics.stringWidth(""+maxValue)+PADDING*2;
        float y = PADDING;
        float w = this.getWidth();
        float h = this.getHeight();
        float graphH = h-y-PADDING, graphW = w-x-PADDING*2;
        int step = (int)(graphW/xAxisData.length);
        int offset = 0;
        int i;
        graphH -= (fontMetrics.getHeight()+PADDING);
        if(xAxisData[0] >= EPISLON){
            offset = (int)(graphW-PADDING*2-step*(xAxisData.length-1))/2;
            if(offset < PADDING)
                offset = (int)PADDING;
        }
        //draw x axis
        g2.draw(new Line2D.Double(PADDING,y+PADDING+graphH,x+graphW,y+PADDING+graphH));
        //draw y axis.
        g2.draw(new Line2D.Double(x+PADDING,y,x+PADDING,h-PADDING));
        
        g2.drawString("Offset: "+offset,w-120,PADDING+15);
        g2.drawString("GraphW: "+graphW,w-120,PADDING+30);
        g2.drawString("Step: "+step,w-120,PADDING+45);
        g2.drawString("Width: "+w,w-120,PADDING+60);
        
        g2.drawString(""+maxValue,PADDING,y+PADDING+(fontMetrics.getHeight()/2));
        g2.draw(new Line2D.Double(x,y+PADDING,x+PADDING*2,y+PADDING));
        
        g2.drawString(""+xAxisData[0], x+PADDING+offset+1,y+PADDING+graphH+fontMetrics.getHeight());
        //
        i = (int) (step*(xAxisData.length-1)+x+PADDING+offset);
        if(i+fontMetrics.stringWidth(""+xAxisData[xAxisData.length-1]) > w)
            i = (int)w - fontMetrics.stringWidth(""+xAxisData[xAxisData.length-1]);
        g2.drawString(""+xAxisData[xAxisData.length-1], i,y+PADDING+graphH+fontMetrics.getHeight());
        
        g2.setColor(Color.RED);
        
        for(i = 0; i < xAxisData.length; i++){
            if(Math.abs(yAxisData[i]) < EPISLON)
                continue;
            float size = graphH*(yAxisData[i]/maxValue);
            if(size > graphH)
                size = graphH;
            if(step == 1)
                g2.draw(new Line2D.Float(i*step+x+PADDING+offset,y+PADDING+graphH-size,i*step+x+PADDING+offset,y+PADDING+graphH-1));
            else
                g2.fill(new Rectangle2D.Float(i*step+x+PADDING+offset,y+PADDING+graphH-size,step,size));    
        }
    }
}

public class Graficos extends JFrame {
    //private static final long serialVersionUID = -4752966848100689153L;
    private DrawingPanel drawPane;
    public Graficos(float[] xAxis, float[] yAxis){
        super();
        if(xAxis == null)
            throw new RuntimeException("Give xAxis data!");
        if(yAxis == null)
            throw new RuntimeException("Give yAxis data!");
        if(xAxis.length != yAxis.length)
            throw new RuntimeException("Must be equal!!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        drawPane = new DrawingPanel(xAxis,yAxis);
        add(drawPane);
        //this.getContentPane().setPreferredSize(new Dimension(400,400));
        /*JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(128,128));// changed it to preferredSize, Thanks!
        this.getContentPane().add( jp ); // adding to content pane will work here. Please read the comment bellow.
        //*/
        //this.setSize(128, 128);
        this.setVisible(true);
        this.pack();
    }
}
