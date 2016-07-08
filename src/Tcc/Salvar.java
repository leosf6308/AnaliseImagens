package Tcc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javafx.stage.FileChooser;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D; 
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;


/** @author Henrique, Leonardo and Joseph **/
public class Salvar {
    public static void salva(Image img, String path){
        try {
            System.out.println("Saving "+path);
            ImageIO.write(SwingFXUtils.fromFXImage(TCC.visuAtual().getImagem(), null), path.substring(path.length()) , new File(path));
        } catch (IOException ex) {
            System.out.println("IOException (Salvar.java@salva(img,path): "+ex.getMessage());
        }
    }
    public static void salvarComo(){
        try {
            Visualizacao imgAtual = TCC.visuAtual();
            if(imgAtual == null)
                return;
            
            FileChooser fa = new FileChooser();
            fa.setTitle("Salvar Como");
            fa.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imagem JPEG","*.jpg"),
                    new FileChooser.ExtensionFilter("Portable Network Graphics","*.png"),
                    new FileChooser.ExtensionFilter("Imagem de Bitmap","*.bmp")

            );
            
            File outFile = fa.showSaveDialog(null);
            String nomeArq = outFile.toString();
            int i = nomeArq.length()-1;
            while(i > 0){
                if(nomeArq.charAt(i) == '.')
                    break;
                i--;
            }
            i++;
            String extensao = nomeArq.substring(i);
            extensao = extensao.toUpperCase();
            //aRGB,BGRa
            System.out.println("Saving '"+nomeArq+"'. Format");
            String writerNames[] = ImageIO.getWriterFormatNames();
            for(i = 0; i < writerNames.length; i++)
                System.out.print(writerNames[i]+",");
            
            if(extensao.equals("PNG"))
                ImageIO.write(SwingFXUtils.fromFXImage(imgAtual.getImagem(),null), "png" , outFile);
            else if(extensao.equals("BMP")){
                BufferedImage bImage = SwingFXUtils.fromFXImage(imgAtual.getImagem(),null);
                BufferedImage outImg = new BufferedImage(bImage.getWidth(),bImage.getHeight(),BufferedImage.TYPE_INT_BGR);
                Graphics2D graph = outImg.createGraphics();
                graph.setColor(Color.WHITE);
                graph.fillRect(0, 0, outImg.getWidth(), outImg.getHeight());
                
                // draw other things on graph
                graph.drawImage(bImage, null, 0,0);
                graph.dispose();
                
                RenderedImage im = (RenderedImage)outImg;
                ImageIO.write(im, "bmp" , outFile);
            }else if(extensao.equals("JPG")){
                BufferedImage bImage = SwingFXUtils.fromFXImage(imgAtual.getImagem(),null);
                BufferedImage outImg = new BufferedImage(bImage.getWidth(),bImage.getHeight(),BufferedImage.TYPE_INT_BGR);
                Graphics2D graph = outImg.createGraphics();
                graph.setColor(Color.WHITE);
                graph.fillRect(0, 0, outImg.getWidth(), outImg.getHeight());
                
                // draw other things on graph
                graph.drawImage(bImage, null, 0,0);
                graph.dispose();
                
                RenderedImage im = (RenderedImage)outImg;//SwingFXUtils.fromFXImage(TCC.obtemClassImagemAtual().imagem,null);
                ImageIO.write(im, "jpeg" , outFile);
            }else
                System.out.println("Unknwon extension '"+extensao+"'");
            System.out.print("\r\nDone!\r\n");
        } catch (IOException ex) {
            System.out.println("IOException (Salvar.java@salvarComo(): "+ex.getMessage());
        }
    }

}
