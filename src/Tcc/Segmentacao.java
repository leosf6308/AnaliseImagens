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
import java.util.ArrayList;
import java.awt.Point;
/**
 *
 * @author Leonardo
 */


public class Segmentacao extends JFrame{
    private class SquareCorners{
        Point NW; //Top left
        Point NE; //Top right
        Point SW; //Bottom left
        Point SE; //Bottom right
    }
    private class VisuSegm extends JPanel{
        public static final float PADDING = 5.0f;
        public static final float EPISLON = 0.00000001f;
        private Segmentacao segmDados;
        private BufferedImage canvas;
        private int x, y;
        public VisuSegm(Segmentacao dados){
            super();
            this.setSize(800,600);
            segmDados = dados;
            x = y = 0;
            canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
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
                SquareCorners box = segmDados.getCluster(segmDados.selectedClus).detectShape();
                if(box != null){
                    Graphics2D graph = canvas.createGraphics();
                    graph.setColor(Color.WHITE);
                    graph.fillOval(box.NW.x-4, box.NW.y-4, 8, 8);
                    graph.fillOval(box.NE.x-4, box.NE.y-4, 8, 8);
                    graph.fillOval(box.SW.x-4, box.SW.y-4, 8, 8);
                    graph.fillOval(box.SE.x-4, box.SE.y-4, 8, 8);
                    graph.drawLine(box.NW.x, box.NW.y, box.NE.x, box.NE.y);
                    graph.drawLine(box.NE.x, box.NE.y, box.SE.x, box.SE.y);
                    graph.drawLine(box.SW.x, box.SW.y, box.NW.x, box.NW.y);
                    graph.drawLine(box.SE.x, box.SE.y, box.NW.x, box.NW.y);
                }
                g2.drawImage(canvas, null, null);
                System.out.println(""+w+"x"+h);
            }
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
            while(head != null && head.next != c2)
                head = head.next;
            head.next = c2.next;
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
        public SquareCorners detectShape(){
            SquareCorners box = new SquareCorners();
            int i, thisPx, pos, cnt, max;
            thisPx = top*imgW+left;
            pos = 0;
            max = (right-left)/10;
            cnt = 0;
            for(i = left; i < right; i++){
                if(pixClus[thisPx] == clusterID){
                    if(pos == 0)
                        pos = i;
                    else{
                        cnt = i-pos;
                        if(cnt > max){
                            System.out.println("Edge is too big: s:"+pos+"x"+top+". l:"+cnt);
                            return null;
                        }
                    }
                }
                thisPx++;
            }
            if(pos != 0){
                box.NW = new Point(pos+(cnt/2), top);
                System.out.println("NW edge found at: s:"+pos+"x"+top+". l:"+cnt);
            }else
                return null;
            
            thisPx = top*imgW+left;
            pos = 0;
            max = (bottom-top)/10;
            cnt = 0;
            for(i = top; i < bottom; i++){
                if(pixClus[thisPx] == clusterID){
                    if(pos == 0)
                        pos = i;
                    else{
                        cnt = i-pos;
                        if(cnt > max){
                            System.out.println("Edge is too big: s:"+left+"x"+pos+". l:"+cnt);
                            return null;
                        }
                    }
                }
                thisPx += imgW;
            }
            if(pos != 0){
                box.SW = new Point(left, pos+(cnt/2));
                System.out.println("SW edge found at: s:"+left+"x"+pos+". l:"+cnt);
            }else
                return null;
            
            thisPx = (bottom-1)*imgW+left;
            pos = 0;
            max = (right-left)/10;
            cnt = 0;
            for(i = left; i < right; i++){
                if(pixClus[thisPx] == clusterID){
                    if(pos == 0)
                        pos = i;
                    else{
                        cnt = i-pos;
                        if(cnt > max){
                            System.out.println("Edge is too big: s:"+pos+"x"+bottom+". l:"+cnt);
                            return null;
                        }
                    }
                }
                thisPx++;
            }
            if(pos != 0){
                box.SE = new Point(pos+(cnt/2), bottom);
                System.out.println("SE edge found at: s:"+pos+"x"+bottom+". l:"+cnt);
            }else
                return null;
            
            thisPx = top*imgW+(right-1);
            pos = 0;
            max = (bottom-top)/10;
            cnt = 0;
            for(i = top; i < bottom; i++){
                if(pixClus[thisPx] == clusterID){
                    if(pos == 0)
                        pos = i;
                    else{
                        cnt = i-pos;
                        if(cnt > max){
                            System.out.println("Edge is too big: s:"+left+"x"+pos+". l:"+cnt);
                            return null;
                        }
                    }
                }
                thisPx += imgW;
            }
            if(pos != 0){
                box.NE = new Point(right, pos+(cnt/2));
                System.out.println("NE edge found at: s:"+left+"x"+pos+". l:"+cnt);
            }else
                return null;
            
            return box;
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
    private JLabel lblClusInf;
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
        lblClusInf = new JLabel("Select a cluster");
        cblClustList = new JComboBox(strClusNames);
        janela.setLayout(null);
        janela.add(lblCntClusters);
        janela.add(lblClusInf);
        janela.add(cblClustList);
        janela.add(drawPane);
        
        lblCntClusters.setFont(this.getFont());
        lblCntClusters.setForeground(this.getForeground());
        size = lblCntClusters.getPreferredSize();
        lblCntClusters.setBounds(insets.left,insets.top+3, size.width, size.height);
        
        lblClusInf.setFont(this.getFont());
        lblClusInf.setForeground(this.getForeground());
        size = lblClusInf.getPreferredSize();
        lblClusInf.setBounds(lblCntClusters.getWidth()+insets.left,insets.top+3, size.width+5, size.height);
        
        size = cblClustList.getPreferredSize();
        cblClustList.setBounds(lblCntClusters.getWidth()+lblClusInf.getWidth()+insets.left,
                insets.top, size.width, size.height);
        
        drawPane.setBounds(insets.left,insets.top+size.height, drawPane.getWidth(), drawPane.getHeight());
        
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
        //https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#LabelDemo
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
                lastClus.next = Ci;
                lastClus = Ci;
                clusterCount++;
            }
        }
        
