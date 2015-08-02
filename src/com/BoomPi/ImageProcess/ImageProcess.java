package com.BoomPi.ImageProcess;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


public interface ImageProcess {

    public ImageProcess resize(int width, int height);

    public ImageProcess volumeReduce(int targetVolume);
    
    public ByteArrayOutputStream getOutPutStream();
    
    public String getImageType();
    
    public String getImageMime();
}
