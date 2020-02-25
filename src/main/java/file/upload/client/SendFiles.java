package file.upload.client;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SendFiles {

    private static Logger log = LoggerFactory.getLogger(SendFiles.class);

    public static void main(String[] args) throws Exception {

        long totalSizeInMB = 5000;
        String baseName = "2.5G";
        String source_dir = "testdata/";
        String url = "http://localhost:5050/upload";
        long CHUNK_SIZE = 100 * 1_048_576;

        FileFilter fileFilter = f -> f.getName().startsWith(baseName);
        File[] files = Paths.get(source_dir).toFile().listFiles(fileFilter);

        SendFiles sf = new SendFiles();

        long startAll = System.currentTimeMillis( );

//        sf.file(files, url, CHUNK_SIZE, "testdata/TEMP/");

        float duration = ( System.currentTimeMillis( ) - startAll ) / 1000;
        float throughput = totalSizeInMB / duration;
        log.info( "****************" );
        log.info( "{} files of {} MB processed in {} s at {} MB/s : " , files.length , totalSizeInMB , duration , throughput );
        log.info( "****************" );

        startAll = System.currentTimeMillis( );

        sf.bytes(files, url, CHUNK_SIZE);

        duration = ( System.currentTimeMillis( ) - startAll ) / 1000;
        throughput = totalSizeInMB / duration;
        log.info( "****************" );
        log.info( "{} files of {} MB processed in {} s at {} MB/s : " , files.length , totalSizeInMB , duration , throughput );
        log.info( "****************" );



    }

    public int file( File[] files, String url, long CHUNK_SIZE, String target_dir) throws Exception {

        for (int i = 0; i < files.length; i++) {
            File sourceFile = files[i];
            long chunks = (long) Math.ceil((double) sourceFile.length() / (double) CHUNK_SIZE);

            for (AtomicInteger chunk = new AtomicInteger(1); chunk.get() <= chunks; chunk.getAndIncrement()) {
                File targetFile = new File(target_dir + sourceFile.getName() + "." + chunk.get());
                FileChannel targetChannel = new FileOutputStream(targetFile).getChannel();

                SplitChunk.go(new FileInputStream(sourceFile).getChannel(), targetChannel, chunk.get(), CHUNK_SIZE);
                go(url, targetFile, chunk.get(), sourceFile.getName() + ".file." + chunk.get());
            }
        }
        return files.length;
    }

    public int bytes( File[] files, String url, long CHUNK_SIZE) throws Exception {

        for (int i = 0; i < files.length; i++) {
            File sourceFile = files[i];

            FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
            long chunks = (long) Math.ceil((double) sourceChannel.size() / (double) CHUNK_SIZE);
            ByteBuffer buffer = ByteBuffer.allocate(Math.toIntExact(CHUNK_SIZE));

            for (AtomicLong chunk = new AtomicLong(1); chunk.get() <= chunks; chunk.getAndIncrement()) {
                buffer.clear();
                long position = (chunk.get() - 1) * CHUNK_SIZE;
                byte[] chunkBuffer = getBytes(sourceChannel, buffer, position);
                go(url, chunkBuffer, chunk.get(), sourceFile.getName() + ".bytes." + chunk.get());
            }
            sourceChannel.close();
        }
        return files.length;
    }


    private byte[] getBytes(FileChannel sourceChannel, ByteBuffer buffer, long position) throws IOException {
        sourceChannel.position(position);
        sourceChannel.read(buffer);

        buffer.flip();
        byte[] chunkBuffer = new byte[buffer.remaining()];
        buffer.get(chunkBuffer);
        return chunkBuffer;
    }

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private final OkHttpClient client = new OkHttpClient();


    private boolean go(String url, byte[] bytes, long chunk, String chunk_name) throws Exception {

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_file", chunk_name, RequestBody.create(bytes, MEDIA_TYPE))
//        .addPart(
//            Headers.of( "Content-Disposition" , "form-data; name=\"title\"" ) ,
//            RequestBody.create( "FileName" , null )
//        )
                .build();

        Request request = new Request.Builder()
                .header("X-Chunk", String.valueOf(chunk))
                .header("X-Bar-Id", String.valueOf(123))
                .header("X-Ana-Id", String.valueOf(1456))
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        response.close();
        return response.isSuccessful();

    }

    private void go(String url, File file, long chunk, String chunk_name) throws Exception {

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_file", chunk_name, RequestBody.create(file, MEDIA_TYPE))
//        .addPart(
//            Headers.of( "Content-Disposition" , "form-data; name=\"title\"" ) ,
//            RequestBody.create( "FileName" , null )
//        )
                .build();

        Request request = new Request.Builder()
                .header("X-Chunk", String.valueOf(chunk))
                .header("X-Bar-Id", String.valueOf(123))
                .header("X-Ana-Id", String.valueOf(1456))
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        response.close();

    }
}
