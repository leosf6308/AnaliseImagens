package Tcc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
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
import javafx.scene.layout.StackPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.AnchorPane;

/*@author Henrique, Leonardo and Joseph*/
public class TCC extends Application {

    public static String caminho;
    public static Group principal = new Group();
    public static int cont = 0, lay = 25;
    public static Player vidPlayer = null;
    public static Visualizacao usuarioVisu[] = new Visualizacao[6];
    public static VBox botoesVisu = null;
    public static HBox Conteudo = null;
    public static StackPane pilhaVisualizacao = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Fecha de vez tudo
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });

        //Janela
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        /*if(w < screenSize.width)
            w = screenSize.width;
        if(h < screenSize.height)
            h = screenSize.height;*/
        Scene janela = new Scene(principal, 1600/*largura ou X*/, 1200/*altura ou Y*/, Color.LIGHTGREY);
        stage.setTitle("TCC");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Foguete.png")));
        stage.setMaximized(true);

        /*Layout da tela:
         *Caixa: VBox (itens ficam um abaixo do outro)
         *  Menus
         *  Conteudo: HBox (itens ficam um ao lado do outro)
         *      botoesVisu: VBox
         *          Botão visualização -> Adicionado durante Runtime
         *      pilhaVisualizacao: StackPane (permite que os itens fiquem um por cima do outro - NÂO PENSE BESTEIRA)
         *          Visualização: Pane (genérico, pode virar HBox ou VBox) -> Adicionado durante runtime.
         *              Itens da visualização
	http://docs.oracle.com/javafx/2/layout/builtin_layouts.htm 
		criaVisu
                    boxVisu.addButton
                    ONACTION:
                            SET INDEX i TO view
                            hide all others
                            SET View.Pane 
                    SET INDEX i TO new View
                    ADD new View's Pane TO ContentPane
                    SET ALL other Views TO Hidden

		new View
                    Set content Pane TO HBox
                    Create image pane, add to HBox
                    Create close button, add to HBox
                    Set image
         */
        //Menu
        MenuBar menu = new MenuBar();
        VBox caixa = new VBox();
        caixa.setMaxSize(janela.getWidth(), janela.getHeight());
        caixa.getChildren().add(menu);
        Conteudo = new HBox();
        Conteudo.setMaxSize(janela.getWidth(), janela.getHeight() - caixa.getHeight());
        caixa.getChildren().add(Conteudo);
        botoesVisu = new VBox();
        botoesVisu.setMaxSize(128, janela.getHeight());
        pilhaVisualizacao = new StackPane();
        Conteudo.getChildren().addAll(botoesVisu, pilhaVisualizacao);
        pilhaVisualizacao.setAlignment(Pos.TOP_LEFT);
        
        // http://stackoverflow.com/questions/10773000/how-to-listen-for-resize-events-in-javafx
        // https://blog.idrsolutions.com/2012/11/adding-a-window-resize-listener-to-javafx-scene/
        janela.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int width = newValue.intValue();
            caixa.setMaxSize(width, caixa.getMaxHeight());
            Conteudo.setMinSize(width, Conteudo.getMaxHeight());
            Conteudo.setMaxSize(width, Conteudo.getMaxHeight());
            botoesVisu.setMaxSize(128, botoesVisu.getHeight());
            redimensonarJanela();
        });
        janela.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int height = newValue.intValue();
            caixa.setMaxSize(caixa.getMaxWidth(), caixa.getMaxHeight());
            Conteudo.setMinSize(Conteudo.getMaxWidth(), height - menu.getHeight());
            Conteudo.setMaxSize(Conteudo.getMaxWidth(), height - menu.getHeight());
            botoesVisu.setMaxSize(128, height - menu.getHeight());
            redimensonarJanela();
        });

        //arquivo
        Menu arquivo = new Menu("_Arquivo");
        arquivo.setAccelerator(
                KeyCombination.keyCombination("A")
        );
        MenuItem abrir = new MenuItem("Ab_rir");
        abrir.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+R")
        );
        abrir.setOnAction(event -> menuAbrir());

        //salvar
        MenuItem salvar = new MenuItem("_Salvar");
        salvar.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+S")
        );
        salvar.setOnAction((ActionEvent event) -> {
            try {
                int i = caminho.length() - 1;
                while (i > 0) {
                    if (caminho.charAt(i) == '.') {
                        break;
                    }
                    i--;
                }

                String extensao = caminho.substring(i);
                extensao = extensao.toUpperCase();
                System.out.println("; extensão: " + extensao);

                //Verificar se é vídeo ou imagem.
                if (extensao.equals(".FLV") || extensao.equals(".AVI") || extensao.equals(".MP4")) {
                    Salvar.salvarComo();
                } else {
                    Visualizacao imgAtual = TCC.obtemClassImagemAtual();
                    if (imgAtual == null) {
                        return;
                    }
                    Salvar.salva(imgAtual.getImagem(), caminho);
                }
            } catch (Exception e) {
                System.out.println("Exception (TCC.java@start()@salvar.setOnAction): " + e.getMessage());
            }
        });

        //salvarcomo
        MenuItem como = new MenuItem("Salvar C_omo");
        como.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+O")
        );
        como.setOnAction((ActionEvent event) -> {
            Salvar.salvarComo();
        });
        arquivo.getItems().addAll(abrir, salvar, como);

        //Exibir
        Menu exibir = new Menu("_Exibir");
        /*filtros.setAccelerator(
            KeyCombination.keyCombination("E")
        );*/
        //preto e branco
        MenuItem histograma = new MenuItem("_Histograma");
        histograma.setOnAction((ActionEvent event) -> {
            tratamentoImagens.exibeHistograma();
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
        preto.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.tonsdeCinza(viewAtual.getImagem()));
        });
        //brighter
        MenuItem brighter = new MenuItem("_Brighter");
        brighter.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+B")
        );
        brighter.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.brighter(viewAtual.getImagem()));
        });
        //darker
        MenuItem darker = new MenuItem("_Escuro");
        darker.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+E")
        );
        darker.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.darker(viewAtual.getImagem()));
        });
        //negativo
        MenuItem negativo = new MenuItem("_Negativo");
        negativo.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+N")
        );
        negativo.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.negativo(viewAtual.getImagem()));
        });
        //deteccao de borda
        MenuItem detecBorda = new MenuItem("_Deteccão de borda");
        detecBorda.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+D")
        );
        detecBorda.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.detecaoBorda(viewAtual.getImagem()));
        });
        //equalizacao de histograma
        MenuItem eqHistograma = new MenuItem("_Equalização de Histograma");
        eqHistograma.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+H")
        );
        eqHistograma.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.eqHistograma(viewAtual.getImagem()));
        });
        //Gradiente
        MenuItem gradiente = new MenuItem("_Gradiente");
        gradiente.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+G")
        );
        gradiente.setOnAction((ActionEvent event) -> {
            Visualizacao viewAtual = TCC.visuAtual();
            ImageView img = new ImageView();
            img.setImage(tratamentoImagens.gradiente(viewAtual.getImagem()));
        });
        //segmentacao
        MenuItem segmentacao = new MenuItem("_Segmentacao");
        segmentacao.setOnAction((ActionEvent event) -> {
            tratamentoImagens.segmentacao();
        });
        //segmentacao
        MenuItem detectItems = new MenuItem("_Detectar obetos");
        detectItems.setOnAction((ActionEvent event) -> {
            tratamentoImagens.shapeDetector();
        });
        filtros.getItems().addAll(preto, brighter, darker, negativo, detecBorda, eqHistograma, gradiente, segmentacao, detectItems);

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
        sobre.setOnAction((ActionEvent event) -> {
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
            ca.getChildren().addAll(gu, titulo);

            //caixa com botão ok
            HBox bo = new HBox();
            bo.setMinSize(500, 100);
            bo.setLayoutY(200);
            bo.setStyle("-fx-background-color: #bfc2c7;");
            Button la = new Button("Ok");
            la.setPrefSize(50, 40);
            la.setLayoutX(430);
            la.setLayoutY(215);
            la.setAlignment(Pos.CENTER);
            la.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    info.close();
                }
            });

            //adicionando as caixas ao root
            rt.getChildren().addAll(ca, bo, la);
        });
        ajuda.getItems().add(sobre);

        //adicionando os itens no menu
        menu.getMenus().addAll(arquivo, exibir, filtros, ajuda);
        //add menu na janela
        //canvas.getChildren().addAll(menu);
        principal.getChildren().addAll(caixa);
        //faz com que apareça a cena(janela) em um stage(que de fato é a janela)
        stage.setScene(janela);
        stage.show();
    }

    public static void redimensonarJanela() {
        int i;
        for (i = 0; i < usuarioVisu.length; i++)
            if (usuarioVisu[i] != null)
                usuarioVisu[i].redimensiona((int) (Conteudo.getMaxWidth() - botoesVisu.getWidth()), (int) Conteudo.getMaxHeight());
        System.out.println("Tcc.TCC.redimensonarJanela(): W:"+(Conteudo.getWidth() - botoesVisu.getWidth()) + "; H:" +  (int) Conteudo.getHeight());
    }

    public static void mostraVisualizacao(int indice) {
        //Esconde todas as outras.
        int i;
        for (i = 0; i < usuarioVisu.length; i++) {
            if (usuarioVisu[i] != null) {
                if (i != indice) {
                    usuarioVisu[i].escondeImagem();
                } else {
                    usuarioVisu[i].mostraImagem();
                }
            }
        }
    }

    public static void fechaVisualizacao(int indice, Button btnTroca) {
        //Faz o cleanup
        int i;
        for (i = 0; i < usuarioVisu.length; i++) {
            if (usuarioVisu[i] != null) {
                if (i != indice) {
                    usuarioVisu[i].mostraImagem();
                    break;
                }
            }
        }
        pilhaVisualizacao.getChildren().remove(usuarioVisu[indice].getPane());
        usuarioVisu[indice].fechaImagem();
        usuarioVisu[indice] = null;
        botoesVisu.getChildren().remove(btnTroca);
        cont--;
    }

    public static void novoVisualizacao(Image imagem, Boolean eVideo) {
        int i;
        Button st;
        Image icone;
        for (i = 0; i < usuarioVisu.length; i++) {
            if (usuarioVisu[i] == null) {
                if (eVideo) {
                    icone = new Image(TCC.class.getResourceAsStream("Video.png"));
                } else {
                    icone = new Image(TCC.class.getResourceAsStream("Imagem.png"));
                }

                //Cria botão de troca
                st = new Button();
                st.setGraphic(new ImageView(icone));
                st.setMaxSize(64, 64);
                final int id = i;
                st.setOnAction(event1 -> mostraVisualizacao(id));
                botoesVisu.getChildren().add(st);

                //Cria a nova Visualizaçao
                usuarioVisu[i] = new Visualizacao(imagem, (int) (Conteudo.getWidth() - botoesVisu.getWidth()), (int) Conteudo.getHeight());
                pilhaVisualizacao.getChildren().add(usuarioVisu[i].getPane());

                final Button btnTroca = st;
                usuarioVisu[i].defineFechar(event2 -> fechaVisualizacao(id, btnTroca));
                mostraVisualizacao(i);
                break;
            }
        }

    }

    /*
    public static void novoVisualizacao(Image imagem, Boolean eVideo) {
        int i;
        boolean encontrado = false;
        for (i = 0; i < usuarioVisu.length; i++) {
            if (usuarioVisu[i] == null) {
                final int ID = i;
                Image icone;
                if (encontrado) {
                    continue;
                }
                //Criar visualização
                if (eVideo) {
                    icone = new Image(TCC.class.getResourceAsStream("Video.png"));
                } else {
                    icone = new Image(TCC.class.getResourceAsStream("Imagem.png"));
                }
                usuarioVisu[i] = new Visualizacao(imagem, icone);
                double posY = (double) (i * 110 + 25);
                usuarioVisu[i].definePosicaoBotao((int) posY);
                usuarioVisu[i].defineBotaoFechar(event -> {

                    for (int n = 0; n < usuarioVisu.length; n++) {
                        if (usuarioVisu[ID] == null && n != ID) {
                            usuarioVisu[ID].ativaImagem();
                            break;
                        }
                    }
                    usuarioVisu[ID].fechaImagem();
                    principal.getChildren().removeAll(usuarioVisu[ID].getAtalhos(), usuarioVisu[ID].getBox());
                    usuarioVisu[ID] = null;
                });
                usuarioVisu[i].defineBotaoAtivar(event -> {
                    for (int n = 0; n < usuarioVisu.length; n++) {
                        if (n == ID) {
                            usuarioVisu[ID].ativaImagem();
                        } else {
                            usuarioVisu[ID].escondeImagem();
                        }
                    }
                    System.out.println("Showing " + ID);
                });
                principal.getChildren().addAll(usuarioVisu[i].getAtalhos(), usuarioVisu[i].getBox());
                encontrado = true;
            } else {
                usuarioVisu[i].escondeImagem();
            }
        }
    }*/

    public static void menuAbrir() {
        caminho = Selecionar.selecionar();
        if (caminho == null) {
            return;
        }
        //try {
        int i = caminho.length() - 1;
        while (i > 0) {
            if (caminho.charAt(i) == '.') {
                break;
            }
            i--;
        }
        String extensao = caminho.substring(i);
        extensao = extensao.toUpperCase();
        System.out.println("Arquivo: '" + caminho + "'; Extensão: '" + extensao + "' (TCC.java@start()@abrir.setOnAction)");

        //Verificar se é vídeo ou imagem.
        if (extensao.equals(".FLV") || extensao.equals(".AVI") || extensao.equals(".MP4")) {
            vidPlayer = new Player();
        } else if (cont >= 6) {
            JOptionPane.showMessageDialog(null, "Só podem existir 6 imagens abertas ao mesmo tempo.", "Aviso!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Image imagem = new Image((new File(TCC.caminho)).toURI().toString());
            novoVisualizacao(imagem, false);
            cont++;
        }
        /*}
        catch (Exception e){
            System.out.println("Exception (TCC.java@start()@abrir.setOnAction): "+e.getMessage());
        }*/
    }

    public static Visualizacao visuAtual() {
        int i;
        for (i = 0; i < 6; i++) {
            if (TCC.usuarioVisu[i] != null) {
                if (TCC.usuarioVisu[i].estaSelecionado()) {
                    return TCC.usuarioVisu[i];
                }
            }
        }
        return null;
    }

    public static Visualizacao obtemClassImagemAtual() {
        Visualizacao retorno = null;
        int i;
        for (i = 0; i < 6; i++) {
            if (TCC.usuarioVisu[i] != null) {
                if (TCC.usuarioVisu[i].estaSelecionado()) {
                    System.out.println("Classe visivel: " + i + "; " + TCC.usuarioVisu[i].toString() + ";");
                    retorno = TCC.usuarioVisu[i];
                    break;
                }
            }
        }
        System.out.println("Devolvendo valor.");
        return retorno;
    }
}
