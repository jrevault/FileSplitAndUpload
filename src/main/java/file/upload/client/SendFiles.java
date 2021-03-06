package file.upload.client;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

        long startAll = System.currentTimeMillis();

        sf.file(files, url, CHUNK_SIZE, "testdata/TEMP/");

        float duration = (System.currentTimeMillis() - startAll) / 1000;
        float throughput = totalSizeInMB / duration;
        log.info("****************");
        log.info("{} files of {} MB processed in {} s at {} MB/s : ", files.length, totalSizeInMB, duration, throughput);
        log.info("****************");

    }

    public void file(File[] files, String url, long CHUNK_SIZE, String target_dir) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Set<Callable<String>> callables = new HashSet<Callable<String>>();

        for (AtomicInteger file_i = new AtomicInteger(0); file_i.get() < files.length; file_i.getAndIncrement()) {
            final int a = file_i.get();
            log.info("Treating file  n°" + a);
            callables.add(new Callable<String>() {
                public String call() {
                    try {
                        file(files[a], url, CHUNK_SIZE, "testdata/TEMP/");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "Done file  n°" + a;
                }
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


//        for (int i = 0; i < files.length; i++) {
//            file(files[i], url, CHUNK_SIZE, "testdata/TEMP/");
//        }
    }

    public void file(File sourceFile, String url, long CHUNK_SIZE, String target_dir) throws Exception {

        long chunks = (long) Math.ceil((double) sourceFile.length() / (double) CHUNK_SIZE);

        for (AtomicInteger chunk = new AtomicInteger(1); chunk.get() <= chunks; chunk.getAndIncrement()) {
            File targetFile = new File(target_dir + sourceFile.getName() + "." + chunk.get());
            FileChannel targetChannel = new FileOutputStream(targetFile).getChannel();

            SplitChunk.go(new FileInputStream(sourceFile).getChannel(), targetChannel, chunk.get(), CHUNK_SIZE);
            go(url, targetFile, chunk.get(), sourceFile.getName() + ".file." + chunk.get());
        }
    }

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private final OkHttpClient client = new OkHttpClient();


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
