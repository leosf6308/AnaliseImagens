/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
import com.xuggle.xuggler.video.IConverter;
import com.xuggle.xuggler.video.ConverterFactory;
import javafx.application.Platform;

import java.awt.image.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Henrique, Leonardo and Joseph
 */
public class videoQuadros extends Thread{
    public String strNomeArquivo;
    private IContainer container;
    private int videoStreamId;
    private IStreamCoder videoCoder;
    private IVideoResampler resampler;
    private IPacket packet;
    private Player player;
    private int msgID;
    private int params[] = new int[4];
    public static final int MSG_OBTEM_PREVIEWS = 3;
    public static final int MSG_FECHA = 65535;
    
    private void abreVideo(){
        container = IContainer.make();
        if (container.open(strNomeArquivo, IContainer.Type.READ, null) < 0)
            throw new RuntimeException("Coudn't open the file " + strNomeArquivo);
        
        int numStreams = container.getNumStreams();
        
        videoStreamId = -1;
        videoCoder = null;
        
        for(int i = 0; i < numStreams; i++){
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO){
                videoStreamId = i;
                videoCoder = coder;
            }
        }
        
        if (videoStreamId == -1 || videoCoder == null)
            throw new RuntimeException("Could not find video stream in container: "+strNomeArquivo);
        if(videoCoder.open() < 0)
            throw new RuntimeException("Could not open video decoder for container: "+strNomeArquivo);
        resampler = null;
        /*if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24){
            
        }*/
        
