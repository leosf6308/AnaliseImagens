package Tcc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import static Tcc.TCC.principal;


/**
 *
 * @author Henrique
 */
public class framesVideos {
    
    public static void frames(){
       /* try{
            String video = "ffplay -i "+TCC.caminho;//+" image%d.jpg";
            System.out.print(video);
            Process p = Runtime.getRuntime().exec(video);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            
        }
        catch(IOException ex){
            Logger.getLogger(framesVideos.class.getName()).log(Level.SEVERE,null,ex);
        }*/
        Player vidPlayer = TCC.vidPlayer;
        HBox mini = new HBox();
        mini.setPrefSize(480, 290);
        for (int i = vidPlayer.pocatek; i < (int) vidPlayer.player.getTotalDuration().toMillis(); i += vidPlayer.posun) {
            //System.out.println(i);
            vidPlayer.cas = new Duration(i);
            //player.seek(cas);

            TCC.obtemClassImagemAtual().defineImagem(vidPlayer.view.snapshot(new SnapshotParameters(), new WritableImage(vidPlayer.w,vidPlayer.h)));
            //WritableImage wi = new WritableImage(1000, 1000);
            //Player.view.snapshot(new SnapshotParameters(), wi);

            //Color c = image.getPixelReader().getColor(100, 100);
            //System.out.println(c);
            // video.snapshot(params, image);

            vidPlayer.im = new ImageView(TCC.obtemClassImagemAtual().getImagem());
            vidPlayer.im.setFitHeight(290);
            vidPlayer.im.setFitWidth(480);
            mini.getChildren().add(vidPlayer.im);
            principal.getChildren().add(mini);
        }
        ScrollPane sp = new ScrollPane();
        sp.setContent(vidPlayer.im);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHmax(480);
        sp.setVmax(290);
        
    }
}