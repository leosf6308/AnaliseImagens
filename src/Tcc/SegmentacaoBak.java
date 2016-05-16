/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;

import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelReader;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author Leonardo
 */
public class SegmentacaoBak {
    private class Cluster{
        public int clusterID;
        public Cluster next;
        public int pixelCount;
        public int x;
        public int y;
        public int xe;
        public int ye;
        public int average;
        public Color tone;
        private int imgW;
        private int imgH;
        private short[] pixClus;
        public Cluster(int coordX, int coordY, int id, int avgVal, int imgWidth, int imgHeight, short[] pixelCluster){
            x = coordX;
            y = coordY;
            xe = coordX;
            ye = coordY;
            clusterID = id;
            average = avgVal;
            pixelCount = 1;
            imgW = imgWidth;
            imgH = imgHeight;
            pixClus = pixelCluster;
            pixClus[y*imgW+x] = (short)clusterID;
            next = null;
        }
        public void mergePoint(int x, int y, int color){
            double newAvg = (double)average*(double)pixelCount+color;
            pixClus[y*imgW+x] = (short)clusterID;
            if(x < this.x)
                this.x = x;
            else if(x > this.xe)
                this.xe = x;
            if(y < this.y)
                this.y = y;
            else if(y > this.ye)
                this.ye = y;
            pixelCount++;
            newAvg = newAvg/pixelCount;
            //System.out.println("Merged: ("+x+";"+y+"). Cluster "+clusterID+". Pixel count: "+pixelCount+". Average tone: "+average+" ("+color+"; "+newAvg+"). ");
            average = (int)Math.round(newAvg);
            if(average > 255){
                average = 255;
                //System.out.println("Pixel ("+x+","+y+") makes average bigger than 255!");
            }else if(average < 0){
                average = 0;
                //System.out.println("Pixel ("+x+","+y+") makes average lower than zero!");
            }
        }
    }
    
