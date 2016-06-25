/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
/**
 *
 * @author lfiorentini
 */
public class Visualizacao {
    private HBox box;
    private ImageView imgView;
    private ScrollPane sp;
    private Image img;
    private Button sair; 
    private boolean visivel = false;
    
    public Visualizacao(Image imagem, int largura, int altura){
        //Visualização padrão contém um ScrollPane com a imagem e um botão fechar ao lado.
        //Os dois estarão em um HBox
        box = new HBox();
        Image sa = new Image(getClass().getResourceAsStream("X.png"));
        sair = new Button();
        sair.setGraphic(new ImageView(sa));
        sair.setOnAction(event -> fechaImagem());
        
        box.setMaxSize(largura, altura);
        
        img = imagem;
        imgView = new ImageView();
        imgView.setImage(imagem);
        
        sp = new ScrollPane();
        sp.setContent(imgView);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHmax(largura-sair.getWidth());
        sp.setVmax(altura);
        imgView.getFitHeight();
        imgView.getFitWidth();
        
        box.getChildren().addAll(sp,sair);
    }
    
    public Pane getPane(){
        return (Pane)box;
    }
    public Image getImagem(){
         return img;
    }
    public void defineFechar(EventHandler<ActionEvent> value){
        sair.setOnAction(value);
    }
    public void defineImagem(Image imagem){
        imgView.setImage(imagem);
        img = imagem;
    }
    public void fechaImagem(){
        imgView.setImage(null);
        sp.setContent(null);
        box.getChildren().removeAll(sp,sair);
        box = null;
        imgView = null;
        sp = null;
        img = null;
        sair = null; 
    }
    public void mostraImagem(){
        box.setVisible(true);
        visivel = true;
    }
    public void escondeImagem(){
        box.setVisible(false);
        visivel = false;
    }
    public boolean estaSelecionado(){
         return visivel;
    }
    public void redimensiona(int largura, int altura){
        box.setMaxSize(largura, altura);
        sp.setHmax(largura-sair.getWidth());
        sp.setVmax(altura);
        imgView.getFitHeight();
        imgView.getFitWidth();
        System.out.println("L:"+largura+";A:"+altura);
    }
}
