/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Admin
 */
public class Segmentacao {
    /**
     * Exibe as informações da segmentação, criando uma visualização para o mesmo.
     * Método modificável de segmentação: DKHFastScanning.
     * A ideia é que caso seja necessário modificar, somente o segmentador seja modificado, mantendo 
     *  as funções e classes auxiliares.
     *  Métodos auxiliares:
     *     adicionaPonto: adiciona o ponto ao cluster especificado, alterando valores conforme.
     *     criaCluster: adiciona um novo cluster à lista de clusters, iniciando seus valores.
     *     distanciaCores: retorna a distância entre duas cores no espaço tridimensional (ou RGB).
     *  Entrada: javafx.scene.image.Image imagem, int limiar, double minCt
     *  Classes auxiliares:
     *     ThreadCallback: interface que permite o envio de mensagens à um objeto específico.
     *        No caso, ao Segmentacao, que implementa os métodos redirecionando o texto para cxTexto.
     *     ContagemPixels: estrutura para lista de regiões que envolvem a atual, usando seus dados como
     *                     critério de mesclagem com a região mais "próxima".
     *     SegmentacaoImg: guarda informações básicas da segmentação, como bitmap de clusters, altura e
     *                     largura da bitmap.
     *     Cluster: contém informações sobre uma região específica, como caixa de limítrofe, número de
     *              pixels e tom médio. É uma lista duplamente ligada.
     *     SegmentacaoThread: executa a segmentação em si, criando um novo thread para a mesma.
     *  Fluxo de execução:
     *     Cria view pelo construtor Segmentacao
     *        Segmentacao(Image imagem, Visualizacao viewSegmentacao)
     *     Cria thread de agrupamento.
     *        thread = new SegmentacaoThread(imagem, limiar, minimo);
     *     Inicia thread de agrupamento.
     *        thread.start();
     *     Java inicia novo thread:
     *       @Override public void run()
     *    Função chama a responsável pela segmentação
     *       DKHFastScanning()
     *    Segmentador executa operações sobre os dados
     *    Segmentador envia mensagens (se necessário)
     *       enviaMensagem(String mensagem)
     *    Quando terminar, segmentador envia MSG_QUIT
     *       enviaMensagem(ThreadCallback.MSG_QUIT);
     * 
     * https://docs.oracle.com/javafx/2/api/javafx/scene/paint/Color.html
     * https://docs.oracle.com/javase/7/docs/api/java/awt/Color.html
     */
    
    private Text cxTexto;
    private ComboBox lista;
    private Visualizacao visuSegm;
    private SegmentacaoImg dadosSegmentacao;
    private Cluster listaRegioes, atual;
    private SegmentacaoThread thread;
    
    public interface ThreadCallback{
        public static final int MSG_INITIALIZING = 1;
        public static final int MSG_CLUSTERING = 2;
        public static final int MSG_COMPACTING = 3;
        public static final int MSG_DONE = 4;
        public static final int MSG_QUIT = 65535;
        public void sendMessage(int id);
        public void sendMessage(String message);
        public void setDadosSegmentacao(SegmentacaoImg segmDados, Cluster listaClusters);
    }
    
    private class ContagemPixels {

        public Cluster cluster;
        public int count;
        public double distance;

        public ContagemPixels(Cluster c1, double dist) {
            cluster = c1;
            count = 1;
            distance = dist;
        }
    }

    private class SegmentacaoImg {
        public Image fonte;
        public int altura;
        public int largura;
        public Cluster[][] bitmap;
        public int contagemClusters;
        SegmentacaoImg(Image img) {
            fonte = img;
            largura = (int) fonte.getWidth();
            altura = (int) fonte.getHeight();
            bitmap = new Cluster[altura][largura];
        }
        public Cluster getPixelCluster(int x, int y){
            if(x < 0 || x >= largura || y < 0 || y >= altura)
                throw new RuntimeException("Pixel "+x+"x"+y+" fora dos limites!");
            return bitmap[y][x];
        }
    }

    private class Cluster {

        public Cluster proximo;
        public Cluster anterior;
        public int contagem;
        public int esquerda;
        public int superior;
        public int direita;
        public int inferior;
        public Color tom;

        Cluster(int x, int y, Color cor, Cluster antes) {
            esquerda = x;
            direita = x + 1;
            superior = y;
            inferior = y + 1;
            tom = cor;
            anterior = antes;
            contagem = 1;
        }
    }

    private class SegmentacaoThread extends Thread {

