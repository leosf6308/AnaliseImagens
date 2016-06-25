package Tcc;

import static Tcc.TCC.novoVisualizacao;
import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.swing.JOptionPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.awt.image.BufferedImage;
import java.io.File;

/*@author Henrique, Leonardo and Joseph*/
public class Player extends Application {

    //Carregando todas as variáveis usadas pelo programa
    public static Button stop, pause, sim, nao, but, full, vol;
    private Group root;
    public MediaPlayer player;
    public Stage pla;
    public MediaView view;
    public Media media;
    public HBox b;
    public int w, h;
    public int posun = 100, pocatek = 0; // milisecs
    public Duration cas = new Duration(0);
    public ImageView im;
    //'verdade' diz se está tocando ou não (quando > 1 está pausado).
    private int verdade = 1, desk = 0, tela = 0, go = 1;
    private Boolean estaCarregandoQuadros;
    private Scene scene;
    private StackPane rt;
    private Group root2;
    private String caminhoVideo;
    private HBox miniaturas;
    private videoQuadros vidQuadros;
    private Slider slider;
    public videoFrame[] vidFrames;
    
    public Player() {

        w = 900;
        h = 550;
        root = new Group();
        pla = new Stage();
        //pla.initStyle(StageStyle.UTILITY);
        pla.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        pla.setTitle("Movie Player");
        pla.show();
        pla.setMaxHeight((double) h);
        pla.setMaxWidth((double) w);
        pla.centerOnScreen();
        pla.getIcons().add(new Image(getClass().getResourceAsStream("Film.jpg")));
        scene = new Scene(root, w, h, Color.BLACK);
        pla.setScene(scene);
        miniaturas = null;
        estaCarregandoQuadros = false;

        //Set do caminho da media para padrão de internet, caso o contrário o file não abre e carregando a media
        vidQuadros = videoQuadros.criaVidQuadros(TCC.caminho);
        caminhoVideo = (new File(TCC.caminho)).toURI().toString();
        System.out.println("Vídeo: '" + caminhoVideo + "' (Player.java@Player())");
        media = new Media(caminhoVideo);
        
        //Carregando as imagens dos botões
        Image st = new Image(getClass().getResourceAsStream("Stop.png"));
        Image pa = new Image(getClass().getResourceAsStream("Pause.png"));
        Image pl = new Image(getClass().getResourceAsStream("Play.png"));
        Image si = new Image(getClass().getResourceAsStream("Quadro.png"));
        Image na = new Image(getClass().getResourceAsStream("Lixo.png"));
        Image ko = new Image(getClass().getResourceAsStream("FullScreen.jpg"));

        //Configuração do botão stop
        stop = new Button();
        stop.setGraphic(new ImageView(st));
        stop.setMaxSize(30, 30);
        stop.setAlignment(Pos.BASELINE_CENTER);

        //Configuração do botão pause
        pause = new Button();
        pause.setGraphic(new ImageView(pa));
        pause.setMaxSize(30, 30);
        pause.setAlignment(Pos.BASELINE_RIGHT);

        //Configuração do botão sim ou imagem
        sim = new Button();
        sim.setGraphic(new ImageView(si));
        sim.setMaxSize(30, 30);
        sim.setAlignment(Pos.BASELINE_RIGHT);

        //Configuração do botão não ou lixo
        nao = new Button();
        nao.setGraphic(new ImageView(na));
        nao.setMaxSize(30, 30);
        nao.setAlignment(Pos.BASELINE_RIGHT);

        //Configuração do botão Fullscreen
        full = new Button();
        full.setGraphic(new ImageView(ko));
        full.setMaxSize(30, 30);
        full.setAlignment(Pos.BASELINE_RIGHT);

        //Configuração do botão volume
        vol = new Button();
        Image vo = new Image(getClass().getResourceAsStream("Volume.png"));
        vol.setGraphic(new ImageView(vo));
        vol.setMaxSize(30, 30);
        vol.setAlignment(Pos.BASELINE_RIGHT);

        //Carregando a media no player e criando uma MediaView para poder ser visualizada na tela
        player = new MediaPlayer(media);
        view = new MediaView(player);
        view.setFitWidth((double) w);
        view.setFitHeight((double) h - 3);
        view.setPreserveRatio(false);

        //Efeito de aparecer e aparecer quando o mouse for posicionado no lugar certo
        Timeline slideIn = new Timeline();
        Timeline slideOut = new Timeline();
        root.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                slideOut.play();
                if (miniaturas != null) {
                    miniaturas.setVisible(false);
                }
            }
        });
        root.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                slideIn.play();
                if (miniaturas != null) {
                    miniaturas.setVisible(true);
                }
            }
        });

        //Criando a barra de botões e de progressão do video
        b = new HBox();
        b.setStyle("-fx-background-color: #bfc2c7;");

        //Slider de Tempo de execução
        slider = new Slider();
        HBox.setHgrow(slider, Priority.ALWAYS);
        slider.setMinWidth(50);
        slider.setMaxWidth(Double.MAX_VALUE);

        //Slider de volume
        Slider volumeSlider = new Slider();
        volumeSlider.setPrefHeight(60);
        volumeSlider.setMaxHeight(Region.USE_PREF_SIZE);
        volumeSlider.setMinHeight(50);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setValue((int) Math.round(player.getVolume() * 100));
        volumeSlider.valueProperty().addListener((Observable ov) -> {
            if (volumeSlider.isValueChanging()) {
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        //Set das configurações do HBox e adicionando os botões e barra de progressão do video 
        b.getChildren().add(slider);
        b.setMinSize(w, h);
        b.setMinWidth(w);
        b.getChildren().addAll(pause, stop, full, vol);
        b.setAlignment(Pos.BOTTOM_CENTER);
        b.setPadding(new Insets(10, 30, 30, 30));

        //Ação do botão Fullscreen
        full.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event1) {
                //Função if que pega o tamanho máximo da tela de qualquer computador
                if (tela == 0) {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    if (w < screenSize.width) {
                        w = screenSize.width;
                    }
                    if (h < screenSize.height) {
                        h = screenSize.height;
                    }

                    DoubleProperty width = view.fitWidthProperty();
                    DoubleProperty height = view.fitHeightProperty();

                    width.bind(Bindings.selectDouble(view.sceneProperty(), "width"));
                    height.bind(Bindings.selectDouble(view.sceneProperty(), "height"));

                    view.setPreserveRatio(false);

                    rt = new StackPane();
                    rt.getChildren().addAll(view, root);
                    rt.setAlignment(Pos.BOTTOM_CENTER);
                    b.setMinWidth(w);
                    Scene second = new Scene(rt, w, h, Color.BLACK);
                    pla.setScene(second);
                    pla.setMaxHeight((double) h);
                    pla.setMaxWidth((double) w);
                    pla.setFullScreen(true);

                    tela += 1;
                } else {
                    pla.setFullScreen(false);
                    tela--;
                    w = 900;
                    h = 550;

                    root2 = new Group();
                    scene = new Scene(root2, w, h, Color.BLACK);
                    root2.getChildren().add(view);
                    b.setMinWidth(w);
                    root.setLayoutY(h - 552);
                    root.setLayoutX(w - 910);
                    root2.getChildren().add(root);
                    pla.setScene(scene);
                    pla.setMaxHeight((double) h);
                    pla.setMaxWidth((double) w);
                    pla.centerOnScreen();
                }

            }
        });

        //Ação do botão Pausa
        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event1) {
                pause.setGraphic(new ImageView(pl));
                player.pause();
                if (verdade > 1) {
                    player.play();
                    pause.setGraphic(new ImageView(pa));
                    verdade = 1;
                } else {
                    b.getChildren().add(sim);
                    b.getChildren().add(nao);
                    verdade++;
                }
            }
        });

        //Ação do Botão Parar
        stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event1) {
                player.stop();
                pause.setGraphic(new ImageView(pl));
                verdade = 2;
            }
        });

        //Ação do botão Lixo
        nao.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event1) {
                b.getChildren().remove(sim);
                b.getChildren().remove(nao);
                pause.setGraphic(new ImageView(pa));
                player.play();
                verdade = 0;
            }
        });

        //Ação do botão de imagens
        sim.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event1) {
                //Verificação de quantas imagens dá para colocar mais de 6 imagens
                if (TCC.cont == 6) {
                    new Aviso();
                } else {
                    //Propriedades do video
                    b.getChildren().remove(sim);
                    b.getChildren().remove(nao);
                    verdade = 2;
                }
                novoVisualizacao(view.snapshot(new SnapshotParameters(), new WritableImage(media.getWidth(), media.getHeight())), false);
            }
        });

        //Ação do botão volume
        vol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event2) {
                if (go == 1) {
                    b.getChildren().add(volumeSlider);
                    go++;
                } else {
                    b.getChildren().remove(volumeSlider);
                    go--;
                }
            }
        });

        //Adicionando o video em execução e a barra de botões ao root
        root.getChildren().add(view);
        root.getChildren().add(b);

        //Fecha de vez o player
        pla.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                player.stop();
                TCC.vidPlayer = null;
                vidQuadros.destroiVidQuadros();
            }
        });

        Player estePlayer = this;
        //Propriedades do player quando ele dá play
        player.play();
        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                //testar se essa função pega o tempo player.getCurrentTime();
                //player.getBufferProgressTime();
                w = media.getWidth();
                h = media.getHeight();
                pla.setWidth(w);
                pla.setHeight(h);
                
                //Propriedade que faz o video ir para o início quando ele chega ao fim, porém não dá play
                player.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        player.seek(Duration.ZERO);
                        player.stop();
                        pause.setGraphic(new ImageView(pl));
                        verdade = 2;
                    }
                });

                b.setMinSize(w, 100);
                b.setTranslateY(h - 110);

                slider.setMin(0.0);
                slider.setValue(0.0);
                slider.setMax(player.getTotalDuration().toSeconds());

                slideOut.getKeyFrames().addAll(
                        new KeyFrame(new Duration(0),
                                new KeyValue(b.translateYProperty(), h - 146),
                                new KeyValue(b.opacityProperty(), 0.7)
                        ),
                        new KeyFrame(new Duration(300),
                                new KeyValue(b.translateYProperty(), h - 147),
                                new KeyValue(b.opacityProperty(), 0.0)
                        )
                );

                slideIn.getKeyFrames().addAll(
                        new KeyFrame(new Duration(0),
                                new KeyValue(b.translateYProperty(), h - 147),
                                new KeyValue(b.opacityProperty(), 0.0)
                        ),
                        new KeyFrame(new Duration(300),
                                new KeyValue(b.translateYProperty(), h - 147),
                                new KeyValue(b.opacityProperty(), 0.7)
                        )
                );

                player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration current) {
                        slider.setValue(current.toSeconds());
                    }
                });

                player.setOnError(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Error: " + player.errorProperty().toString());
                    }
                });

                slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        player.pause();
                        player.seek(Duration.seconds(slider.getValue()));
                        if (verdade > 1) {
                            player.play();
                        }
                    }
                });

                slider.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        double percent, totalSeconds;
                        int second;
                        if (estaCarregandoQuadros) {
                            return;
                        }
                        System.out.println("Scene: " + mouseEvent.getSceneX() + "x" + mouseEvent.getSceneY() + "\nCoord: " + slider.getLayoutX() + "x" + slider.getLayoutY() + "\nSize:" + slider.getWidth() + "x" + slider.getHeight());
                        percent = mouseEvent.getSceneX() - slider.getLayoutX();
                        percent = percent / slider.getWidth();
                        totalSeconds = player.getTotalDuration().toSeconds();
                        second = (int) (totalSeconds * percent);
                        System.out.println("Second: " + second + "\nTotalSeconds: " + totalSeconds);
                        if (miniaturas == null) {
                            miniaturas = new HBox();
                            miniaturas.setStyle("-fx-background-color: rgba(191, 194, 199, 0.8);");// -fx-background-radius: 10;");
                            miniaturas.setMinSize(pla.getMaxWidth(), 110);
                            miniaturas.setAlignment(Pos.BOTTOM_CENTER);
                            miniaturas.setPadding(new Insets(30, 30, 30, 30));
                            miniaturas.managedProperty().bind(miniaturas.visibleProperty());
                            //miniaturas.setLayoutY(root.getLayoutY()-miniaturas.getHeight());
                            //miniaturas.setLayoutY(-20);
                        } else {
                            miniaturas.getChildren().clear();
                        }

                        estaCarregandoQuadros = true;
                        Text t = new Text();
                        t.setText("Carregando miniaturas...");
                        t.setFill(Color.BLACK);
                        t.setFont(Font.font(null, FontWeight.BOLD, 20));
                        t.setX(25);
                        t.setY(65);
                        miniaturas.getChildren().add(t);
                        miniaturas.setLayoutY(pla.getHeight() - (170 + b.getHeight()));
                        if (root.getChildren().contains(miniaturas) == false) {
                            root.getChildren().add(miniaturas);
                        }

                        if (vidFrames != null) {
                            for (int i = 0; i < vidFrames.length; i++) {
                                if (vidFrames[i] != null) {
                                    vidFrames[i].clear();
                                    vidFrames[i] = null;
                                }
                            }
                            vidFrames = null;
                        }
                        vidQuadros.lerPreviews(estePlayer, second, 200, 100);
                    }
                });

            }
        });
    }

    public void mostraMiniaturas() {
        if (miniaturas == null) {
            miniaturas = new HBox();
            miniaturas.setStyle("-fx-background-color: rgba(191, 194, 199, 0.7);");// -fx-background-radius: 10;");
            miniaturas.setMinSize(pla.getMaxWidth(), 110);
            miniaturas.setAlignment(Pos.BOTTOM_CENTER);
            miniaturas.setPadding(new Insets(30, 30, 30, 30));
            miniaturas.managedProperty().bind(miniaturas.visibleProperty());
            //miniaturas.setLayoutY(root.getLayoutY()-miniaturas.getHeight());
            //miniaturas.setLayoutY(-20);
        } else {
            miniaturas.getChildren().clear();
        }

        miniaturas.setLayoutY(pla.getHeight() - (vidFrames[0].getPreview().getHeight() + 120 + b.getHeight()));
        System.out.println("Player: " + pla.getWidth() + "x" + pla.getHeight() + "\nb: " + b.getWidth() + "x" + b.getHeight() + "\nSize: " + pla.getMaxWidth() + "(" + pla.getWidth() + ")x" + (vidFrames[0].getPreview().getHeight() + 60));
        for (int i = 0; i < vidFrames.length; i++) {
            if (vidFrames[i] == null) {
                System.out.println("Whaaaat? Frame #" + i + " is null????");
                continue;
            }
            Button btnPreview = new Button();
            BufferedImage buffImg = vidFrames[i].getPreview();
            final BufferedImage image = vidFrames[i].getImage();
            btnPreview.setGraphic(new ImageView(SwingFXUtils.toFXImage(buffImg, null)));
            btnPreview.setMaxSize(buffImg.getWidth() + 4, buffImg.getHeight() + 4);
            btnPreview.setAlignment(Pos.BASELINE_CENTER);
            btnPreview.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Image imagem = SwingFXUtils.toFXImage(image, null);
                    novoVisualizacao(imagem, false);
                }
            });
            miniaturas.getChildren().add(btnPreview);
        }

        if (root.getChildren().contains(miniaturas) == false) {
            root.getChildren().add(miniaturas);
        }
        estaCarregandoQuadros = false;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
