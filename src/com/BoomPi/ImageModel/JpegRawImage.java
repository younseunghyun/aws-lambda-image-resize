package com.BoomPi.ImageModel;

import java.awt.image.BufferedImage;

public class JpegRawImage
    extends RawImage
{
    private final static String JPG_TYPE = (String) "jpg";
    private final static String JPG_MIME = (String) "image/jpeg";

    public JpegRawImage(
        int width,
        int height,
        String s3Bucket,
        String s3Key,
        BufferedImage rawImage)
    {
        super(width, height, s3Bucket, s3Key, JPG_TYPE, JPG_MIME);
        this.rawImage = rawImage;
    }

    private BufferedImage rawImage;

    public BufferedImage getRawImage() {
        return rawImage;
    }

    public void setRawImage(BufferedImage rawImage) {
        this.rawImage = rawImage;
    }

}
