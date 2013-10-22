/**
 * 
 */
package org.carcv.impl.core.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.carcv.core.input.VideoHandler;
import org.carcv.core.model.file.FileEntry;

/**
 * {@linkplain https://trac.ffmpeg.org/wiki/Create%20a%20video%20slideshow%20from%20images}
 * @author oskopek
 */
public class FFMPEG_VideoHandler extends VideoHandler {//TODO: test FFMPEG vid disector

    final private static int defaultFrameRate = 30;
    
    final private static String image_suffix = ".png";
    
    final private static String video_suffix = ".h264";

    public static boolean disectToFrames(Path video) {
        FFMPEG_VideoHandler fvd = new FFMPEG_VideoHandler();
        try {
            return fvd.disectToFrames(video, defaultFrameRate);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static OutputStream generateVideoAsStream(Path imageDir) {
        FFMPEG_VideoHandler fvd = new FFMPEG_VideoHandler();
        try {
            return fvd.generateVideoAsStream(imageDir, defaultFrameRate);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void generateVideoAsStream(final FileEntry entry, final OutputStream outStream) throws IOException {
        FFMPEG_VideoHandler fvd = new FFMPEG_VideoHandler();
        Path dir = entry.getCarImages().get(0).getFilepath().getParent();
        
        fvd.generateVideoAsStream(dir, defaultFrameRate, outStream);
    }

    @Override
    public boolean disectToFrames(Path video, int frameRate) throws IOException {

        Path dir = Paths.get(video.getParent().toString(), video.getFileName() + ".dir");
        Files.createDirectory(dir);

        return disectToFrames(dir, video, frameRate);
    }

    @Override
    public boolean disectToFrames(Path dir, Path video, int frameRate) throws IOException {

        String filenamePrefix = video.getFileName().toString();

        Path images = Paths.get(dir.toString(), filenamePrefix + "-%09d" + image_suffix);

        String command = "ffmpeg -i " + video.toString() + " -r " + frameRate + " " + images.toString();

        System.out.println("Executing: " + command);

        Process p = Runtime.getRuntime().exec(command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    @Override
    public Path generateVideo(Path imageDir, int frameRate) throws IOException {
        return createVideo(imageDir, frameRate);
    }
    
    public void generateVideoAsStream(Path imageDir, int frameRate, final OutputStream outStream) throws IOException {
        Path tmp = createVideo(imageDir, frameRate);
        Files.copy(tmp, outStream);
        
    }

    @Override
    public OutputStream generateVideoAsStream(Path imageDir, int frameRate) throws IOException {
        Path tmp = createVideo(imageDir, frameRate);
        OutputStream ostream = new FileOutputStream(tmp.toFile());
        return ostream;
        
    }

    @Override
    public void generateVideo(Path imageDir, Path videoPath, int frameRate) throws IOException {
        Path tmp = createVideo(imageDir, frameRate);
        Files.move(tmp, videoPath, StandardCopyOption.ATOMIC_MOVE);
    }
    
    private Path createVideo(Path imageDir, int frameRate) throws IOException {
        Path output = Files.createTempFile("video", video_suffix);

        String command = "ffmpeg -r " + frameRate + " -pattern_type glob -i '" + 
        imageDir.toAbsolutePath().toString() + File.separator + "*" + image_suffix +
        "' -c:v libx264 -pix_fmt yuv420p " + output.toAbsolutePath().toString();

        System.out.println("Executing: " + command);

        Process p = Runtime.getRuntime().exec(command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            return null;
        }
        
        return output;
    }

}
