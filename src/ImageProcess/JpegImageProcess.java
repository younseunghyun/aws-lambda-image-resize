package ImageProcess;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import ImageModel.JpegRawImage;

public class JpegImageProcess
    implements ImageProcess
{
    private JpegRawImage jpegRawImage;
    private BufferedImage resizedJpegImage; 

    @Override
    public BufferedImage resize(int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        g.setPaint(Color.white);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(jpegRawImage.getRawImage(), 0, 0, width, height, null);
        g.dispose();
        return resizedImage;

    }

    @Override
    public void volumeReduce(int targetVolume) {
        // TODO using later

    }

    public JpegImageProcess(JpegRawImage jpegRawImage) {
        this.jpegRawImage = jpegRawImage;
    }

    public JpegRawImage getJpegRawImage() {
        return jpegRawImage;
    }

    public void setJpegRawImage(JpegRawImage jpegRawImage) {
        this.jpegRawImage = jpegRawImage;
    }

    @Override
    public ByteArrayOutputStream getOutPutStream() {
        
        return null;
    }

}
