package Tcc;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.swing.JOptionPane;
import javafx.scene.image.Image;

/** @author Henrique, Joseph and Leonardo **/

public class tratamentoImagens {
    public static int cont=1; 
    
    public static Image tonsdeCinza(Image imgAtual){
        //System.out.println("Aplicando cinza em "+imgAtual.toString()+"; "+imgAtual.box.toString());
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage nova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = nova.getPixelWriter();
        
        for(int h=0;h<imgAtual.getHeight();h++){
            for(int w=0;w<imgAtual.getWidth();w++){
               Color color = pr.getColor(w,h);
               color = color.grayscale();
               pw.setColor(w,h,color);
            }
        }
        return (Image)nova;
    }
    
    public static Image brighter(Image imgAtual){
        
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();

        for(int h=0;h<imgAtual.getHeight();h++){
            for(int w=0;w<imgAtual.getWidth();w++){
               Color color = pr.getColor(w,h);

               color = color.brighter();
               pw.setColor(w,h,color);

            }
        }
        return (Image)imgNova;
    }
    
    public static Image darker(Image imgAtual){
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();

        for(int h=0;h<imgAtual.getHeight();h++){
            for(int w=0;w<imgAtual.getWidth();w++){
               Color color = pr.getColor(w,h);
               color = color.darker();
               pw.setColor(w,h,color);
            }
        }
        return (Image)imgNova;
    }
    
    public static Image negativo(Image imgAtual){
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();

        for(int h=0;h<imgAtual.getHeight();h++){
            for(int w=0;w<imgAtual.getWidth();w++){
               Color color = pr.getColor(w,h);

               color = color.invert();
               pw.setColor(w,h,color);

            }
        }
        return (Image)imgNova;
    }
    
    public static Image sobelOperator(Image imgAtual, Boolean isGradient){
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();
        int[][] GX = new int[3][3];
        int[][] GY = new int[3][3];
        int x, y, i, j, r, g, b, res, width, height;
        /* 3x3 GX Sobel mask.  Ref: www.cee.hw.ac.uk/hipr/html/sobel.html */
        GX[0][0] = -1; GX[0][1] = 0; GX[0][2] = 1;
        GX[1][0] = -2; GX[1][1] = 0; GX[1][2] = 2;
        GX[2][0] = -1; GX[2][1] = 0; GX[2][2] = 1;

        /* 3x3 GY Sobel mask.  Ref: www.cee.hw.ac.uk/hipr/html/sobel.html */
        GY[0][0] =  1; GY[0][1] =  2; GY[0][2] =  1;
        GY[1][0] =  0; GY[1][1] =  0; GY[1][2] =  0;
        GY[2][0] = -1; GY[2][1] = -2; GY[2][2] = -1;
        
        width = (int)imgAtual.getWidth();
        height = (int)imgAtual.getHeight();
        for(y = 0; y < height; y++){
            for(x = 0; x < width; x++){
                if(x == 0 || y == 0 || x >= width-1 || y >= height-1 ){
                    if(isGradient)
                        res = 128;
                    else
                        res = 0;
                }else{
                    int sumX = 0, sumY = 0;
                    for(i = -1; i <= 1; i++){
                        for(j = -1; j <= 1; j++){
                            Color color = pr.getColor(x+i,y+j);
                            r = (int)(color.getRed()*255);
                            g = (int)(color.getGreen()*255);
                            b = (int)(color.getBlue()*255);
                            sumX += ((r+g+b)/3)* GX[i+1][j+1];
                            sumY += ((r+g+b)/3)* GY[i+1][j+1];
                        }
                    }
                    if(isGradient){
                        //res = sumX+sumY;
                        float percent = ((sumX+sumY)/4.0f)/255.0f;
                        if(percent > 100.0f)
                            percent = 100.0f;
                        res = 128+ (int)(percent*128.0f);
                    }else{
                        res = Math.abs(sumX)+Math.abs(sumY);
                        if(res > 255)
                            res = 255;
                    }
                }
                if(res < 0){
                    System.out.println("[WARNING]Got a negative value: "+res);
                    res = 0;
                }else if(res > 255){
                    System.out.println("[WARNING]Got a way too big value: "+res);
                    res = 255;
                }
                Color newColor = Color.rgb(res,res,res,1.0);
                pw.setColor(x,y,newColor);
            }
        }
        return (Image)imgNova;
    }
    
