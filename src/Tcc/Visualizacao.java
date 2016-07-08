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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.collections.*;
import javafx.scene.control.ComboBox;
/**
 *
 * @author lfiorentini
 */
public class Visualizacao {
    private HBox box;
    private HBox Ferramentas;
    private ImageView imgView;
    private ScrollPane sp;
    private Image img;
    private Button sair; 
    private EventHandler<ActionEvent> VisuRedimensionar;
    private boolean visivel = false;
    
    public Visualizacao(int largura, int altura){
        //Visualização padrão contém um ScrollPane com a imagem e um botão fechar ao lado.
        //Os dois estarão em um HBox
        box = new HBox();
        Image sa = new Image(getClass().getResourceAsStream("X.png"));
        sair = new Button();
        sair.setGraphic(new ImageView(sa));
        sair.setOnAction(event -> fechaImagem());
        
        box.setMaxSize(largura, altura);
        Ferramentas = null;
    }
    
    public Visualizacao(Image imagem, int largura, int altura){
        this(largura,altura);
        setImagem(imagem);
    }
    
    private void criaCaixaFerramentas(){
        VBox caixaVertical = new VBox();
        Ferramentas = new HBox();
        Ferramentas.setStyle("-fx-background-color:#EEEEEE;");
        if(sp != null)
            caixaVertical.getChildren().add(sp);
        caixaVertical.getChildren().add(Ferramentas);
        if(imgView != null){
            imgView.getFitHeight();
            imgView.getFitWidth();
        }
        box.getChildren().clear();
        box.getChildren().addAll(caixaVertical,sair);
    }
    
    public Button adcBotao(String strTitulo, EventHandler<ActionEvent> aoClicar){
        if(Ferramentas == null)
            criaCaixaFerramentas();
        Button btnAdc = new Button();
        btnAdc.setText(strTitulo);
        btnAdc.setAlignment(Pos.BASELINE_CENTER);
        btnAdc.setOnAction(aoClicar);
        Ferramentas.getChildren().add(btnAdc);
        return btnAdc;
    }
    
    public ComboBox adcLista(String[] valores){
        if(Ferramentas == null)
            criaCaixaFerramentas();
        ObservableList<String> options = FXCollections.observableArrayList(valores);
        ComboBox lista = new ComboBox(options);
        Ferramentas.getChildren().add(lista);
        return lista;
    }
    
    public Node adcDeslizante(){
        return null;
    }
    
    public Text adcTextoEstatico(String texto){
        if(Ferramentas == null)
            criaCaixaFerramentas();
        Text cxTexto = new Text();
        cxTexto.setText(texto);
        cxTexto.setStyle("-fx-background-color:#DDDDDD;");
        Ferramentas.getChildren().add(cxTexto);
        return cxTexto;
    }
    
    public void adcBotaoImagem(Image icone, EventHandler<ActionEvent> aoClicar){
        if(Ferramentas == null)
            criaCaixaFerramentas();
        Button btnAdc = new Button();
        btnAdc.setGraphic(new ImageView(icone));
        btnAdc.setAlignment(Pos.BASELINE_CENTER);
        btnAdc.setOnAction(aoClicar);
        Ferramentas.getChildren().add(btnAdc);
    }
    
    public void limpaFerramentas(){
        Ferramentas.getChildren().clear();
        Ferramentas = null;
        box.getChildren().clear();
        if(sp != null)
            box.getChildren().add(sp);
        box.getChildren().add(sair);
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
    private void setImagem(Image imagem){
        /**
         * Verificar se já existe o StackPane e o imgView
         * Se não existir, criar estes itens e adicionar ao box
         */
        img = imagem;
        if(sp == null){
            img = imagem;
            imgView = new ImageView();
            imgView.setImage(imagem);

            sp = new ScrollPane();
            sp.setContent(imgView);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sp.setHmax(box.getMaxWidth()-sair.getWidth());
            sp.setVmax(box.getMaxHeight());
            imgView.getFitHeight();
            imgView.getFitWidth();
            
            box.getChildren().clear();
            if(Ferramentas != null){
                VBox caixaVertical = new VBox();
                caixaVertical.getChildren().addAll(sp,Ferramentas);
                box.getChildren().addAll(caixaVertical,sair);
            }else
                box.getChildren().addAll(sp,sair);
        }else{
            imgView.setImage(imagem);
            imgView.getFitHeight();
            imgView.getFitWidth();
        }
    }
    public void defineImagem(Image imagem){
        setImagem(imagem);
    }
    public void fechaImagem(){
        if(imgView != null)
            imgView.setImage(null);
        if(sp != null)
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
        if(sp != null){
            sp.setHmax(largura-sair.getWidth());
            sp.setVmax(altura);
            imgView.getFitHeight();
            imgView.getFitWidth();
        }
        System.out.println("L:"+largura+";A:"+altura);
    }
}
