package Tcc;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/** @author Henrique, Leonardo and Joseph**/
public class Aviso {
    Stage aviso;
    
    public Aviso(){
       aviso = new Stage();
       Group aparencia = new Group();
       aviso.setTitle("AVISO");
       aviso.show();
       aviso.setMaxHeight(208);
       aviso.setMaxWidth(480);
       aviso.centerOnScreen();
       
       Scene cena = new Scene(aparencia,100,350,Color.LIGHTGRAY);
       aviso.setScene(cena);

       Label info = new Label("Você só pode abrir até 6 imagens por vez!!");
       info.setFont(Font.font("Cooper Black", 20));
       info.setAlignment(Pos.CENTER);
       Image imagem = new Image(getClass().getResourceAsStream("Alert.jpg"));
       ImageView img = new ImageView(imagem);
       img.setLayoutX(178);
       img.setLayoutY(30);
       Button ok = new Button("Ok");
       ok.setPrefSize(50, 10);
       ok.setLayoutY(135);
       ok.setLayoutX(200);
       HBox h = new HBox();
       h.getChildren().addAll(info);
       h.setPrefSize(480, 100);
       h.setLayoutX(20);
       aparencia.getChildren().addAll(h,img,ok);
     
       ok.setOnAction(new EventHandler<ActionEvent>() {  
            @Override  
            public void handle(ActionEvent event1) {  
              aviso.close();
            }
        });
    }
}
