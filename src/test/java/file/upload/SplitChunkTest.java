package file.upload;

import file.upload.client.SplitChunk;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SplitChunkTest {

    @Test
    void t_1() {
        try {
            String source = "testdata/2.5G.7z";
            String target = "testdata/TEMP/t_1.chunk";
            int chunkSizeInMB = 500 * 1_048_576;

            long start = System.currentTimeMillis();
            SplitChunk.go(source, target, 2, chunkSizeInMB);
            System.out.println(this.getClass().getSimpleName() + ".t_1 : " + (System.currentTimeMillis() - start));

            assertTrue(exists(target));
        } catch (Exception e) {
            fail(e);
        }

    }

    @Test
    void t_2() {
        try {
            String source = "testdata/2.5G.7z";
            String target = "testdata/TEMP/t_2.chunk";
            int chunkSizeInMB = 500 * 1_048_576;

            long start = System.currentTimeMillis();
            SplitChunk.go(source, target, 1, chunkSizeInMB);
            System.out.println(this.getClass().getSimpleName() + ".t_2 : " + (System.currentTimeMillis() - start));

            assertTrue(exists(target));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void t_3() {
        try {
            String source = "testdata/2.5G.7z";
            String target = "testdata/TEMP/t_3.chunk";
            int chunkSizeInMB = 500 * 1_048_576;

            long start = System.currentTimeMillis();
            SplitChunk.go(source, target, 2, chunkSizeInMB);
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