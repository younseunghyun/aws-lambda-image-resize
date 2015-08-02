package com.BoomPi.ImageModel;

public class RawImage {
    private int width;
    private int height;
    private String s3Bucket;
    private String s3Key;
    private String imageType;
    private String imageMime;

    public RawImage(int width, int height, String s3Bucket, String s3Key, String imageType, String imageMime) {
        
        this.width = width;
        this.height = height;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.imageType = imageType;
        this.setImageMime(imageMime);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getImageMime() {
        return imageMime;
    }

    public void setImageMime(String imageMime) {
        this.imageMime = imageMime;
    }

}
