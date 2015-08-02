package ImageProcess;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


public interface ImageProcess {

    public BufferedImage resize(int width, int height);

    public void volumeReduce(int targetVolume);
    
    public ByteArrayOutputStream getOutPutStream();
    
}
