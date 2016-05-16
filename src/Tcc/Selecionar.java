 package Tcc;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
 
/* @author Henrique, Leonardo and Joseph*/


public class Selecionar {
    public static FileChooser fc;
    
    public static String selecionar() {
        try{
            fc = new FileChooser();
            fc.setTitle("Abrir");
            ExtensionFilter imagem = new ExtensionFilter("Imagem","*.jpg","*.jpeg","*.gif","*.png");
            ExtensionFilter video = new ExtensionFilter("Video","*.flv","*.avi","*.mp4");
            ExtensionFilter todos = new ExtensionFilter("Todos os suportados","*.jpg","*.jpeg","*.gif","*.png","*.flv","*.avi","*.mp4");
            fc.getExtensionFilters().addAll(imagem,video,todos);
            fc.setSelectedExtensionFilter(todos);
            File file = fc.showOpenDialog(null);
            return file.getAbsolutePath();
        }catch (Exception e){
            System.out.println("Exception (Selecionar.java@selecionar): "+e.getMessage());
        }
        return null;
    }
}