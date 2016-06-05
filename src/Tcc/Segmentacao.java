/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;

import static Tcc.DrawingPanel.PADDING;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
/**
 *
 * @author Leonardo
 */

public class Segmentacao extends JFrame{
    public static final float ANGLE_THRESHOLD = 5.0f;
    private class VisuSegm extends JPanel implements MouseMotionListener{
        public static final float PADDING = 5.0f;
        public static final float EPISLON = 0.00000001f;
        private Segmentacao segmDados;
        private BufferedImage canvas;
        private int x, y, xLast, yLast;
        public VisuSegm(Segmentacao dados){
            super();
            this.setSize(800,600);
            segmDados = dados;
            x = y = 0;
            canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            addMouseMotionListener(this);
        }
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 600);
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g); 
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_OFF);
            java.awt.FontMetrics fontMetrics = g.getFontMetrics();
            //g2.draw(new Line2D.Double(0,0,this.getWidth(),this.getHeight()));
            if(segmDados.getSegmState() != Segmentacao.STATE_DONE){
                String strMsg = null;
                switch(segmDados.getSegmState()){
                    case Segmentacao.STATE_INITIALIZING:
                        strMsg = "Inicializando...";
                        break;
                    case Segmentacao.STATE_CLUSTERING:
                        strMsg = "Agrupando pixels em clusters...";
                        break;
                    case Segmentacao.STATE_COMPACTING:
                        strMsg = "Removendo redundâncias..";
                        break;
                }
                Rectangle2D strRect = fontMetrics.getStringBounds(strMsg, g);
                g2.drawString(strMsg,
                        (int)((this.getWidth()/2)-(strRect.getWidth()/2)),
                        (int)((this.getHeight()/2)-(strRect.getHeight()/2)));
            }else if(segmDados.selectedClus != 0){
                int i, j;
                int w = this.getWidth();
                int h = this.getHeight();
                Cluster tClus;
                String strStats;
                if(w > segmDados.imgWidth-x)
                    w = segmDados.imgWidth-x;
                if(h > segmDados.imgHeight-y)
                    h = segmDados.imgHeight-y;
                
                for(i = 0; i < w; i++){
                    for(j = 0; j < h; j++){
                        if(segmDados.getPixelClusterID(x+i, y+j) == segmDados.selectedClus)
                            canvas.setRGB(i, j, 0xFFFF0000);
                        else
                            canvas.setRGB(i, j, 0xFF000000);
                    }
                }
                tClus = segmDados.getCluster(segmDados.selectedClus);
                strStats = segmDados.getCluster(segmDados.selectedClus).detectShape();
                lblClusInf.setText("ID"+tClus.clusterID+"; x:"+tClus.left+"; y:"+tClus.top+"; w:"+(tClus.right-tClus.top)+"; h:"+(tClus.bottom-tClus.left)+" cnt:"+tClus.pixelCount+"; avg:"+tClus.tone+"; "+strStats);
                g2.drawImage(canvas, null, null);
                System.out.println(""+w+"x"+h);
            }
        }
        public void mouseMoved(MouseEvent e) {
            xLast = e.getX();
            yLast = e.getY();
            return;
        }

        public void mouseDragged(MouseEvent e) {
            int diffX, diffY;
            diffX = e.getX()-xLast;
            diffY = e.getY()-yLast;
            x -= diffX;
            y -= diffY;
            if(x > segmDados.imgWidth-this.getWidth())
                x = segmDados.imgWidth-this.getWidth();
            if(y > segmDados.imgHeight-this.getHeight())
                y = segmDados.imgHeight-this.getHeight();
            if(x < 0)
                x = 0;
            if(y < 0)
                y = 0;
            System.out.println("Mouse moved by "+e.getX()+"x"+e.getY()+". New position: "+x+"x"+y+".");
            xLast = e.getX();
            yLast = e.getY();
            this.repaint();
        }
        public void RedimVisu(int width, int height){
            setSize(width,height);
            canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
    }
    public static double colorDistance(Color c1, Color c2){
        int dr = c1.getRed()-c2.getRed();
        int dg = c1.getGreen()-c2.getGreen();
        int db = c1.getBlue()-c2.getBlue();
        double dist = dr*dr + dg*dg + db*db;
        return Math.sqrt(dist);
    }
    
    private class PixelCount{
        public int clusterID;
        public int count;
        public double distance;
        public PixelCount(int id, double dist){
             clusterID = id;
             count = 1;
             distance = dist;
        }
    }
    private class Cluster{
        public int clusterID;
        public Cluster next;
        public Cluster before;
        public int pixelCount;
        public int left;
        public int top;
        public int right;
        public int bottom;
        public Color tone;
        private int imgW;
        private int imgH;
        private int[] pixClus;
        public Cluster(int coordX, int coordY, int id, Color avgVal, int imgWidth, int imgHeight, int[] pixelCluster){
            left = coordX;
            top = coordY;
            right = coordX+1;
            bottom = coordY+1;
            clusterID = id;
            pixelCount = 1;
            imgW = imgWidth;
            imgH = imgHeight;
            tone = avgVal;
            pixClus = pixelCluster;
            pixClus[top*imgW+left] = clusterID;
            next = null;
            before = null;
        }
        public void mergePoint(int x, int y, Color color){
            float newR = (tone.getRed()*pixelCount+color.getRed());
            float newG = (tone.getGreen()*pixelCount+color.getGreen());
            float newB = (tone.getBlue()*pixelCount+color.getBlue());
            pixelCount++;
            newR /= pixelCount;
            newG /= pixelCount;
            newB /= pixelCount;
            tone = new Color(Math.round(newR), Math.round(newG), Math.round(newB));
            if(x < left)
                 left = x;
            if(x >= right)
                 right = x+1;
            if(y < top)
                 top = y;
            if(y >= bottom)
                 bottom = y+1;
            pixClus[y*imgW+x] = clusterID;
            pixelCount++;
        }
        void mergeCluster(Cluster c2, Cluster head){
            int i;
            Cluster clusBefore;
            if(c2.clusterID == clusterID)
                throw new RuntimeException("A self merge was detected.");
            
            float newR = tone.getRed()*pixelCount+c2.tone.getRed()*c2.pixelCount;
            float newG = tone.getGreen()*pixelCount+c2.tone.getGreen()*c2.pixelCount;
            float newB = tone.getBlue()*pixelCount+c2.tone.getBlue()*c2.pixelCount;
            
            if(left > c2.left)
                left = c2.left;
            if(top > c2.top)
                top = c2.top;
            if(right < c2.right)
                right = c2.right;
            if(bottom < c2.bottom)
                bottom = c2.bottom;
            
            pixelCount += c2.pixelCount;
            newR /= pixelCount;
            newG /= pixelCount;
            newB /= pixelCount;
            tone = new Color(Math.round(newR), Math.round(newG), Math.round(newB));

            for(i = 0; i < imgW*imgH; i++)
                if(pixClus[i] == c2.clusterID)
                    pixClus[i] = clusterID;
            
            clusBefore = c2.before;
            clusBefore.next = c2.next;
            if(clusBefore.next != null)
                clusBefore.next.before = clusBefore;
            //GC will delete c2.
        }
        void mergeClosest(Cluster head){
            /*Iterar pelo retângulo que envolve o cluster.
             *Nessa iteração, contar quantos pixels tem cores mais próximas.
             *Pesar pelas menores distâncias.
             *
             */
            int i, x, y, n, sizew, sizeh, step;
            double maxDist, maxWeight;
            ArrayList<PixelCount> pixCnt = new ArrayList();
            Cluster Ci;
            i = (top>0?top-1:0);
            i *= imgW;
            i += (left>0?left-1:0);
            sizew = (right+1>imgW?right:right+1)-(left>0?left-1:0);
            sizeh = (bottom+1>imgH?bottom:bottom+1)-(top>0?top-1:0);
            step = imgW-sizew;
            y = 0;
            maxDist = 0;
            while(y < sizeh){
                x = 0;
                while(x < sizew){
                    if(pixClus[i] != clusterID){
                        Boolean found = false;
                        for(n = 0; n < pixCnt.size(); n++){
                            if(pixCnt.get(n).clusterID == pixClus[i]){
                                pixCnt.get(n).count++;
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            double distance = colorDistance(tone,getClusterColor(head,pixClus[i]));
                            pixCnt.add(new PixelCount(pixClus[i],distance));
                            if(distance > maxDist)
                                maxDist = distance;
                        }
                    }
                    i++;
                    x++;
                }
                i += step;
                y++;
            }
            i = -1;
            maxWeight = 0.0;
            maxDist--;
            for(n = 0; n < pixCnt.size(); n++){
                double thisWeight = pixCnt.get(n).count*(maxDist-pixCnt.get(n).distance);
                if(thisWeight > maxWeight || pixCnt.get(n).distance-maxDist < 1.01){
                    i = n;
                    maxWeight = thisWeight;
                }
            }
            
            if(i != -1){
                Ci = getClusterByID(head,pixCnt.get(i).clusterID);
                Ci.mergeCluster(this, head);
                System.out.println("Merged cluster " + clusterID + " with " + Ci.clusterID );
            }else{
                String strException = "Can't find any cluster to merge with " + clusterID + ". Position: " + top + "x" + left + ". Size: " + 
                        (right-left) + "x" + (bottom-top) + "(" + sizew + "x" + sizeh + "). i: " + i + ". Step: " + step +
                        ". Clusters: " + pixCnt.size() + ". maxDist:" + maxDist + ". maxWeight:" + maxWeight;
                throw new RuntimeException(strException);
            }
            pixCnt.clear();
        }
        public String detectShape(){
            String stats, sType;
            Point center;
            int i, j, S, width, height, thisPx, lnStep;
            double ux, uy, u20, u02, u11, C, angleAbs, ratio;
            thisPx = top*imgW+left;
            //Get region center
            width = right-left;
            height = bottom-top;
            lnStep = imgW-width;
            j = S = 0;
            ux = uy = 0.0;
            while(j < height){
		i = 0;
		while(i < width){
                    if(pixClus[thisPx] == clusterID){
                        ux += i;
                        uy += j;
                        S++;
                    }
                    thisPx++;
                    i++;
		}
		thisPx += lnStep;
		j++;
            }
            ux /= S;
            uy /= S;
            center = new Point((int)ux,(int)uy);
            
            thisPx = top*imgW+left;
            u20 = u02 = u11 = 0.0;
            j = 0;
            while(j < height){
                i = 0;
                while(i < width){
                    if(pixClus[thisPx] == clusterID){
                        int xvar, yvar;
                        xvar = i - center.x;
                        yvar = j - center.y;
                        u20 += (xvar*xvar);
                        u02 += (yvar*yvar);
                        u11 += (xvar*yvar);
                    }
                    thisPx++;
                    i++;
                }
                thisPx += lnStep;
                j++;
            }
            u20 /= S;
            u02 /= S;
            u11 /= S;
            C = 0.5*Math.atan2(2*u11,u20-u02);
            
            angleAbs = Math.abs(C);
            if(angleAbs < (ANGLE_THRESHOLD*Math.PI)/180.0 ||
		(angleAbs-(Math.PI/4) < (ANGLE_THRESHOLD*Math.PI)/180.0 && angleAbs-(Math.PI/4) > -((ANGLE_THRESHOLD*Math.PI)/180.0)) ||
		(angleAbs-(Math.PI/2) < (ANGLE_THRESHOLD*Math.PI)/180.0 && angleAbs-(Math.PI/2) > -((ANGLE_THRESHOLD*Math.PI)/180.0))){
		//Nice, no rotation is required
		ratio = ((double)S)/(width*height);
                System.out.println("No rotation was required.");
            }else{
                int rWidth, rHeight, x, y, xMax, yMax;
                Point[] corners = new Point[4];
                Point rCenter;
                double sine, cossine, newx, newy;
                byte[][] pixels;
		sine = Math.sin(-C);
		cossine = Math.cos(-C);
                corners[0] = new Point(-(width/2),-(height/2));
		corners[1] = new Point(width/2,-(height/2));
		corners[2] = new Point(-(width/2),height/2);
		corners[3] = new Point(width/2,height/2);
                xMax = -width;
                yMax = -height; //It's the maximum point
		x = width;
		y = height;
                for(i = 0; i < 4; i++){
                    if(C > 0.0){ //Is angle clockwise?
                        //Rotate Counterclockwise
                        newx = corners[i].x*cossine+corners[i].y*sine;
                        newy = -1*corners[i].x*sine+corners[i].y*cossine;
                    }else{				
                        //Rotate Clockwise
                        newx = corners[i].x*cossine-corners[i].y*sine;
                        newy = corners[i].x*sine+corners[i].y*cossine;
                    }
                    corners[i].x = (int)Math.round(newx);
                    corners[i].y = (int)Math.round(newy);
                    if(corners[i].x < x)
                        x = corners[i].x;
                    if(corners[i].y < y)
                        y = corners[i].y;
                    if(corners[i].x > xMax)
                        xMax = corners[i].x;
                    if(corners[i].y > yMax)
                        yMax = corners[i].y;
		}
                rWidth = (xMax-x)+1;
                rHeight = (yMax-y)+1;
                rCenter = new Point(rWidth/2, rHeight/2);
                pixels = new byte[rHeight][rWidth];
                thisPx = top*imgW+left;
                j = 0;
                while(j < height){
                    i = 0;
                    while(i < width){
                        if(pixClus[thisPx] == clusterID){
                            if(C > 0.0){ //Is angle clockwise?
                                //Rotate Counterclockwise
                                newx = (i-center.x)*cossine+(j-center.y)*sine;
                                newy = -1*(i-center.x)*sine+(j-center.y)*cossine;
                            }else{				
                                //Rotate Clockwise
                                newx = (i-center.x)*cossine-(j-center.y)*sine;
                                newy = (i-center.x)*sine+(j-center.y)*cossine;
                            }
                            newx += rCenter.x;
                            newy += rCenter.y;
                            x = (int)Math.floor(newx);
                            y = (int)Math.floor(newy);
                            if(x < 0 || x+1 > rWidth || y < 0 || y+1 > rHeight)
                                System.out.printf("Can't plot %d (pt%dx%d r%.2fx%.2f %.2f,%.2f)\n",y*rWidth+x,i,j,newx+rCenter.x,newy+rCenter.y,newx-x,newy-y);
                            else{
                                pixels[y][x] = 127;
                                if(x+1 < rWidth && newx-x > 0.2){
                                    pixels[y][x+1] = 127;
                                    if(y+1 < rHeight && newy-y > 0.2)
                                        pixels[y+1][x+1] = 127;
                                }
                                if(y+1 < rHeight && newy-y > 0.2)
                                    pixels[y+1][x] = 127;
                            }
                        }
                        thisPx++;
                        i++;
                    }
                    thisPx += lnStep;
                    j++;
                }
                x = rWidth;
		y = rHeight;
		xMax = 0;
		yMax = 0;
		thisPx = 0;
		j = 0;
		S = 0;
		while(j < rHeight){
                    i = 0;
                    while(i < rWidth){
                        if(pixels[j][i] == 127){
                            if(i < x)
                                x = i;
                            if(j < y)
                                y = j;
                            if(i > xMax)
                                xMax = i;
                            if(j > yMax)
                                yMax = j;
                            S++;
                        }
                        thisPx++;
                        i++;
                    }
                    j++;
		}
                rWidth = (xMax-x);
		rHeight = (yMax-y);
		ratio = ((double)S)/(rWidth*rHeight);
            }
            if(Math.abs(ratio-1) < 0.05)
		sType = "SQUARE";
            else if(Math.abs(ratio-0.78) < 0.02)
                sType = "CIRCLE";
            else if(ratio < 0.5 && ratio > 0.25 )
                sType = "TRIANGLE";
            else
                sType = "UNKNOWN";
            stats = String.format("(%03d|%03d|%03d)\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%.4e\t%.4e\t%.4e\t%.3f\t%.3f\t%s\n",
            tone.getRed(),tone.getGreen(),tone.getBlue(),pixelCount,left,top,right-left,bottom-top,center.x,center.y,
            u20,u02,u11,(C*180.0)/Math.PI,ratio,sType);
            return stats;
        }
    }
    private int state;
    private int selectedClus;
    private int imgWidth;
    private int imgHeight;
    private int clusterCount;
    private int lastID;
    private int[] pixelCluster;
    private Cluster clusterList;
    private PixelReader pixReader;
    public static final int SEGM_DKH_FAST_SCANNING = 1;
    public static final int SEGM_WATERSHED = 2;
    public static final int SEGM_KMEANS = 3;
    public static final int STATE_INITIALIZING = 0;
    public static final int STATE_CLUSTERING = 1;
    public static final int STATE_COMPACTING = 2;
    public static final int STATE_DONE = 255;
    private VisuSegm drawPane;
    private JLabel lblCntClusters;
    private JComboBox cblClustList;
    private JTextField lblClusInf;
    public Segmentacao(int tipo, int imgW, int imgH, PixelReader pr){
        super();
        state = STATE_INITIALIZING;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container janela = getContentPane();
        Insets insets = getInsets();
        drawPane = new VisuSegm(this);
        String[] strClusNames = {"Cluster 1", "Cluster 2", "Cluswter 3"};
        Dimension size;
        lblCntClusters = new JLabel("Counting clusters...");
        lblClusInf = new JTextField("Select a cluster");
        cblClustList = new JComboBox(strClusNames);
        janela.setLayout(null);
        janela.add(lblCntClusters);
        janela.add(cblClustList);
        janela.add(lblClusInf);
        janela.add(drawPane);
        
        lblCntClusters.setFont(this.getFont());
        lblCntClusters.setForeground(this.getForeground());
        size = lblCntClusters.getPreferredSize();
        lblCntClusters.setBounds(insets.left,insets.top+3, size.width, size.height);
        
        size = cblClustList.getPreferredSize();
        cblClustList.setBounds(lblCntClusters.getWidth()+insets.left,insets.top, size.width, size.height);
        
        lblClusInf.setFont(this.getFont());
        lblClusInf.setForeground(this.getForeground());
        size = lblClusInf.getPreferredSize();
        lblClusInf.setBounds(lblCntClusters.getWidth()+cblClustList.getWidth()+insets.left,insets.top+3, 200, size.height);
        
        drawPane.setBounds(insets.left,insets.top+cblClustList.getHeight(), drawPane.getWidth(), drawPane.getHeight());
        
        setSize(drawPane.getWidth() + insets.left + insets.right, drawPane.getHeight() + size.height + insets.top + insets.bottom);
        this.setVisible(true);
        imgWidth = imgW;
        imgHeight = imgH;
        pixReader = pr;
        clusterList = null;
        pixelCluster = new int[imgWidth*imgHeight];
        clusterCount = 0;
        lastID = 0;
        switch(tipo){
            case SEGM_DKH_FAST_SCANNING:
                DKHFastScanning();
                break;
        }
        selectedClus = 0;
        Segmentacao jPane = this;
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // This is only called when the user releases the mouse button.
                jPane.resize();
            }
        });
        //https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#LabelDemo
    }
    public void resize(){
        Container janela = getContentPane();
        Insets insets = getInsets();
        drawPane.RedimVisu(this.getWidth()-(insets.right+insets.left), this.getHeight()-(drawPane.getX()+insets.top));
        
        System.out.println("New size: "+(this.getWidth()-(insets.right+insets.left))+"x"+(this.getHeight()-(drawPane.getX()+insets.top))+". ");
    }
    public void updateUI(){
        //drawPane.invalidate();
        //this.invalidate();
        this.repaint();
    }
    public int getImgWidth(){
        return imgWidth;
    }
    public int getImgHeight(){
        return imgHeight;
    }
    public int getSelectedClus(){
        return selectedClus;
    }
    public static Color getClusterColor(Cluster head, int id){
        while(head != null && head.clusterID != id)
            head = head.next;
        return head.tone;
    }
    public int getPixelClusterID(int x, int y){
        if(x < 0 || x >= imgWidth || y < 0 || y >= imgHeight)
            throw new RuntimeException("Pixel ("+x+","+y+") out of bounds! ");
        return pixelCluster[y*imgWidth+x];
    }
    private Cluster getPixelCluster(int x, int y){
        int clusID = getPixelClusterID(x,y);
        Cluster thisClus = clusterList;
        while(thisClus != null){
            if(thisClus.clusterID == clusID)
                return thisClus;
            thisClus = thisClus.next;
        }
        throw new RuntimeException("Coudn't find cluster "+clusID+" for point ("+x+","+y+"). ");
        //return null;
    }
    private Cluster getClusterByID(Cluster head, int id){
        while(head != null){
            if(head.clusterID == id){
                return head;
            }
            head = head.next;
        }
        return null;
    }
    public int getSegmState(){
        return state;
    }
    public Cluster getCluster(int id){
        Cluster head = clusterList;
        while(head != null){
            if(head.clusterID == id){
                return head;
            }
            head = head.next;
        }
        throw new RuntimeException("Cluster "+id+" not found.");
    }
    
    public void DKHFastScanning(){
        int i, j, k, x, y;
        int threshold;
        double dist;
        String inputValue = JOptionPane.showInputDialog("Type a threshold value (or just accept by pressing RETURN):","45");
        threshold = Integer.parseInt(inputValue);
        
        state = STATE_CLUSTERING;
        updateUI();
        
        javafx.scene.paint.Color color = pixReader.getColor(0, 0);
        Color thisPx;
        Cluster Ci, Cu, Cl, lastClus;
        lastID = 0;
        clusterCount = 1;
        color = pixReader.getColor(0, 0);
        thisPx = new Color((int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255));
        Ci = new Cluster(0,0,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
        clusterList = lastClus = Ci;
        for(j = 1; j < imgWidth; j++){
            Ci = getPixelCluster(j-1,0);
            color = pixReader.getColor(j, 0);
            thisPx = new Color((int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255));
            dist = colorDistance(Ci.tone,thisPx);
            if(dist < threshold)
                Ci.mergePoint(j, 0, thisPx);
            else{
                Ci = new Cluster(j,0,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
                Ci.before = lastClus;
                lastClus.next = Ci;
                lastClus = Ci;
                clusterCount++;
            }
        }
        
        for(i = 1; i < imgHeight; i++){
            Ci = getPixelCluster(0,i-1);
            color = pixReader.getColor(0, i);
            thisPx = new Color((int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255));
            dist = colorDistance(Ci.tone,thisPx);
            if(dist < threshold)
                Ci.mergePoint(0, i, thisPx);
            else{
                Ci = new Cluster(0,i,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
                Ci.before = lastClus;
                lastClus.next = Ci;
                lastClus = Ci;
                clusterCount++;
            }
            
            for(j = 1; j < imgWidth; j++){
                /*
                dist = getColorDistance(Cu->getTone(),*thisPx);
                */
                Cu = getPixelCluster(j,i-1);
                Cl = getPixelCluster(j-1,i);
                color = pixReader.getColor(j, i);
                thisPx = new Color((int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255));
                dist = colorDistance(Cu.tone,thisPx);
                if(dist < threshold){
                    Cu.mergePoint(j, i, thisPx);
                    dist = colorDistance(Cl.tone,thisPx);
                    if(dist < threshold){
                        if(Cu.clusterID != Cl.clusterID){
                            Boolean findLast = false;
                            clusterCount--;
                            //Do not destroy first cluster.
                            if(Cl != clusterList){
                                Cu.mergeCluster(Cl, clusterList);
                                if(Cl == lastClus)
                                    lastClus = lastClus.before;
                            }else{
                                Cl.mergeCluster(Cu, clusterList);
                                if(Cu == lastClus)
                                    lastClus = lastClus.before;
                            }
                        }
                    }
                }else{
                    dist = colorDistance(Cl.tone,thisPx);
                    if(dist < threshold)
                        Cl.mergePoint(j, i, thisPx);
                    else{
                        Ci = new Cluster(j,i,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
                        Ci.before = lastClus;
                        lastClus.next = Ci;
                        lastClus = Ci;
                        clusterCount++;
                    }
                }
            }
            System.out.println("Line "+i+" done. Cluster count "+clusterCount+".");
        }
        
        int minCt = (imgWidth*imgHeight)/100;
        Ci = clusterList.next;
        System.out.println(clusterCount+" clusters found in the image. Removing small ones (less than "+minCt+" pixels...");
        state = STATE_COMPACTING;
        updateUI();
        
        j = 0;
        while(Ci != null){
            if(Ci.pixelCount < minCt){
                Cluster delClus = Ci;
                j++;
                Ci = Ci.next;
                clusterCount--;
                delClus.mergeClosest(clusterList);
                continue;
            }
            Ci = Ci.next;
        }
        
        System.out.println(clusterCount+" clusters found in the image.");
        state = STATE_DONE;
        updateUI();
        lblCntClusters.setText(clusterCount + " clusters.");
        String[] clusNames = new String[clusterCount];
        i = 0;
        Ci = clusterList;
        while(Ci != null){
            if(i >= clusterCount)
                break;
            clusNames[i] = "ID"+Ci.clusterID+"; cnt:"+Ci.pixelCount;
            System.out.println(clusNames[i]);
            i++;
            Ci = Ci.next;
        }
        cblClustList.setModel(new DefaultComboBoxModel(clusNames));
        cblClustList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                if(arg0.getStateChange() == ItemEvent.SELECTED){
                    String strObjName = arg0.getItem().toString();
                    strObjName = strObjName.substring(2, strObjName.indexOf(';'));
                    selectedClus = Integer.parseInt(strObjName);
                    //JOptionPane.showMessageDialog(null,"Changed to "+selectedClus,"Selected cluster!",JOptionPane.INFORMATION_MESSAGE);
                    updateUI();
                }
            }
        });
        
        WritableImage imgNova = new WritableImage(imgWidth,imgHeight);
        PixelWriter pw = imgNova.getPixelWriter();
        for(y = 0; y < imgHeight; y++)
            for(x = 0; x < imgWidth; x++){
                Color clr = getPixelCluster(x,y).tone;
                color = javafx.scene.paint.Color.rgb(clr.getRed(), clr.getGreen(), clr.getBlue());
                pw.setColor(x,y,color);
            }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(imgNova, null), "png" , new File("C:\\Temp\\output.png"));
            JOptionPane.showMessageDialog(null,"Image saved at 'C:\\Temp\\output.png'","Work done!",JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            System.out.println("IOException: "+ex.getMessage());
        }  
    }
    public void watershed(){
        WritableImage imgNova = new WritableImage(imgWidth,imgHeight);
        PixelWriter pw = imgNova.getPixelWriter();
        int[][] GX = new int[3][3];
        int[][] GY = new int[3][3];
        int x, y, i, j, r, g, b, res, m;
        /* 3x3 GX Sobel mask.  Ref: www.cee.hw.ac.uk/hipr/html/sobel.html */
        GX[0][0] = -1; GX[0][1] = 0; GX[0][2] = 1;
        GX[1][0] = -2; GX[1][1] = 0; GX[1][2] = 2;
        GX[2][0] = -1; GX[2][1] = 0; GX[2][2] = 1;

        /* 3x3 GY Sobel mask.  Ref: www.cee.hw.ac.uk/hipr/html/sobel.html */
        GY[0][0] =  1; GY[0][1] =  2; GY[0][2] =  1;
        GY[1][0] =  0; GY[1][1] =  0; GY[1][2] =  0;
        GY[2][0] = -1; GY[2][1] = -2; GY[2][2] = -1;
        
        for(y = 0; y < imgHeight; y++){
            for(x = 0; x < imgWidth; x++){
                if(x == 0 || y == 0 || x >= imgWidth-1 || y >= imgHeight-1 )
                    res = 0;
                else{
                    int sumX = 0, sumY = 0;
                    for(i = -1; i <= 1; i++){
                        for(j = -1; j <= 1; j++){
                            javafx.scene.paint.Color color = pixReader.getColor(x+i,y+j);
                            r = (int)(color.getRed()*255);
                            g = (int)(color.getGreen()*255);
                            b = (int)(color.getBlue()*255);
                            m = (30*r+59*g+11*b)/100;
                            sumX += m* GX[i+1][j+1];
                            sumY += m* GY[i+1][j+1];
                        }
                    }
                    if(Math.abs(sumX+sumY) > 128)
                        res = 255;
                    else
                        res = 0;
                }
                javafx.scene.paint.Color newColor = javafx.scene.paint.Color.rgb(res,res,res,1.0);
                pw.setColor(x,y,newColor);
            }
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(imgNova, null), "png" , new File("C:\\Temp\\output.png"));
            System.out.println("Image saved.");
        } catch (IOException ex) {
            System.out.println("IOException: "+ex.getMessage());
        }        
    }
    public void kMeans(){
        int x, y, r, g, b, m, min, max, N, At, k, h;
        min = 256;
        max = 0;
        for(y = 0; y < imgHeight; y++){
            for(x = 0; x < imgWidth; x++){
                javafx.scene.paint.Color color = pixReader.getColor(x,y);
                r = (int)(color.getRed()*255);
                g = (int)(color.getGreen()*255);
                b = (int)(color.getBlue()*255);
                m = (30*r+59*g+11*b)/100;
                if(m < min)
                    min = m;
                else if(m > max)
                    max = m;
            }
        }
        N = imgHeight*imgWidth;
        At = (max-min);
        k = (int)Math.round(Math.sqrt(N));
        double result = (double)At/(double)k;
        h = (int)Math.ceil(result);
        System.out.println("N = "+N);
        System.out.println("At = xmax - xmin = "+max+" - "+min+" = "+At);
        System.out.println("k = ROUND(SQRT(N)) = "+k);
        System.out.println("h = At/k = "+h);
    }
    public static void iniciaSegmentacao(int tipo, javafx.scene.image.Image img) {
        int imgW = (int)img.getWidth();
        int imgH = (int)img.getHeight();
        PixelReader pr = img.getPixelReader();
        SwingUtilities.invokeLater(() -> {
            new Segmentacao(tipo, imgW, imgH, pr);
        });
    }
    public static void iniciaSegmentacao(int tipo, int imgW, int imgH, PixelReader pr) {
        SwingUtilities.invokeLater(() -> {
            new Segmentacao(tipo, imgW, imgH, pr);
        });
    }
}