    private int imgWidth;
    private int imgHeight;
    private int clusterCount;
    private int lastID;
    private short[] pixelCluster;
    private Cluster clusterList;
    private PixelReader pixReader;
    public SegmentacaoBak(Image img){
        imgWidth = (int)img.getWidth();
        imgHeight = (int)img.getHeight();
        pixReader = img.getPixelReader();
        clusterList = null;
        pixelCluster = new short[imgWidth*imgHeight];
        clusterCount = 0;
        lastID = 0;
    }
    public SegmentacaoBak(int imgW, int imgH, PixelReader pr){
        imgWidth = imgW;
        imgHeight = imgH;
        pixReader = pr;
        clusterList = null;
        pixelCluster = new short[imgWidth*imgHeight];
        clusterCount = 0;
        lastID = 0;
    }
    private int getPixelClusterID(int x, int y){
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
    private void createCluster(int x, int y, int average){
        int i = 0, clusID = 0;
        Cluster thisClus = clusterList;
        clusID = lastID++;
        if(thisClus != null){
            while(thisClus.next != null){
                /*if(thisClus.clusterID != i && thisClus.clusterID != clusID)
                    clusID = i;*/
                thisClus = thisClus.next;
                i++;
            }
            thisClus.next = new Cluster(x,y,clusID,average,imgWidth,imgHeight,pixelCluster);
        }else{
            clusterList = new Cluster(x,y,0,average,imgWidth,imgHeight,pixelCluster);
        }
        clusterCount++;
    }
    private void mergeClusters(int idC1, int idC2){
        Cluster thisClus = clusterList;
        Cluster C1 = null, C2 = null, c2Bef;
        float heaviness, newAvg;
        int i;
        if(idC1 == idC2)
            return;
        if(idC2 == 0){
            idC2 = idC1;
            idC1 = 0;
        }
            
        while(thisClus != null){
            if(thisClus.clusterID == idC1)
                C1 = thisClus;
            Cluster nextClus = thisClus.next;
            if(nextClus != null && nextClus.clusterID == idC2){
                c2Bef = thisClus;
                C2 = nextClus;
                c2Bef.next = C2.next;
            }
            thisClus = nextClus;
        }
        if(C1 == null || C2 == null)
            return;
        C1.pixelCount += C2.pixelCount;
        heaviness = (float)C2.average/(float)C1.pixelCount;
        newAvg = C1.average+heaviness;
        C1.average = Math.round(newAvg);
        for(i = 0; i < pixelCluster.length; i++){
            if(pixelCluster[i] == idC2)
                pixelCluster[i] = (short)idC1;
        }
        clusterCount--;
    }
    public void DKHFastScanning(){
        int i, j, k, x, y;
        int threshold;
        String inputValue = JOptionPane.showInputDialog("Type a threshold value (or just accept by pressing RETURN):","45");
        threshold = Integer.parseInt(inputValue);
        Color color = pixReader.getColor(0, 0);
        int avg = ((int)(color.getRed()*255)+(int)(color.getGreen()*255)+(int)(color.getBlue()*255))/3;
        Cluster Ci;
        createCluster(0,0,avg);
        for(j = 1; j < imgWidth; j++){
            Ci = getPixelCluster(j-1,0);
            color = pixReader.getColor(j, 0);
            avg = ((int)(color.getRed()*255)+(int)(color.getGreen()*255)+(int)(color.getBlue()*255))/3;
            if( Math.abs(avg-Ci.average) < threshold)
                Ci.mergePoint(j,0,avg);
            else
                createCluster(j,0,avg);
        }
        for(y = 1; y < imgHeight; y++){
            Ci = getPixelCluster(0,y-1);
            color = pixReader.getColor(0, y);
            avg = ((int)(color.getRed()*255)+(int)(color.getGreen()*255)+(int)(color.getBlue()*255))/3;
            if( Math.abs(avg-Ci.average) < threshold)
                Ci.mergePoint(0,y,avg);
            else
                createCluster(0,y,avg);
            for(j = 1; j < imgWidth; j++){
                Cluster Cu = getPixelCluster(j,y-1);
                Cluster Cl = getPixelCluster(j-1,y);
                color = pixReader.getColor(j, y);
                avg = ((int)(color.getRed()*255)+(int)(color.getGreen()*255)+(int)(color.getBlue()*255))/3;
                //lum = (30*(int)(color.getRed()*255)+59*(int)(color.getGreen()*255)+11*(int)(color.getBlue()*255))/100;
                if(Math.abs(avg-Cu.average) < threshold){
                    if(Math.abs(avg-Cl.average) < threshold){
                        Cu.mergePoint(j,y,avg);
                        mergeClusters(Cu.clusterID,Cl.clusterID);
                    }else
                        Cu.mergePoint(j,y,avg);
                }else{
                    if(Math.abs(avg-Cl.average) < threshold)
                        Cl.mergePoint(j, y, avg);
                    else
                        createCluster(j,y,avg);
                }
            }
        }
        Cluster thisClus = clusterList;
        
        int minCt = (imgWidth*imgHeight)/100;
        System.out.println(clusterCount+" clusters found in the image. Removing small ones (less than "+minCt+" pixels...");
        while(thisClus != null){
            if(thisClus.pixelCount < minCt){
                int clusMerge = getPixelClusterID(thisClus.x,thisClus.y);
                if(thisClus.clusterID == clusMerge){
                    i = 1;
                    j = 0;
                    while(true){
                        int c2 = getPixelClusterID(thisClus.x+i,thisClus.y+j);
                        if(c2 != clusMerge){
                            mergeClusters(clusMerge,c2);
                            break;
                        }
                        i++;
                        if(thisClus.x+i > thisClus.xe){
                            j++;
                            if(thisClus.y+j > thisClus.ye){
                                break;
                            }
                        }
                    }
                }else{
                    i = 1;
                    j = 0;
                    int c1 = clusMerge;
                    while(true){
                        int c2 = getPixelClusterID(thisClus.x+i,thisClus.y+j);
                        if(c2 == clusMerge){
                            mergeClusters(clusMerge,c1);
                            break;
                        }
                        c1 = c2;
                        i++;
                        if(thisClus.x+i > thisClus.xe){
                            j++;
                            if(thisClus.y+j > thisClus.ye){
                                break;
                            }
                        }
                    }
                }
                System.out.println("DEL #"+thisClus.clusterID+"; px:"+thisClus.pixelCount+"; avg: "+thisClus.average+
                        "; x: "+thisClus.x+" y:"+thisClus.y+"; xe:"+thisClus.xe+" ye:"+thisClus.ye+"; cm:"+clusMerge);
            }
            thisClus = thisClus.next;
        }
        
        System.out.println(clusterCount+" clusters found in the image.");
        if(clusterCount < 1792){
            if(clusterCount > 28){
                i = 255;
                j = 256/(clusterCount/7);
                k = 0;
            }else{
                i = 255;
                j = 256/clusterCount;
                k = 0;
            }
            thisClus = clusterList;
            while(thisClus != null){
                int w, h;
                double extent;
                w = (thisClus.xe-thisClus.x)+1;
                h = (thisClus.ye-thisClus.y)+1;
                extent = (double)thisClus.pixelCount/(double)(w*h);
                System.out.println("ID"+thisClus.clusterID+";px:"+thisClus.pixelCount+";avg:"+thisClus.average+
                        ";x:"+thisClus.x+";y:"+thisClus.y+";w:"+w+";h:"+h+"; Extent:"+extent);
                switch(k){
                    case 0:
                        thisClus.tone = Color.rgb(i,0,0,1.0);
                        break;
                    case 1:
                        thisClus.tone = Color.rgb(0,i,0,1.0);
                        break;
                    case 2:
                        thisClus.tone = Color.rgb(0,0,i,1.0);
                        break;
                    case 3:
                        thisClus.tone = Color.rgb(i,i,0,1.0);
                        break;
                    case 4:
                        thisClus.tone = Color.rgb(0,i,i,1.0);
                        break;
                    case 5:
                        thisClus.tone = Color.rgb(i,0,i,1.0);
                        break;
                    case 6:
                        thisClus.tone = Color.rgb(i,i,i,1.0);
                        break;
                    default:
                        thisClus.tone = Color.rgb(128,i,i,1.0);
                        break;
                }
                i -= j;
                if(i < j){
                    i = 255;
                    k++;
                }
                thisClus = thisClus.next;
            }
            System.out.println("i = "+i+"; j = "+j+"; k = "+k+";");
        }else{
            thisClus = clusterList;
            int r = 0, g = 0, b = 0;
            while(thisClus != null){
                thisClus.tone = Color.rgb(r,g,b,1.0);
                b++;
                if(b > 255){
                    b = 0;
                    g++;
                    if(g > 255){
                        g = 0;
                        r++;
                    }
                }
                thisClus = thisClus.next;
            }
        }
        
        WritableImage imgNova = new WritableImage(imgWidth,imgHeight);
        PixelWriter pw = imgNova.getPixelWriter();
        for(y = 0; y < imgHeight; y++)
            for(x = 0; x < imgWidth; x++)
                pw.setColor(x,y,getPixelCluster(x,y).tone);
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
                            Color color = pixReader.getColor(x+i,y+j);
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
                Color newColor = Color.rgb(res,res,res,1.0);
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
                Color color = pixReader.getColor(x,y);
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
}
