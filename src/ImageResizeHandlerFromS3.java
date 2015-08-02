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

import ImageModel.JpegRawImage;
import ImageModel.RawImage;
import ImageProcess.ImageProcess;
import ImageProcess.JpegImageProcess;





import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class ImageResizeHandlerFromS3
    implements
    RequestHandler<S3Event, String>
{
    private static final float MAX_WIDTH = 400;
    private static final float MAX_HEIGHT = 400;

    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        RawImage sourceImage = null;
        try {
            sourceImage = getOriginalImage(s3Event);
        } catch (Exception e1) {
            return "fail to get original image";
        }

        float scalingFactor = Math.min(MAX_WIDTH / sourceImage.getWidth(), MAX_HEIGHT
            / sourceImage.getHeight());
        int width = (int) (scalingFactor * sourceImage.getWidth());
        int height = (int) (scalingFactor * sourceImage.getHeight());

        ImageProcess jpegImageProcess = new JpegImageProcess((JpegRawImage) sourceImage);

        try {
            return putResizeImageToS3(sourceImage, width, height, jpegImageProcess);
        } catch (IOException e) {
            return "fail to put to s3";
        }

    }

    private String putResizeImageToS3(RawImage sourceImage, int width, int height, ImageProcess jpegImageProcess)
        throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(jpegImageProcess.resize(width, height), sourceImage.getImageType(), os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(os.size());
        meta.setContentType(sourceImage.getImageMime());

        String dstBucket = "resized-image";
        String dstKey = String.join("_", "resized", sourceImage.getS3Key());

        AmazonS3 s3Client = new AmazonS3Client();

        System.out.println("Writing to: " + dstBucket + "/" + dstKey);
        s3Client.putObject(dstBucket, dstKey, is, meta);
        System.out.println("Successfully resized " + sourceImage.getS3Bucket() + "/"
            + sourceImage.getS3Key() + " and uploaded to " + dstBucket + "/" + dstKey);
        return "Ok";
    }

    private RawImage getOriginalImage(S3Event s3Event)
        throws Exception
    {

        S3EventNotificationRecord record = s3Event.getRecords().get(0);

        String srcBucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getKey().replace('+', ' ');
        srcKey = URLDecoder.decode(srcKey, "UTF-8");

        if (!new FormatValidator(srcKey).isValid()) throw new Exception("not valid file format");

        System.out.println(String.format("valid file format srcBucket : %s srcKey : %s", srcBucket, srcKey));

        BufferedImage srcImage = null;
        AmazonS3 s3Client = new AmazonS3Client();

        try (S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));)
        {
            InputStream objectData = s3Object.getObjectContent();
            srcImage = ImageIO.read(objectData);
        }

        System.out.println(String.format("get file from srcBucket : %s srcKey", srcBucket, srcKey));

        int srcHeight = srcImage.getHeight();
        int srcWidth = srcImage.getWidth();

        // TODO implement other type of RawImage model
        JpegRawImage jpegRawImage = new JpegRawImage(
            srcWidth,
            srcHeight,
            srcBucket,
            srcKey,
            srcImage
            );

        return jpegRawImage;

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
