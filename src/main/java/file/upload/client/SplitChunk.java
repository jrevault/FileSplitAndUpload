package file.upload.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SplitChunk {

    private static final Logger LOG = LoggerFactory.getLogger(SplitChunk.class);

    private static final int START_CHUNK = 1;


    private SplitChunk() {
    }

    public static void go(String sourcePath, String targetPath, int chunk, long chunkSizeInBytes) throws Exception {
        go(new FileInputStream(sourcePath).getChannel(), new FileOutputStream(targetPath).getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(String sourcePath, File targetFile, int chunk, long chunkSizeInBytes) throws Exception {
        go(new FileInputStream(sourcePath).getChannel(), new FileOutputStream(targetFile).getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(File sourceFile, String targetPath, int chunk, long chunkSizeInBytes) throws Exception {
        go(new FileInputStream(sourceFile).getChannel(), new FileOutputStream(targetPath).getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(File sourceFile, File targetFile, int chunk, long chunkSizeInBytes) throws Exception {
        go(new FileInputStream(sourceFile).getChannel(), new FileOutputStream(targetFile).getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(FileInputStream sourceStream, File targetFile, int chunk, long chunkSizeInBytes) throws Exception {
        go(sourceStream.getChannel(), new FileOutputStream(targetFile).getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(File sourceFile, FileInputStream targetStream, int chunk, long chunkSizeInBytes) throws Exception {
        go(new FileInputStream(sourceFile).getChannel(), targetStream.getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(FileInputStream sourceStream, FileOutputStream targetStream, int chunk, long chunkSizeInBytes) throws Exception {
        go(sourceStream.getChannel(), targetStream.getChannel(), chunk, chunkSizeInBytes);
    }

    public static void go(FileChannel sourceChannel, FileChannel targetChannel, int chunk,  long chunkSizeInBytes) throws Exception {
        work(sourceChannel, targetChannel, chunk,chunkSizeInBytes );
    }

    private static void work(FileChannel sourceChannel, FileChannel targetChannel, int chunk,  long chunkSizeInBytes) throws Exception {

        long numberOfChunks = getNumberOfChunks(sourceChannel, chunkSizeInBytes);

        if (chunk < START_CHUNK) {
            LOG.info("Start chunk is 0 or less...");
            LOG.info("How should I proceed ? A file chunk is at least the first (1) one, not 0 or less...");
            throw new Exception("Start chunk is 0 or less...");
        } else if (chunk > numberOfChunks) {
            LOG.info("Start chunk is more than the total number of chunks {} : can't work in this conditions!", numberOfChunks);
            throw new Exception("Start chunk is more than the total number of chunks");
        }

        try {
            // Resuming at the end of previous chunk
            sourceChannel.position((chunk - 1) * chunkSizeInBytes);
            targetChannel.transferFrom( sourceChannel , 0 , chunkSizeInBytes );
        } finally {
            closeChannel(targetChannel);
            closeChannel(sourceChannel);
        }

    }

    public static long getNumberOfChunks(FileChannel sourceChannel, long chunkSizeInBytes) throws IOException {
        return (long) Math.ceil((double) sourceChannel.size() / (double) chunkSizeInBytes);
    }

    public static long getNumberOfChunks(File sourceFile, long chunkSizeInBytes) throws IOException {
        return (long) Math.ceil((double) sourceFile.length() / (double) chunkSizeInBytes);
    }


    private static void closeChannel(FileChannel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception ignore) {
            }
        }
    }
}