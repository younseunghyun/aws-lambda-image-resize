package com.BoomPi.ImageProcess;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.BoomPi.ImageModel.JpegRawImage;

public class JpegImageProcess
    implements ImageProcess
{
    private JpegRawImage jpegRawImage;
    private BufferedImage resizedJpegImage;
    private String imageType;
    private String imageMime;

    @Override
    public JpegImageProcess resize(int width, int height) {

        this.resizedJpegImage = Scalr.resize(jpegRawImage.getRawImage(), Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,
            width, height, Scalr.OP_ANTIALIAS);
        return this;

    }

    @Override
    public JpegImageProcess volumeReduce(int targetVolume) {
        // TODO using later
        return this;

    }

    public JpegImageProcess(JpegRawImage jpegRawImage) {
        this.jpegRawImage = jpegRawImage;
        this.imageType = jpegRawImage.getImageType();
        this.imageMime = jpegRawImage.getImageMime();
    }

    public JpegRawImage getJpegRawImage() {
        return jpegRawImage;
    }

    public void setJpegRawImage(JpegRawImage jpegRawImage) {
        this.jpegRawImage = jpegRawImage;
    }

    @Override
    public ByteArrayOutputStream getOutPutStream() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(this.resizedJpegImage, imageType, os);
        } catch (IOException e) {
            System.out.println("fiail to getOutputStream");
        }
        return os;
    }

    @Override
    public String getImageType() {

        return this.imageType;
    }

    @Override
    public String getImageMime() {

        return this.imageMime;
    }

}