        public Cluster lista;
        //public Cluster[][] bitmap;
        private SegmentacaoImg imgSegmentacao;
        private int limiar;
        private double minimo;
        private Cluster listaClusters;
        private Cluster ultCluster;
        private ThreadCallback callbackMensagens;

        public SegmentacaoThread(Image imagem, int iLimiar, double dMinimo) {
            imgSegmentacao = new SegmentacaoImg(imagem);
            limiar = iLimiar;
            minimo = dMinimo;
        }

        private double distanciaCores(Color c1, Color c2) {
            double dr = c1.getRed()*255.0 - c2.getRed()*255.0;
            double dg = c1.getGreen()*255.0 - c2.getGreen()*255.0;
            double db = c1.getBlue()*255.0 - c2.getBlue()*255.0;
            double dist = dr * dr + dg * dg + db * db;
            return Math.sqrt(dist);
        }

        private Cluster criaCluster(int x, int y, Color cor, Cluster antes) {
            Cluster novo = new Cluster(x, y, cor, ultCluster);
            imgSegmentacao.bitmap[y][x] = novo;
            return novo;
        }

        private void adicionaPonto(Cluster cluster, int x, int y, Color cor) {
            double newR = (cluster.tom.getRed() * cluster.contagem + cor.getRed());
            double newG = (cluster.tom.getGreen() * cluster.contagem + cor.getGreen());
            double newB = (cluster.tom.getBlue() * cluster.contagem + cor.getBlue());
            cluster.contagem++;
            newR /= cluster.contagem;
            newG /= cluster.contagem;
            newB /= cluster.contagem;
            cluster.tom = Color.color(newR, newG, newB);
            if (x < cluster.esquerda) {
                cluster.esquerda = x;
            }
            if (x >= cluster.direita) {
                cluster.direita = x + 1;
            }
            if (y < cluster.superior) {
                cluster.superior = y;
            }
            if (y >= cluster.inferior) {
                cluster.inferior = y + 1;
            }
            imgSegmentacao.bitmap[y][x] = cluster;
        }

        private void mesclaCluster(Cluster c1, Cluster c2) {
            int i, j;
            Cluster anterior;
            if (c1 == c2) {
                throw new RuntimeException("A self merge was detected.");
            }

            double newR = (c1.tom.getRed() * c1.contagem + c2.tom.getRed() * c2.contagem);
            double newG = (c1.tom.getGreen() * c1.contagem + c2.tom.getGreen() * c2.contagem);
            double newB = (c1.tom.getBlue() * c1.contagem + c2.tom.getBlue() * c2.contagem);

            if (c1.esquerda > c2.esquerda)
                c1.esquerda = c2.esquerda;
            if (c1.direita < c2.direita)
                c1.direita = c2.direita;
            if (c1.superior > c2.superior)
                c1.superior = c2.superior;
            if (c1.inferior < c2.inferior)
                c1.inferior = c2.inferior;

            c1.contagem += c2.contagem;
            newR /= c1.contagem;
            newG /= c1.contagem;
            newB /= c1.contagem;
            c1.tom = Color.color(newR, newG, newB);
            for (i = 0; i < imgSegmentacao.altura; i++)
                for (j = 0; j < imgSegmentacao.largura; j++)
                    if (imgSegmentacao.bitmap[i][j] == c2)
                        imgSegmentacao.bitmap[i][j] = c1;
            if(c2 == listaClusters){
                throw new RuntimeException("Tentativa de exclusão do cluster inicial.");
            }
            
            anterior = c2.anterior;
            anterior.proximo = c2.proximo;
            if (anterior.proximo != null) {
                anterior.proximo.anterior = anterior;
            }
        }