        packet = IPacket.make();
    }
    
    private void avancaParaSegundo(int second){
        container.seekKeyFrame(videoStreamId, second, 0);
        while(container.readNextPacket(packet) >= 0){
            if (packet.getStreamIndex() == videoStreamId){
                IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
                if (bytesDecoded >= 0){
                    if(picture.isComplete()){
                        if(picture.getTimeStamp()/1000000 >= second){
                            System.out.println("Timestamp: "+picture.getTimeStamp()+". Formatted: "+picture.getFormattedTimeStamp());
                            break;
                        }  
                    }
                }
            }
        } 
    }
    
    private BufferedImage obtemMiniatura(int width){
        BufferedImage retImg = null;
        
        while(container.readNextPacket(packet) >= 0){
            if (packet.getStreamIndex() == videoStreamId){
                IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
                if (bytesDecoded < 0)
                    throw new RuntimeException("Error decoding video in: " + this.strNomeArquivo);
                if (picture.isComplete()){
                    IVideoPicture newPic = picture;
                    int divFactor = picture.getWidth()/width;
                    int height = picture.getHeight()/divFactor;
                    if(resampler == null){
                        resampler = IVideoResampler.make(width, height, IPixelFormat.Type.BGR24,
                        videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                        if (resampler == null)
                            throw new RuntimeException("Could not create color space resampler for: " + strNomeArquivo);
                    }
                    if (resampler != null){
                        newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), width, height);
                        if (resampler.resample(newPic, picture) < 0)
                            throw new RuntimeException("Could not resample video from: " + this.strNomeArquivo + ". ");
                    }
                    if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                        throw new RuntimeException("Could not decode video as BGR 24 bit data in: " + strNomeArquivo);
                    IConverter converter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, newPic);
                    retImg = converter.toImage(newPic);
                    break;
                }
            }
        }
        return retImg;
    }
    
    private void criaListaPreview(){
        int second = params[0];
        int delayMs = params[1];
        int previewWidth = params[2];
        long lastTimestamp, delay;
        int frame;
        videoFrame[] previewList = new videoFrame[5];
        BufferedImage image, preview;
        IConverter converter = null;
        IConverter previewConverter = null;
        avancaParaSegundo(second);
        lastTimestamp = second*1000000;
        delay = delayMs*1000;
        frame = 0;
        while(container.readNextPacket(packet) >= 0){
            if (packet.getStreamIndex() == videoStreamId){
                IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
                if(bytesDecoded >= 0){
                    if (picture.isComplete()){
                        if(picture.getTimeStamp() >= lastTimestamp+delay){
                            if(converter == null)
                                converter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, picture);
                            image = converter.toImage(picture);
                            IVideoPicture newPic = picture;
                            int divFactor = picture.getWidth()/previewWidth;
                            int height = picture.getHeight()/divFactor;
                            if(resampler == null){
                                resampler = IVideoResampler.make(previewWidth, height, IPixelFormat.Type.BGR24,
                                videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                                if (resampler == null)
                                    throw new RuntimeException("Could not create color space resampler for: " + strNomeArquivo);
                            }
                            if (resampler != null){
                                newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), previewWidth, height);
                                if (resampler.resample(newPic, picture) < 0)
                                    throw new RuntimeException("Could not resample video from: " + this.strNomeArquivo + ". ");
                            }
                            if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                                throw new RuntimeException("Could not decode video as BGR 24 bit data in: " + strNomeArquivo);
                            if(previewConverter == null)
                                previewConverter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, newPic);
                            preview = previewConverter.toImage(newPic);

                            lastTimestamp += delay;
                            previewList[frame] = new videoFrame(lastTimestamp/1000L,preview,image);
                            
                            frame++;
                            if(frame >= 5)
                                break;
                        }
                    }
                }
            }
        }
        player.vidFrames = previewList;
    }
        
    private BufferedImage obtemProxQuadro(){
        BufferedImage retImg = null;
        packet = IPacket.make();
        while(container.readNextPacket(packet) >= 0){
            if (packet.getStreamIndex() == videoStreamId){
                IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                //System.out.println("Decoding...");
                int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
                if (bytesDecoded < 0)
                    throw new RuntimeException("Error decoding video in: " + this.strNomeArquivo);
                if (picture.isComplete()){
                    IConverter converter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, picture);
                    retImg = converter.toImage(picture);
                    break;
                }
            }
        }
        return retImg;
    }
    
    private void fechaVideo(){
        if (videoCoder != null){
            videoCoder.close();
            videoCoder = null;
        }
        if (container !=null){
            container.close();
            container = null;
        }
    }
    
    @Override
    public void run(){
        abreVideo();
        while(msgID != MSG_FECHA){
            if(msgID == MSG_OBTEM_PREVIEWS){
                criaListaPreview();
                videoQuadros esteVidQuadros = this;
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        //your code here
                        player.mostraMiniaturas();
                        esteVidQuadros.limpaMsg();
                    }
                });
            }
                
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("Exception:"+ex.getMessage());
            }
        }
        fechaVideo();
    }
    
    public static videoQuadros criaVidQuadros(String nomeArquivo){
        videoQuadros xugglerReader = new videoQuadros();
        xugglerReader.strNomeArquivo = nomeArquivo;
        xugglerReader.player = null;
        xugglerReader.msgID = 0;
        xugglerReader.start();
        return xugglerReader;
    }
    
    public static void main(String args[]){
        (new videoQuadros()).start();
    }
    
    public void lerPreviews(Player vidPlayer, int segundo, int atraso, int largPreview){
        player = vidPlayer;
        msgID = videoQuadros.MSG_OBTEM_PREVIEWS;
        params[0] = segundo;
        params[1] = atraso;
        params[2] = largPreview;
        /*
        player passes strArquivo

        whenever it needs previews, it would call lerPreviews
                this function will set Player, second, delay and previewWidth

        videoQuadros thread will be checking for the validity of Player, second, delay and previewWidth
                when they all are valid, it will generate the previews, store it in Player.vidFrames

        */
    }
    
    public void destroiVidQuadros(){
        msgID = videoQuadros.MSG_FECHA;
    }
    
    public void limpaMsg(){
        player = null;
        msgID = 0;
        params[0] = 0;
        params[1] = 0;
        params[2] = 0;
        params[3] = 0;
    }
}