    public static Image detecaoBorda(Image imgAtual){
        return sobelOperator(imgAtual, false);
    }
    
    public static Image gradiente(Image imgAtual){
        return sobelOperator(imgAtual, true);
    }
    
    public static int[] geraHistograma(PixelReader pr, int width, int height){
        int[] histograma = new int[256];
        int i, x, y, r, g, b, m;
        
        for(i = 0; i < 256; i++)
            histograma[i] = 0;
        for(y = 0; y < height; y++){
            for(x = 0; x < width; x++){
                Color color = pr.getColor(x,y);
                r = (int)(color.getRed()*255);
                g = (int)(color.getGreen()*255);
                b = (int)(color.getBlue()*255);
                m = (30*r+59*g+11*b)/100;
                histograma[m]++;
            }
        }
        return histograma;
    }
    
    public static Image eqHistograma(Image imgAtual){
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();
        
        int i, j, x, y, width, height, r, g, b, m, size;
        double alpha;
        int[] histograma = geraHistograma(pr,(int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        int[] lookup = new int[256];
        width = (int)imgAtual.getWidth();
        height = (int)imgAtual.getHeight();
        size = width*height;
        alpha = 255.0/size;
        
        lookup[0] = histograma[0];
        for(i = 1; i < 256; i++)
            lookup[i] = histograma[i] + lookup[i-1];
        //    lookup[i] = (int)(lookup[i-1] + alpha*histograma[i]);
        for(i = 0; i < 256; i++){
            lookup[i] = (int)Math.round(lookup[i]*alpha);
            if(lookup[i] > 255){
                System.out.print(histograma[i]+"; "+lookup[i]);
                lookup[i] = 255;
            }else if(lookup[i] < 0){
                System.out.print(histograma[i]+"; "+lookup[i]);
                lookup[i] = 0;
            }
        }
        for(y = 0; y < height; y++){
            for(x = 0; x < width; x++){
                Color color = pr.getColor(x,y);
                r = (int)(color.getRed()*255);
                g = (int)(color.getGreen()*255);
                b = (int)(color.getBlue()*255);
                m = (30*r+59*g+11*b)/100;
                m = lookup[m];
                Color newColor = Color.rgb(m,m,m,1.0);
                pw.setColor(x,y,newColor);
            }
        }
        return (Image)imgNova;
    }
        
    public static void exibeHistograma(){
        Visualizacao imgAtual = TCC.visuAtual();
        if(imgAtual == null)
            return;
        
        PixelReader pr = imgAtual.getImagem().getPixelReader();
        int i;
        int[] histograma = geraHistograma(pr,(int)imgAtual.getImagem().getWidth(),(int)imgAtual.getImagem().getHeight());
        float[] xAxis = new float[256];
        float[] yAxis = new float[256];
        for(i = 0; i < 256; i++){
            xAxis[i] = i;
            yAxis[i] = (float)histograma[i];
        }
        
        new Graficos(xAxis,yAxis);
    }
    
    public static Image segmentacaoLimiar(Image imgAtual, PixelReader pr, int limiar){
        WritableImage newImage = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = newImage.getPixelWriter();
        
        int i, x, y, width, height, r, g, b, m;
        width = (int)imgAtual.getWidth();
        height = (int)imgAtual.getHeight();
        System.out.println("Segmenting current image by threshold "+limiar);
        for(y = 0; y < height; y++){
            for(x = 0; x < width; x++){
                Color color = pr.getColor(x,y);
                r = (int)(color.getRed()*255);
                g = (int)(color.getGreen()*255);
                b = (int)(color.getBlue()*255);
                m = (30*r+59*g+11*b)/100;
                if(m > limiar)
                    pw.setColor(x, y, Color.WHITE);
                else
                    pw.setColor(x, y, Color.BLACK);
            }
        }
        return newImage;
    }
    
    public static Image segmentacaoOtsu(Image imgAtual){
        //http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html
        
        PixelReader pr = imgAtual.getPixelReader();
        
        int[] histograma = geraHistograma(pr,(int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        int i, x, y, width, height, size;
        
        width = (int)imgAtual.getWidth();
        height = (int)imgAtual.getHeight();
        size = width*height;
        
        //Utilizando o método de Otsu.
        float soma = 0, somaB = 0;
        for(i = 0; i < 256; i++)
            soma += i*histograma[i];
        int wF = 0;//weight foreground
        int wB = 0;//weight background
        float mB, mF;
        float max = 0.0f;
        float meio;
        int limiar = 0;
        for(i = 0; i < 256; i++){
            wB += histograma[i];
            if(wB == 0)
                continue;
            wF = size-wB;
            if(wF == 0)
                break;
            somaB += i*histograma[i];
            mB = somaB/wB;
            mF = (soma-somaB)/wF;
            meio = wB*wF*(mB-mF)*(mB-mF);
            if(meio > max){
                max = meio;
                limiar = i;
            }
        }
        return segmentacaoLimiar(imgAtual,pr,(int)limiar);
    }
    
    public static Image shapeDetector(Image imgAtual){
        //http://www.propulsion-analysis.com/screenshots.htm
        //http://www.edgeofspace.org/intro.htm
        //http://www.ufabc.edu.br/
        //http://www.spl.ch/
        
        //http://www.cs.cmu.edu/~stein/nsf_webpage/
        //https://en.wikipedia.org/wiki/Canny_edge_detector
        /*
        *recognizeBoundaries:
	test all eight surroundings
	if less than 2 zeros, inside point (no connection)
        else able to connect
        */
        
        PixelReader pr = imgAtual.getPixelReader();
        WritableImage imgNova = new WritableImage((int)imgAtual.getWidth(),(int)imgAtual.getHeight());
        PixelWriter pw = imgNova.getPixelWriter();
        
        int i, j, x, y, width, height, r, g, b, m, size;
        width = (int)imgAtual.getWidth();
        height = (int)imgAtual.getHeight();
        size = width*height;
     
        for(y = 0; y < height; y++){
            for(x = 0; x < width; x++){
                Color color = pr.getColor(x,y);
                Color newColor;
                if(((int)color.getRed()) == 0)
                    newColor = Color.rgb(0,0,0,1.0);
                else{
                    Color cN = pr.getColor(x,y-1);
                    Color cS = pr.getColor(x,y+1);
                    Color cW = pr.getColor(x+1,y);
                    Color cE = pr.getColor(x-1,y);
                    Color cNW = pr.getColor(x+1,y-1);
                    Color cNE = pr.getColor(x-1,y-1);
                    Color cSW = pr.getColor(x+1,y+1);
                    Color cSE = pr.getColor(x-1,y-1);
                    if(((int)cN.getRed()) == 0 || ((int)cS.getRed()) == 0 || ((int)cW.getRed()) == 0 || ((int)cE.getRed()) == 0){
                        if(((int)cNW.getRed()) == 0 || ((int)cNE.getRed()) == 0 || ((int)cSW.getRed()) == 0 || ((int)cSE.getRed()) == 0)
                            newColor = Color.rgb(255,255,255,1.0);
                        else
                            newColor = Color.rgb(0,0,0,1.0);
                    }else{
                        newColor = Color.rgb(0,0,0,1.0);
                    }
                }
                pw.setColor(x,y,newColor);
            }
        }
        
        return imgNova;
    }
}