        private void mesclaProximo(Cluster cluster) {
            /*Iterar pelo retângulo que envolve o cluster.
             *Nessa iteração, contar quantos pixels tem cores mais próximas.
             *Pesar pelas menores distâncias.
             *
             */
            boolean found;
            int i, x, y, n;
            double maxDist, maxWeight;
            ArrayList<ContagemPixels> pixCnt = new ArrayList();
            Cluster Ci;
            maxDist = 0;
            x = (cluster.esquerda>0?cluster.esquerda-1:0);
            y = (cluster.superior>0?cluster.superior-1:0);
            while (y < (cluster.inferior<imgSegmentacao.altura?cluster.inferior+1:imgSegmentacao.altura)) {
                while (x < (cluster.direita<imgSegmentacao.largura?cluster.direita+1:imgSegmentacao.largura)) {
                    if (imgSegmentacao.bitmap[y][x] != cluster) {
                        found = false;
                        for (n = 0; n < pixCnt.size(); n++) {
                            if (pixCnt.get(n).cluster == imgSegmentacao.bitmap[y][x]) {
                                pixCnt.get(n).count++;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            double distance = distanciaCores(cluster.tom, imgSegmentacao.bitmap[y][x].tom);
                            pixCnt.add(new ContagemPixels(imgSegmentacao.bitmap[y][x], distance));
                            if (distance > maxDist) {
                                maxDist = distance;
                            }
                        }
                    }
                    x++;
                }
                y++;
            }
            if(pixCnt.isEmpty()){
                throw new RuntimeException("Não foram encontrados vizinhos na região : " + (cluster.esquerda>0?cluster.esquerda-1:0) + " to " +
                        (cluster.direita+1<imgSegmentacao.largura?cluster.direita+1:imgSegmentacao.largura) + "; " + 
                        (cluster.superior>0?cluster.superior-1:0) + " to " + (cluster.inferior+1<imgSegmentacao.altura?
                        cluster.inferior+1:imgSegmentacao.altura));
            }
            i = -1;
            maxWeight = 0.0;
            maxDist--;
            for (n = 0; n < pixCnt.size(); n++) {
                double thisWeight = pixCnt.get(n).count * (maxDist - pixCnt.get(n).distance);
                if (thisWeight > maxWeight || pixCnt.get(n).distance - maxDist < 1.001) {
                    i = n;
                    maxWeight = thisWeight;
                }
            }
            if(i != -1){
                System.out.println("Mesclado " + cluster + " com  " + pixCnt.get(i).cluster );
                mesclaCluster(pixCnt.get(i).cluster,cluster);
            }else{
                String strException = "Não foi possível mesclar " + cluster + " com nenhum um dos seus vizinhos. Local: "
                        + cluster.esquerda + "x" + cluster.superior + ". Size: " + (cluster.direita-cluster.esquerda) + "x" +
                        (cluster.inferior-cluster.superior) + ". Clusters: " + pixCnt.size() + ". maxDist:" + maxDist +
                        ". maxWeight:" + maxWeight + ". BoundingBox: " + (cluster.esquerda>0?cluster.esquerda-1:0) + " to " +
                        (cluster.direita+1<imgSegmentacao.largura?cluster.direita+1:imgSegmentacao.largura) + "; " + 
                        (cluster.superior>0?cluster.superior-1:0) + " to " + (cluster.inferior+1<imgSegmentacao.altura?
                        cluster.inferior+1:imgSegmentacao.altura);
                throw new RuntimeException(strException);
            }
            pixCnt.clear();
        }
        
        public void setCallback(ThreadCallback callback){
            callbackMensagens = callback;
        }
        
        private void enviaMensagem(int id){
            String mensagem;
            switch(id){
                case ThreadCallback.MSG_INITIALIZING:
                    System.out.println("Inicializando segmentação...");
                    break;
                case ThreadCallback.MSG_CLUSTERING:
                    System.out.println("Agrupando pixels...");
                    break;
                case ThreadCallback.MSG_COMPACTING:
                    System.out.println("Removendo clusters pequenos...");
                    break;
                case ThreadCallback.MSG_DONE:
                    System.out.println("Concluído.");
                    break;
                case ThreadCallback.MSG_QUIT:
                    System.out.println("Finalizado. "+imgSegmentacao.contagemClusters+" regiões na imagem.");
                    break;
            }
            if(callbackMensagens != null)
                callbackMensagens.sendMessage(id);
        }
        
        private void enviaDadosSegmentacao(){
            if(callbackMensagens != null)
                callbackMensagens.setDadosSegmentacao(imgSegmentacao, listaClusters);
        }
        
        private void enviaMensagem(String mensagem){
            System.out.println(mensagem);
            if(callbackMensagens != null)
                callbackMensagens.sendMessage(mensagem);
        }
        
        public void DKHFastScanning() {
            int i, j;
            double dist;
            Cluster Ci, Cu, Cl;
            PixelReader pixReader = imgSegmentacao.fonte.getPixelReader();
            //Notificar o início da operação
            //Criar uma função "exibeMensagem", ou algo do tipo.
            //Todas as mensagens serão roteadas para esta mensagem
            enviaMensagem("Iniciando processo de segmentação...");
            //enviaMensagem("Imagem: "+imgSegmentacao.largura+"x"+imgSegmentacao.altura);
            
            Color color = pixReader.getColor(0, 0);
            Ci = criaCluster(0, 0, color, null);
            listaClusters = ultCluster = Ci;
            imgSegmentacao.contagemClusters = 1;
            for(j = 1; j < imgSegmentacao.largura; j++){
                Ci = imgSegmentacao.getPixelCluster(j-1, 0);
                color = pixReader.getColor(j, 0);
                dist = distanciaCores(Ci.tom,color);
                if(dist < limiar)
                    adicionaPonto(Ci,j,0,color);
                else{
                    Ci = criaCluster(j,0,color,ultCluster);
                    ultCluster.proximo = Ci;
                    ultCluster = Ci;
                    imgSegmentacao.contagemClusters++;
                }
            }
            
            for(i = 1; i < imgSegmentacao.altura; i++){
                Ci = imgSegmentacao.getPixelCluster(0, i-1);
                color = pixReader.getColor(0, i);
                dist = distanciaCores(Ci.tom,color);
                if(dist < limiar)
                    adicionaPonto(Ci,0,i,color);
                else{
                    Ci = criaCluster(0,i,color,ultCluster);
                    ultCluster.proximo = Ci;
                    ultCluster = Ci;
                    imgSegmentacao.contagemClusters++;
                }
                
                for(j = 1; j < imgSegmentacao.largura; j++){
                    Cu = imgSegmentacao.getPixelCluster(j, i-1);
                    Cl = imgSegmentacao.getPixelCluster(j-1, i);
                    color = pixReader.getColor(j,i);
                    dist = distanciaCores(Cu.tom,color);
                    if(dist < limiar){
                        adicionaPonto(Cu,j,i,color);
                        dist = distanciaCores(Cl.tom,color);
                        if(dist < limiar){
                            if(Cu != Cl){
                                imgSegmentacao.contagemClusters--;
                                Cluster anterior = ultCluster.anterior;
                                if(Cl != listaClusters){
                                    mesclaCluster(Cu,Cl);
                                    if(Cl == ultCluster)
                                        ultCluster = anterior;
                                }else{
                                    mesclaCluster(Cl,Cu);
                                    if(Cu == ultCluster)
                                        ultCluster = anterior;
                                }
                            }
                        }
                    }else{
                        dist = distanciaCores(Cl.tom,color);
                        if(dist < limiar){
                            adicionaPonto(Cl,j,i,color);
                        }else{
                            Ci = criaCluster(j,i,color,ultCluster);
                            ultCluster.proximo = Ci;
                            ultCluster = Ci;
                            imgSegmentacao.contagemClusters++;
                        }
                    }
                    
                }
                //enviaMensagem(String.format("Segmentação: %.2f%% concluído...", ((double)i)/imgSegmentacao.altura*100.0));
            }
            int minCt = (int)(minimo*imgSegmentacao.altura*imgSegmentacao.largura);
            enviaMensagem(imgSegmentacao.contagemClusters+" clusters encontrados na imagem. Removendo clusters com menos de "+minCt+" pixels...");
            
            j = 0;
            Ci = listaClusters.proximo; //Manter o cabeça.
            while(Ci != null){
                if(Ci.contagem < minCt){
                    Cluster excluir = Ci;
                    j++;
                    Ci = Ci.proximo;
                    mesclaProximo(excluir);
                    imgSegmentacao.contagemClusters--;
                    continue;
                }
                Ci = Ci.proximo;
            }
            enviaMensagem(imgSegmentacao.contagemClusters+" clusters encontrados na imagem.");
            //Enviar mensagem para o chamador.
            //Terminar o thread.
            enviaMensagem(ThreadCallback.MSG_QUIT);
            enviaDadosSegmentacao();
        }
        
        @Override
        public void run() {
            DKHFastScanning();
        }
    }
    
