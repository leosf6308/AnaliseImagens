package Tcc;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;

/*@author Henrique, Leonardo and Joseph*/

public class TCC extends Application{
    public static String caminho;
    public static Group principal = new Group();
    public static int cont =0, lay=25;
    public static Player vidPlayer = null;
    public final static Imagens usuarioImgs[] = new Imagens[6];
        
    public static void main(String[] args){
       launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {    
        //Fecha de vez tudo
        stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event){
               Platform.exit();
            }
        });
        
        //Janela
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        /*if(w < screenSize.width)
            w = screenSize.width;
        if(h < screenSize.height)
            h = screenSize.height;*/
        Scene janela = new Scene(principal, 1290/*largura ou X*/, 680/*altura ou Y*/, Color.LIGHTGREY);
        stage.setTitle("TCC");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Foguete.png")));
        stage.setMaximized(true);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);
                
        //Menu
        MenuBar menu = new MenuBar();
        VBox caixa = new VBox();
        caixa.setMinSize(1920, 1080);
        caixa.getChildren().add(menu);
        
        
        //arquivo
        Menu arquivo = new Menu("_Arquivo");
        arquivo.setAccelerator(
            KeyCombination.keyCombination("A")
        );
        MenuItem abrir = new MenuItem("Ab_rir");
        abrir.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+R")
        );
        abrir.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event3){
                caminho = Selecionar.selecionar();
                if(caminho == null)
                    return;
                //try {
                    int i = caminho.length()-1;
                    while(i > 0){
                        if(caminho.charAt(i) == '.')
                            break;
                        i--;
                    }
                    String extensao = caminho.substring(i);
                    extensao = extensao.toUpperCase();
                    System.out.println("Arquivo: '"+caminho+"'; Extensão: '"+extensao+"' (TCC.java@start()@abrir.setOnAction)");

                    //Verificar se é vídeo ou imagem.
                    if(extensao.equals(".FLV") || extensao.equals(".AVI") || extensao.equals(".MP4")){
                        vidPlayer = new Player();
                    } else {
                        if(cont>=6){
                            //new Aviso();
                            JOptionPane.showMessageDialog(null, "Só podem existir 6 imagens abertas ao mesmo tempo.", "Aviso!", JOptionPane.INFORMATION_MESSAGE);
                        }else{
                            new Imagens();                                     
                        }
                    }
                /*}
                catch (Exception e){
                    System.out.println("Exception (TCC.java@start()@abrir.setOnAction): "+e.getMessage());
                }*/
            }
        });

        //salvar
        MenuItem salvar = new MenuItem("_Salvar");
        salvar.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+S")
        );
        salvar.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                try {
                    int i = caminho.length()-1;
                    while(i > 0){
                        if(caminho.charAt(i) == '.')
                            break;
                        i--;
                    }

                    String extensao = caminho.substring(i);
                    extensao = extensao.toUpperCase();
                    System.out.println("; extensão: "+extensao);

                    //Verificar se é vídeo ou imagem.
                    if(extensao.equals(".FLV") || extensao.equals(".AVI") || extensao.equals(".MP4")){
                        Salvar.salvarComo();
                    }else{
                        Imagens imgAtual = TCC.obtemClassImagemAtual();
                        if(imgAtual == null)
                            return;
                        Salvar.salva(imgAtual.imagem,caminho);
                    }
                }
                catch(Exception e){
                    System.out.println("Exception (TCC.java@start()@salvar.setOnAction): "+e.getMessage());
                }
            }
        });
        
        //salvarcomo
        MenuItem como = new MenuItem("Salvar C_omo");
        como.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+O")
        );
        como.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               Salvar.salvarComo(); 
            }
        });
        arquivo.getItems().addAll(abrir,salvar,como);
        
        //Exibir
        Menu exibir = new Menu("_Exibir");
        /*filtros.setAccelerator(
            KeyCombination.keyCombination("E")
        );*/
        //preto e branco
        MenuItem histograma = new MenuItem("_Histograma");
        histograma.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.exibeHistograma();
            }
        });
        exibir.getItems().add(histograma);
        
        //filtros
        Menu filtros = new Menu("_Filtros");
        filtros.setAccelerator(
            KeyCombination.keyCombination("F")
        );
        //preto e branco
        MenuItem preto = new MenuItem("_Preto & Branco");
        preto.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+P")
        );
        preto.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.tonsdeCinza();
            }
        });
        //brighter
        MenuItem brighter = new MenuItem("_Brighter");
        brighter.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+B")
        );
        brighter.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.brighter();
            }
        });
        //darker
        MenuItem darker = new MenuItem("_Escuro");
        darker.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+E")
        );
        darker.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.darker();
            }
        });
        //negativo
        MenuItem negativo = new MenuItem("_Negativo");
        negativo.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+N")
        );
        negativo.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.negativo();
            }
        });
        //deteccao de borda
        MenuItem detecBorda = new MenuItem("_Deteccão de borda");
        detecBorda.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+D")
        );
        detecBorda.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.detecaoBorda();
            }
        });
        //equalizacao de histograma
        MenuItem eqHistograma = new MenuItem("_Equalização de Histograma");
        eqHistograma.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+H")
        );
        eqHistograma.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.eqHistograma();
            }
        });
        //Gradiente
        MenuItem gradiente = new MenuItem("_Gradiente");
        gradiente.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+G")
        );
        gradiente.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.gradiente();
            }
        });
        //segmentacao
        MenuItem segmentacao = new MenuItem("_Segmentacao");
        segmentacao.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.segmentacao();
            }
        });
        //segmentacao
        MenuItem detectItems = new MenuItem("_Detectar obetos");
        detectItems.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
               tratamentoImagens.shapeDetector();
            }
        });
        filtros.getItems().addAll(preto,brighter,darker,negativo,detecBorda,eqHistograma,gradiente,segmentacao,detectItems);
        
        //shapeDetector()
        
        //Ajuda
        Menu ajuda = new Menu("Aj_uda");
        ajuda.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+U")
        );
        //Sobre
        MenuItem sobre = new MenuItem("_Sobre");
        sobre.setAccelerator(
            KeyCombination.keyCombination("SHORTCUT+S")
        );
        sobre.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                Group rt = new Group();
                Stage info = new Stage();
                info.initStyle(StageStyle.UTILITY);
                info.setTitle("Informações");
                info.setMaxHeight(300);
                info.setMaxWidth(500);
                info.centerOnScreen();
                info.setResizable(false);
                Scene in = new Scene(rt, 500, 300, Color.BLACK);
                info.setScene(in);
                info.show();
                
                //caixa com as informações
                HBox ca = new HBox();
                ca.setMinSize(500, 200);
                ca.setStyle("-fx-background-color:#FFFAFA;");
                Label titulo = new Label("Analisador de imagens para lançamento de foguetes \n Professor-Orientador: Cesar \n Autores: \n Henrique R. Lacerda \n Joseph Oliveira Correa \n Leonardo Fiorentini \n ₢Todos os direitos reservados");
                titulo.setFont(Font.font("Cooper Black", 15));
                titulo.setLayoutX(7000);
                Image inf = new Image(getClass().getResourceAsStream("Informação.jpg"));
                ImageView gu = new ImageView(inf);
                gu.setLayoutX(20);
                ca.getChildren().addAll(gu,titulo);
                
                //caixa com botão ok
                HBox bo = new HBox();
                bo.setMinSize(500, 100);
                bo.setLayoutY(200);
                bo.setStyle("-fx-background-color: #bfc2c7;");
                Button la = new Button("Ok");
                la.setPrefSize(50,40);
                la.setLayoutX(430);
                la.setLayoutY(215);
                la.setAlignment(Pos.CENTER);
                la.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event){
                        info.close();
                    }
                });
                
                //adicionando as caixas ao root
                rt.getChildren().addAll(ca,bo,la);
            }
        });
        ajuda.getItems().add(sobre);
               
        //adicionando os itens no menu
        menu.getMenus().addAll(arquivo,exibir,filtros,ajuda);
        //add menu na janela
        //canvas.getChildren().addAll(menu);
        principal.getChildren().addAll(caixa);
        //faz com que apareça a cena(janela) em um stage(que de fato é a janela)
        stage.setScene(janela);
        stage.show();
    }
    
    public static Imagens obtemClassImagemAtual(){
        Imagens retorno = null;
        int i;
        for(i = 0; i < 6; i++){
            if(TCC.usuarioImgs[i] != null){
                if(TCC.usuarioImgs[i].visivel){
                    System.out.println("Classe visivel: "+i+"; "+TCC.usuarioImgs[i].toString()+";"+TCC.usuarioImgs[i].box.toString());
                    retorno = TCC.usuarioImgs[i];
                    break;
                }
            }
        }
        System.out.println("Devolvendo valor.");
        return retorno;
    }
}