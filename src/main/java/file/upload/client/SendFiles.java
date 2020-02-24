package file.upload.client;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SendFiles {

  private static Logger log = LoggerFactory.getLogger( SendFiles.class );

  public static void main( String[] args ) throws Exception {
    SendFiles sf = new SendFiles( );

    //        String fileName = "2.5G.7z";
//        String fileName = "2.5G.7z";
    String fileName = "1G.txt";
    String source_dir = "testdata/";
    String url = "http://localhost:5050/upload";
    String target_dir = "testdata/TEMP/";
    int CHUNK_SIZE = 100 * 1_048_576;

    final File sourceFile = new File( source_dir + fileName );
    float totalSizeInMB = sourceFile.length( ) / 1_048_576f;
    long chunks = ( long ) Math.ceil( ( double ) sourceFile.length( ) / ( double ) CHUNK_SIZE );

    long startAll = System.currentTimeMillis( );

    //sf.file( source_dir , fileName , url , CHUNK_SIZE , target_dir );

    float duration = ( System.currentTimeMillis( ) - startAll ) / 1000;
    float throughput = totalSizeInMB / duration;

    log.info( "****************" );
    log.info( "{} files of {} MB processed in {} s at {} MB/s : " , chunks , totalSizeInMB , duration , throughput );
    log.info( "****************" );

    startAll = System.currentTimeMillis( );

    sf.bytes( source_dir , fileName , url , CHUNK_SIZE );

    duration = ( System.currentTimeMillis( ) - startAll ) / 1000;
    throughput = totalSizeInMB / duration;

    log.info( "****************" );
    log.info( "{} files of {} MB processed in {} s at {} MB/s : " , chunks , totalSizeInMB , duration , throughput );
    log.info( "****************" );
  }

  // MUST BE LESS THAN MAX INT

  public void file( String source_dir , String fileName , String url , int CHUNK_SIZE , String target_dir ) throws Exception {

//    RandomAccessFile sourceFile = new RandomAccessFile( source_dir + fileName , "r" );
    File sourceFile = new File( source_dir + fileName );
    long chunks = ( long ) Math.ceil( ( double ) sourceFile.length( ) / ( double ) CHUNK_SIZE );

    for ( AtomicInteger chunk = new AtomicInteger( 1 ) ; chunk.get( ) <= chunks ; chunk.getAndIncrement( ) ) {
      File targetFile = new File( target_dir + fileName + "." + chunk.get( ) );
      FileChannel targetChannel = new FileOutputStream( targetFile ).getChannel( );

      SplitChunk.go( new FileInputStream( sourceFile ).getChannel( ) , targetChannel , chunk.get( ) , CHUNK_SIZE );
      go( url , targetFile , chunk.get( ) , fileName + ".file." + chunk.get( ) );
    }
  }

  public void bytes( String source_dir , String fileName , String url , int CHUNK_SIZE ) throws Exception {

    FileChannel sourceChannel = new RandomAccessFile( source_dir + fileName , "r" ).getChannel( );
    long chunks = ( long ) Math.ceil( ( double ) sourceChannel.size( ) / ( double ) CHUNK_SIZE );
    ByteBuffer buffer = ByteBuffer.allocate( CHUNK_SIZE );

    for ( AtomicInteger chunk = new AtomicInteger( 1 ) ; chunk.get( ) <= chunks ; chunk.getAndIncrement( ) ) {
      buffer.clear( );
      int position = ( chunk.get( ) - 1 ) * CHUNK_SIZE;
      byte[] chunkBuffer = getBytes( CHUNK_SIZE , sourceChannel , buffer , position );
      go( url , chunkBuffer , chunk.get( ) , fileName + ".bytes." + chunk.get( ) );
    }
    sourceChannel.close( );
  }


  private byte[] getBytes( int CHUNK_SIZE , FileChannel sourceChannel , ByteBuffer buffer , int position ) throws IOException {
    sourceChannel.position( position );
    sourceChannel.read( buffer );

    buffer.flip( );
    byte[] chunkBuffer = new byte[ buffer.remaining( ) ];
    buffer.get( chunkBuffer );
    return chunkBuffer;
  }

  private static final MediaType MEDIA_TYPE = MediaType.parse( "application/octet-stream" );
  private final OkHttpClient client = new OkHttpClient( );


  private boolean go( String url , byte[] bytes , long chunk , String chunk_name ) throws Exception {

    RequestBody requestBody = new MultipartBody.Builder( )
        .setType( MultipartBody.FORM )
        .addFormDataPart( "upload_file" , chunk_name , RequestBody.create( bytes , MEDIA_TYPE ) )
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
    response.close( );
    return response.isSuccessful( );

  }

  private void go( String url , File file , long chunk , String chunk_name ) throws Exception {

    RequestBody requestBody = new MultipartBody.Builder( )
        .setType( MultipartBody.FORM )
        .addFormDataPart( "upload_file" , chunk_name , RequestBody.create( file , MEDIA_TYPE ) )
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
    response.close( );

  }
}
