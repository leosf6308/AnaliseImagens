/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tcc;


import java.awt.image.*;
/**
 *
 * @author Henrique, Leonardo and Joseph
 */

public class videoFrame{
    private int id;
    private long timestampMs;
    private BufferedImage preview;
    private BufferedImage image;
    public videoFrame(long timestamp, BufferedImage prev, BufferedImage img){
        this.timestampMs = timestamp;
        this.preview = prev;
        this.image = img;
    }
    
    public BufferedImage getPreview(){
        return this.preview;
    }
    
    public BufferedImage getImage(){
        return this.image;
    }
    
    public void clear(){
        id = 0;
        timestampMs = 0;
        preview = null;
        image = null;
    }
}
