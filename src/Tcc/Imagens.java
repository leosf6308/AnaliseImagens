package Tcc;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import static Tcc.TCC.caminho;
import static Tcc.TCC.principal;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

/** @author Henrique,Leonardo and Joseph **/

public class Imagens {
    public Image imagem, sta;
    public Button sair, st; 
    public StackPane stack;
    public HBox box;
    public VBox atalhos;
    public ImageView img;
    public boolean visivel = false;
    public Image sa;
    public ScrollPane sp;
    private void criaVisuImg(Image imagem, Boolean eVideo){
        //StackPane propriedades para imagens
        int idImg;
        for(idImg = 0; idImg < 6; idImg++){
            if(TCC.usuarioImgs[idImg] == null)
                break;
            else{
                TCC.usuarioImgs[idImg].box.setVisible(false);
                TCC.usuarioImgs[idImg].sair.setVisible(false);
                TCC.usuarioImgs[idImg].visivel = false;
            }
        }
        
        final int idControle = idImg;
        if(idControle >= 6 || idControle == -1){
            System.out.println("FATAL: Não há mais espaço para imagens!");
            return;
        }
        stack = new StackPane();
        atalhos = new VBox(); 
        st = new Button();
        if(eVideo)
            sta = new Image(getClass().getResourceAsStream("Video.png"));
        else
            sta = new Image(getClass().getResourceAsStream("Imagem.png"));
        st.setGraphic(new ImageView(sta));
        st.setMaxSize(100,100);
        stack.getChildren().add(st);
        atalhos.getChildren().add(stack);
        visivel = true;
        TCC.cont++;
        double posY = (double)(idControle*110+25);
        stack.setLayoutY(posY);
        atalhos.setLayoutY(posY);
        
        img = new ImageView();
        img.setImage(imagem);
        
        //Criando e adicionando a barra de rolagem na imagem se necessário
        sp = new ScrollPane();
        sp.setContent(img);
        sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        sp.setHmax(800);
        sp.setVmax(780);
        img.getFitHeight();
        img.getFitWidth();
        
        //Criando a caixa que exibirá a imagem, barra de rolagem e o botão que fecha a imagem
        box = new HBox();
        box.setMaxSize(1100, 610);
        box.setLayoutX(121);
        box.setLayoutY(25);
                
        //sair do stackpane
        sa = new Image(getClass().getResourceAsStream("X.png"));
        sair = new Button();
        sair.setGraphic(new ImageView(sa));
        sair.setLayoutY(900);
        sair.setOnAction(event -> fechaImagem());
        
        //link para abrir a imagem previamente aberta
        st.setOnAction(event -> ativaImagem());
                
        //Adicionamdo todas as coisas ao Scene        
        box.getChildren().addAll(img,sp,sair);
        principal.getChildren().addAll(atalhos,box);
        TCC.usuarioImgs[idImg] = this;
        System.out.println("Criado item "+idControle+"; "+this.toString()+"; "+box.toString()+" (Imagens.java@Imagens())");
    }
            
    public Imagens(){
        
        //Abrindo a imagem
        if(caminho == null){
            System.out.println("Erro fatal: Qual é o caminho?????");
            return;
        }
        
        caminho = caminho.replaceAll("[\\\\]", "/");
        caminho = caminho.replaceAll("[ ]", "%20");
        
        int i = caminho.length()-1;
        while(i > 0){
            if(caminho.charAt(i) == '.')
                break;
            i--;
        }
        
        String extensao = caminho.substring(i);
        extensao = extensao.toUpperCase();
        
        //Verificar se é vídeo ou imagem.
        if(extensao.equals(".FLV") || extensao.equals(".AVI") || extensao.equals(".MP4")){
            Player vidPlayer = TCC.vidPlayer;
            imagem = vidPlayer.view.snapshot(new SnapshotParameters(), new WritableImage(vidPlayer.w,vidPlayer.h));
            criaVisuImg(imagem,true);
        }else{
            String exibe = "file:/"+caminho;
            imagem = new Image(exibe);
            criaVisuImg(imagem,false);
        }
    }
    
    public Imagens(BufferedImage buffImg){
        imagem = SwingFXUtils.toFXImage(buffImg, null);
        criaVisuImg(imagem,true);
    }
    
    public final void ativaImagem(){
        int i;
        for(i = 0; i < 6; i++){
            if(TCC.usuarioImgs[i] != null){
                TCC.usuarioImgs[i].box.setVisible(false);
                TCC.usuarioImgs[i].sair.setVisible(false);
                TCC.usuarioImgs[i].visivel = false;
                System.out.println("\tEscondendo "+i+"; "+TCC.usuarioImgs[i].toString()+"; "+TCC.usuarioImgs[i].box.toString());
            }
        }
        box.setVisible(true);
        sair.setVisible(true);
        visivel = true;
        System.out.println("Mostrando "+this.toString()+"; "+box.toString()+" (Imagens.java@ativaImagem())");
    }
    
    public final void fechaImagem(){
        boolean itemVisivel = true;
        int i;
        TCC.cont--;
        for(i = 0; i < 6; i++){
            if(TCC.usuarioImgs[i] != null){
                TCC.usuarioImgs[i].box.setVisible(itemVisivel);
                TCC.usuarioImgs[i].sair.setVisible(itemVisivel);
                TCC.usuarioImgs[i].visivel = itemVisivel;
                if(TCC.usuarioImgs[i] == this){
                    principal.getChildren().removeAll(TCC.usuarioImgs[i].atalhos,TCC.usuarioImgs[i].box);
                    System.out.println("Deletando "+i+"; "+this.toString()+"; "+box.toString()+" (Imagens.java@fechaImagem())");
                    TCC.usuarioImgs[i] = null;
                }else
                    itemVisivel = false;
            }
        }
    }
}