    private void alteraImagemVisualizacao(){
        /**
         * Altera a imagem exibida no view, com base no cluster selecionado e
         *    nos dados da segmentação.
         * Cria pixelWriter
         * Definir em vermelho os pixels do cluster selecionado
         * Definir em preto os outros clusters.
         */
        WritableImage nova = new WritableImage(dadosSegmentacao.largura,dadosSegmentacao.altura);
        PixelWriter pw = nova.getPixelWriter();
        for(int h=0; h < dadosSegmentacao.altura; h++){
            for(int w = 0;w < dadosSegmentacao.largura; w++){
                Color color;
                if(dadosSegmentacao.bitmap[h][w] == atual)
                    color = atual.tom;
                else if(atual == null)
                    color = dadosSegmentacao.bitmap[h][w].tom;
                else
                    color = Color.BLACK;
                pw.setColor(w,h,color);
            }
        }
        visuSegm.defineImagem(nova);
    }
        
    Segmentacao(Image imagem, Visualizacao viewSegmentacao) {
        /**
         * Mostrar mensagem de "Aguarde..." Chamar thread segmentador (para não
         * travar o Thread da UI) Executar segmentação Enviar mensagem para
         * thread principal Atualizar imagem Criar listbox e caixa de texto
         * Esperar troca do Listbox. Atualizar imagem novamente.
         */
        int limiar = 50;
        double minimo = 0.001; //0,1%
        visuSegm = viewSegmentacao;
        cxTexto = viewSegmentacao.adcTextoEstatico("Coletando dados...");
        cxTexto.setFont(javafx.scene.text.Font.font("Verdana", 20));
        //http://code.makery.ch/blog/javafx-dialogs-official/
        TextInputDialog dialog = new TextInputDialog("50");
        dialog.setTitle("Segmentação de imagem");
        dialog.setHeaderText("Limiar");
        dialog.setContentText("Favor insira um valor de limiar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            limiar = Integer.parseInt(result.get());
        }
        
        dialog = new TextInputDialog("0,1");
        dialog.setTitle("Segmentação de imagem");
        dialog.setHeaderText("Tamanho mínimo");
        dialog.setContentText("Especifique o tamanho mínimo da região (em porcentagem):");
        result = dialog.showAndWait();
        if (result.isPresent()) {
            String valor = result.get();
            valor = valor.replace(".", "");
            valor = valor.replace(',', '.');
            minimo = Double.parseDouble(valor);
            minimo = minimo / 100.0;
        }

        thread = new SegmentacaoThread(imagem, limiar, minimo);
        ThreadCallback thrdCallback = new ThreadCallback(){
            @Override
            public void sendMessage(int id){
                switch(id){
                    case MSG_INITIALIZING:
                        Platform.runLater(() -> cxTexto.setText("Inicializando segmentação..."));
                        break;
                    case MSG_CLUSTERING:
                        Platform.runLater(() -> cxTexto.setText("Agrupando pixels..."));
                        break;
                    case MSG_COMPACTING:
                        Platform.runLater(() -> cxTexto.setText("Removendo clusters pequenos..."));
                        break;
                    case MSG_DONE:
                        Platform.runLater(() -> cxTexto.setText("Concluído."));
                        break;
                    case MSG_QUIT:
                        Platform.runLater(() -> {
                            cxTexto.setFont(javafx.scene.text.Font.font("Verdana", 12));
                            cxTexto.setText("Finalizado. "+thread.imgSegmentacao.contagemClusters+" na imagem.");
                        });
                        break;
                }
            }
            @Override
            public void sendMessage(String message){
                Platform.runLater(() -> cxTexto.setText(message));
            }
            @Override
            public void setDadosSegmentacao(SegmentacaoImg segmDados, Cluster listaClusters){
                Platform.runLater(() -> {
                    int i;
                    Cluster Ci;
                    String[] valores;
                    dadosSegmentacao = segmDados;
                    listaRegioes = listaClusters;
                    atual = null;
                    visuSegm.limpaFerramentas();
                    alteraImagemVisualizacao();
                    cxTexto = visuSegm.adcTextoEstatico("Selecione um cluster (dos " + dadosSegmentacao.contagemClusters + ") na caixa ao lado: ");
                    valores = new String[dadosSegmentacao.contagemClusters];
                    i = 0;
                    Ci = listaRegioes;
                    while(Ci != null){
                        valores[i] = String.format("%03d: "+Ci+" (%d,%d,%d)",i,(int)(Ci.tom.getRed()*255),
                                (int)(Ci.tom.getGreen()*255),(int)(Ci.tom.getBlue()*255));
                        Ci = Ci.proximo;
                        i++;
                    }
                    lista = visuSegm.adcLista(valores);
                    lista.valueProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            int i = 0;
                            String strId = newValue.substring(0, newValue.indexOf(':'));
                            int id = Integer.parseInt(strId);
                            Cluster Ci = listaRegioes;
                            while(Ci != null){
                                if(i == id){
                                    atual = Ci;
                                    alteraImagemVisualizacao();
                                }
                                Ci = Ci.proximo;
                                i++;
                            }
                        }
                    });
                });
            }
        };
        thread.setCallback(thrdCallback);
        thread.start();
        /*Exemplo de TextInputDialog
        http://code.makery.ch/blog/javafx-dialogs-official/
        TextInputDialog dialog = new TextInputDialog("walter");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            System.out.println("Your name: " + result.get());
        }
         */
    }

    public static Segmentacao main(Image imagem, Visualizacao viewSegmentacao) {
        Segmentacao novoSegm = new Segmentacao(imagem, viewSegmentacao);
        return novoSegm;
    }
}
