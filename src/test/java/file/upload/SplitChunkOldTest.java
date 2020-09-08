package file.upload;

import file.upload.client.SplitChunkOld;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SplitChunkOldTest {

    @Test
    void t_1() {
        try {
          String source = "testdata/001.fastq.gz";
          String target = "testdata/2M.chunk";
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel targetChannel = new FileOutputStream(target).getChannel();
            int chunkSizeInMB = 2;
            int bufferSizeInMB = 1;

            long start = System.currentTimeMillis();
            SplitChunkOld split = new SplitChunkOld(sourceChannel, targetChannel, chunkSizeInMB, bufferSizeInMB);
            split.work(2);
            System.out.println(this.getClass().getSimpleName() + ".t_1 : " + (System.currentTimeMillis() - start));

            assertTrue(exists(target));
        } catch (Exception e) {
            fail(e);
        }

    }

    @Test
    void t_2() {
        try {
          String source = "testdata/001.fastq.gz";
          String target = "testdata/200M.chunk";
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel targetChannel = new FileOutputStream(target).getChannel();
            int chunkSizeInMB = 200;

            long start = System.currentTimeMillis();
            SplitChunkOld split = new SplitChunkOld(sourceChannel, targetChannel, chunkSizeInMB);
            split.work(1);
            System.out.println(this.getClass().getSimpleName() + ".t_2 : " + (System.currentTimeMillis() - start));

            assertTrue(exists(target));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void t_3() {
        try {
          String source = "testdata/001.fastq.gz";
          String target = "testdata/160M.chunk";
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel targetChannel = new FileOutputStream(target).getChannel();
            int chunkSizeInMB = 200;

            long start = System.currentTimeMillis();
            SplitChunkOld split = new SplitChunkOld(sourceChannel, targetChannel, chunkSizeInMB);
            split.work(2);
            System.out.println(this.getClass().getSimpleName() + ".t_3 : " + (System.currentTimeMillis() - start));

            assertTrue(exists(target));
        } catch (Exception e) {
            fail(e);
        }
    }


    // ********************** Usefull methods
    private boolean exists( String target ) {
        assert target != null;
        File file = new File(target);
        boolean exists = file.exists();
        file.delete();
        return exists;
    }
}