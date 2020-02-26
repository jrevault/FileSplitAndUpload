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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SendBytes {

    private static Logger log = LoggerFactory.getLogger(SendBytes.class);


    public static void main(String[] args) throws Exception {

        long totalSizeInMB = 5000;
        String baseName = "2.5G";
        String source_dir = "testdata/";
        String url = "http://localhost:5050/upload";
        long CHUNK_SIZE = 5 * 1_048_576;

        FileFilter fileFilter = f -> f.getName().startsWith(baseName);
        File[] files = Paths.get(source_dir).toFile().listFiles(fileFilter);

        SendBytes sf = new SendBytes();

        long startAll = System.currentTimeMillis();

        sf.bytes(files, url, CHUNK_SIZE);

        float duration = (System.currentTimeMillis() - startAll) / 1000;
        float throughput = totalSizeInMB / duration;
        log.info("****************");
        log.info("{} files of {} MB processed in {} s at {} MB/s : ", files.length, totalSizeInMB, duration, throughput);
        log.info("****************");
    }

    public void bytes(File[] files, String url, long CHUNK_SIZE) throws Exception {

//        final Observable<File> fileObservable = Observable.fromArray(files).subscribeOn(Schedulers.io());
//        fileObservable
//                .observeOn(Schedulers.computation())
//                .doOnNext(f -> bytes(f, url, CHUNK_SIZE))
//                .subscribe(length -> System.out.println("item length " + length));


        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Set<Callable<String>> callables = new HashSet<Callable<String>>();

        for (AtomicInteger file_i = new AtomicInteger(0); file_i.get() < files.length; file_i.getAndIncrement()) {
            final int a = file_i.get();
            log.info("Treating file  n°" + a);
            callables.add(() -> {
                try {
                    bytes(files[a], url, CHUNK_SIZE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "Done file  n°" + a;
            });
        }

        try {
            List<Future<String>> futures = executorService.invokeAll(callables);

            executorService.shutdown();

            //executorService.awaitTermination(1, TimeUnit.HOURS);

            for (Future<String> future : futures) {
                System.out.println("resultat = " + future.get());
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        }

    }

    public void bytes(File sourceFile, String url, long CHUNK_SIZE) throws Exception {

        log.info(sourceFile.getName() );

        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        long chunks = (long) Math.ceil((double) sourceChannel.size() / (double) CHUNK_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(Math.toIntExact(CHUNK_SIZE));

        for (int chunk = 1; chunk <= chunks; chunk ++) {
            buffer.clear();
            long position = (chunk - 1) * CHUNK_SIZE;
            byte[] chunkBuffer = SendBytes.getBytes(sourceChannel, buffer, position);

            go(url, chunkBuffer, chunk, sourceFile.getName() + ".bytes." + chunk);
        }
        sourceChannel.close();
    }


    private static byte[] getBytes(FileChannel sourceChannel, ByteBuffer buffer, long position) throws IOException {
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

}