        for(i = 1; i < imgHeight; i++){
            Ci = getPixelCluster(0,i-1);
            color = pixReader.getColor(i, 0);
            thisPx = new Color((int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255));
            dist = colorDistance(Ci.tone,thisPx);
            if(dist < threshold)
                Ci.mergePoint(0, i, thisPx);
            else{
                Ci = new Cluster(0,i,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
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
                                    findLast = true;
                            }else{
                                Cl.mergeCluster(Cu, clusterList);
                                if(Cu == lastClus)
                                    findLast = true;
                            }
                            
                            if(findLast){
                                System.out.println("Recomputing cluster list last item...");
                                lastClus = clusterList;
                                while(lastClus != null && lastClus.next != null)
                                    lastClus = lastClus.next;
                            }
                        }
                    }
                }else{
                    dist = colorDistance(Cl.tone,thisPx);
                    if(dist < threshold)
                        Cl.mergePoint(j, i, thisPx);
                    else{
                        Ci = new Cluster(j,i,++lastID,thisPx,imgWidth,imgHeight,pixelCluster);
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
                    Cluster tClus = clusterList;
                    while(tClus != null){
                        if(tClus.clusterID == selectedClus)
                            break;
                        tClus = tClus.next;
                    }
                    lblClusInf.setText("ID"+tClus.clusterID+"; x:"+tClus.left+"; y:"+tClus.top+"; w:"+(tClus.right-tClus.top)+"; h:"+(tClus.bottom-tClus.left)+" cnt:"+tClus.pixelCount+"; avg:"+tClus.tone);
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
