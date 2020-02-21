package file.upload.client;

import okhttp3.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class SendFiles {

  private static Logger log = LoggerFactory.getLogger( SendFiles.class );

  public static void main( String[] args ) throws Exception {
    SendFiles sf = new SendFiles( );
    sf.t0();
  }
  public void t0 () throws Exception {
    String location = "/Users/julienrevaultdallonnes/DEV/TESTS/GITHUB/FileSplitAndUpload/testdata/";
    String fileName = "001.fastq.gz";
    String url = "http://localhost:8080/upload";


    int chunkSize = 300 * 1_048_576;

    final File sourceFile = new File(location + fileName);

    long chunks = SplitChunk.getNumberOfChunks(sourceFile, chunkSize);
    float totalSizeInMB = chunkSize * chunks / 1_048_576;

    long start = System.currentTimeMillis( );
    for (int i = 1; i <= chunks; i++) {
      File targetFile = new File(location + fileName + "." + i);
      SplitChunk.go(sourceFile , targetFile , i, chunkSize );
      //sf.go(url, targetFile, i);
    }
    float duration = ( System.currentTimeMillis( ) - start ) / 1000;
    float throughput = totalSizeInMB / duration;

    log.info( "****************" );
    log.info( "{} files of {} MB splitted in {} s at {} MB/s  : " , chunks , totalSizeInMB , duration , throughput );
    log.info( "****************" );

 }
  public static void t1 () throws Exception {
    String baseName = "700M.7z";
    String source = "D:/DEV/GO/src/data/" + baseName;
    String target = "D:/DEV/GO/src/data/TEMP/" + baseName;
    String url = "http://localhost:8080/upload";

    SendFiles sf = new SendFiles( );

    int chunkSizeInMb = 300;
    long start = System.currentTimeMillis( );
    Split split = new Split( source , target , chunkSizeInMb );
    Map<Long, String> all = split.all( );

    float durationSplit = ( System.currentTimeMillis( ) - start ) / 1000;


    start = System.currentTimeMillis( );
    long totalSizeInMB = 0;
    for ( Long chunk : all.keySet( ) ) {
      totalSizeInMB += sf.go( url , new File( all.get( chunk ) ) , chunk );
    }

    float durationNetwork = ( System.currentTimeMillis( ) - start ) / 1000;
    float throughput = totalSizeInMB / durationSplit;
    float bandwith = totalSizeInMB / durationNetwork;
    log.info( "****************" );
    log.info( "{} files of {} MB splitted in {} s at {} MB/s  : " , all.size( ) , totalSizeInMB , durationSplit ,
        throughput );
    log.info( "{} files of {} MB uploaded in {} s at {} MB/s  : " , all.size( ) , totalSizeInMB , durationNetwork ,
        bandwith );
    log.info( "****************" );

  }

  private static final MediaType MEDIA_TYPE = MediaType.parse( "application/octet-stream" );
  private final OkHttpClient client = new OkHttpClient( );

  private float go( String url , File file , long chunk ) throws Exception {

    long start = System.currentTimeMillis( );

    RequestBody requestBody = new MultipartBody.Builder( )
        .setType( MultipartBody.FORM )
        .addFormDataPart( "upload_file" , file.getName( ) , RequestBody.create( file , MEDIA_TYPE ) )
//        .addPart(
//            Headers.of( "Content-Disposition" , "form-data; name=\"title\"" ) ,
//            RequestBody.create( "FileName" , null )
//        )
        .build( );

    Request request = new Request.Builder( )
        .header( "X-Chunk" , String.valueOf( chunk ) )
        .header( "X-Bar-Id" , String.valueOf( 123 ) )
        .header( "X-Ana-Id" , String.valueOf( 1456 ) )
        .url( url )
        .post( requestBody )
        .build( );

    Response response = client.newCall( request ).execute( );
    if ( !response.isSuccessful( ) ) throw new IOException( "Unexpected code " + response );

    //System.out.println( response.body( ).string( ) );

    float duration = ( System.currentTimeMillis( ) - start ) / 1000;
    float size = file.length( ) / 1_048_576;
    float throughput = size / duration;
    log.info( "File {} of {} MB uploaded in {} s at {} MB/s  : " , file.getAbsolutePath( ) , size , duration ,
        throughput );
    return size;

  }
}
