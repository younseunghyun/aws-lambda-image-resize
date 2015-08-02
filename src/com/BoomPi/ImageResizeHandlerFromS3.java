package com.BoomPi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.BoomPi.ImageModel.JpegRawImage;
import com.BoomPi.ImageModel.RawImage;
import com.BoomPi.ImageProcess.ImageProcess;
import com.BoomPi.ImageProcess.JpegImageProcess;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/*Copyright 2015 jack.yun

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

public class ImageResizeHandlerFromS3
    implements
    RequestHandler<S3Event, String>
{

    //put the desired picture size and destination bucket to store resized image
    private static final float MAX_WIDTH = 400;
    private static final float MAX_HEIGHT = 400;
    private static final String dstBucket = "resized-image";

    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        RawImage sourceImage = null;
        try {
            sourceImage = getOriginalImage(s3Event);
        } catch (Exception e1) {
            return "fail to get original image";
        }

        float scalingFactor = Math.min(MAX_WIDTH / sourceImage.getWidth(), 
                                        MAX_HEIGHT/ sourceImage.getHeight());
        int width = (int) (scalingFactor * sourceImage.getWidth());
        int height = (int) (scalingFactor * sourceImage.getHeight());

        ImageProcess imageProcess ;
        if (sourceImage.getImageType() != null && sourceImage.getImageType().equals("jpg")) {
            imageProcess = new JpegImageProcess((JpegRawImage) sourceImage);
        }else{
            imageProcess = null;
        }
            

        try {
            return putResizedImageToS3(imageProcess, sourceImage.getS3Key(), width, height);
        } catch (IOException e) {
            return "fail to put to s3";
        }

    }

    private RawImage getOriginalImage(S3Event s3Event)
        throws Exception
    {

        S3EventNotificationRecord record = s3Event.getRecords().get(0);

        String srcBucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getKey().replace('+', ' ');
        srcKey = URLDecoder.decode(srcKey, "UTF-8");

        if (!new FormatValidator(srcKey).isValid()) throw new Exception("not valid file format");

        BufferedImage srcImage = null;
        AmazonS3 s3Client = new AmazonS3Client();

        try (S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));)
        {
            InputStream objectData = s3Object.getObjectContent();
            srcImage = ImageIO.read(objectData);
        }

        // TODO implement other type of RawImage model
        JpegRawImage jpegRawImage = new JpegRawImage(
            srcImage.getWidth(),
            srcImage.getHeight(),
            srcBucket,
            srcKey,
            srcImage
            );

        return jpegRawImage;

    }

    private String putResizedImageToS3(
        ImageProcess jpegImageProcess,
        String s3ObjectKey,
        int width,
        int height)
        throws IOException
    {
        ByteArrayOutputStream os = jpegImageProcess
            .resize(width, height)
            .getOutPutStream();
        try (InputStream is = new ByteArrayInputStream(os.toByteArray());) {

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(os.size());
            meta.setContentType(jpegImageProcess.getImageMime());

            String dstKey = String.join("_", "resized", s3ObjectKey);

            AmazonS3 s3Client = new AmazonS3Client();

            System.out.println("Writing to: " + dstBucket + "/" + dstKey);
            s3Client.putObject(dstBucket, dstKey, is, meta);
        }
        return "Ok";
    }

    private class FormatValidator

    {

        private final boolean isSrcKeyValidate;
        private final boolean isImageForamtValidate;
        private final Set<String> availImageTypeSet = new HashSet<String>();

        public FormatValidator(String srcKey) {

            setAvailImageTypeSet();

            Matcher matcher = Pattern.compile(".*\\.(\\w+)$").matcher(srcKey);

            this.isSrcKeyValidate = srcKey != null && matcher.matches();
            this.isImageForamtValidate = srcKey != null && availImageTypeSet.contains(matcher.group(1));

        }

        public boolean isValid() {
            // TODO add not valid type error logger
            if (this.isSrcKeyValidate && this.isImageForamtValidate)
                return true;
            else
                return false;
        }

        private void setAvailImageTypeSet() {
            this.availImageTypeSet.add("jpeg");
            this.availImageTypeSet.add("jpg");
            this.availImageTypeSet.add("png");
        }

    }

}
