package file.upload.client;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class SendFiles {

  private static Logger log = LoggerFactory.getLogger( SendFiles.class );

  public static void main( String[] args ) throws Exception {
    SendFiles sf = new SendFiles( );
    sf.t0( );
  }

  // MUST BE LESS THAN MAX INT
  int CHUNK_SIZE = 300 * 1_048_576;

  public void t0() throws Exception {
    //String fileName = "2.5G.7z";
    String fileName = "11G.7z";
    String source_dir = "testdata/";

    String url = "http://localhost:8080/upload";

    final File sourceFile = new File( source_dir + fileName );
    long fileLength = sourceFile.length( );
    long chunks = ( long ) Math.ceil( ( double ) fileLength / ( double ) CHUNK_SIZE );
    float totalSizeInMB = fileLength / 1_048_576f;

    long startAll = System.currentTimeMillis( );
    final AtomicInteger chunk = new AtomicInteger( 1 );
    for ( ; chunk.get( ) <= chunks ; chunk.getAndIncrement( ) ) {
      //File targetFile = new File( target_dir + fileName + "." + i );
      //SplitChunk.go( sourceFile , targetFile , i.get() , CHUNK_SIZE );

//      Observable<File> names = Observable.just( targetFile );
//      final Observable<File> async = names.subscribeOn( Schedulers.io( ) );
//      async.subscribe( n -> go( url , targetFile , i.get() ) );

      //    ByteBuffer buffer = ByteBuffer.allocateDirect( CHUNK_SIZE );
//    fileChannel.position((chunk - 1) * CHUNK_SIZE);
//    int bytesRead = fileChannel.read( buffer );

//      WritableByteChannel writableByteChannel = Channels.newChannel( new ByteArrayOutputStream( CHUNK_SIZE ) );
//      ReadableByteChannel readableByteChannel = Channels.newChannel( new ByteArrayInputStream( CHUNK_SIZE ) );
//      long bytesRead = fileChannel.transferTo( (chunk - 1) * CHUNK_SIZE, CHUNK_SIZE, writableByteChannel);
//      ByteBuffer buffer = ByteBuffer.wrap(result.getRawBytes());
//
//      writableByteChannel.close();
//    writableByteChannel.

//    buffer.flip();
//    buffer.rewind();

      int position = ( chunk.get( ) - 1 ) * CHUNK_SIZE;
      int sizeChunk = Math.toIntExact( Math.min( fileLength - position , CHUNK_SIZE ) );

//      try ( RandomAccessFile reader = new RandomAccessFile( source_dir + fileName , "r" ) ;
//            FileChannel fileChannel = reader.getChannel( ) ;
//            ByteArrayOutputStream ignored = new ByteArrayOutputStream( ) ) {

      // KLOUG : https://www.baeldung.com/java-filechannel
      // Go back to FileChannel and TransferTo a ByteBuffer mapped to a byte[]
      ByteBuffer buf = ...
      byte[] arr = new byte[ buf.remaining( ) ];
      buf.get( arr );
      // KLOUG END

      byte[] bFile = new byte[ CHUNK_SIZE ];
      IOUtils.read( new FileInputStream( sourceFile ) , bFile , position , sizeChunk );

//        MappedByteBuffer buff = fileChannel.map( FileChannel.MapMode.READ_ONLY , position , CHUNK_SIZE );
//        long bytesRead = 0;
//        if ( buff.hasRemaining( ) ) {
//          byte[] data = new byte[ buff.remaining( ) ];
//          buff.get( data );
//          bytesRead += data.length;
//        }

      long start = System.currentTimeMillis( );

      go( url , bFile , chunk.get( ) , fileName + "." + chunk.get( ) );

      float duration = ( System.currentTimeMillis( ) - start ) / 1000;
      sizeChunk /= 1_048_576;
      float throughput = sizeChunk / duration;
      log.info( "File {} of {} MB uploaded in {} s at {} MB/s  : " , fileName + "." + chunk.get( ) , sizeChunk ,
          duration , throughput );
//      }


    }
    float duration = ( System.currentTimeMillis( ) - startAll ) / 1000;
    float throughput = totalSizeInMB / duration;

    log.info( "****************" );
    log.info( "{} files of {} MB splitted in {} s at {} MB/s  : " , chunks , totalSizeInMB , duration , throughput );
    log.info( "****************" );

  }

  private static final MediaType MEDIA_TYPE = MediaType.parse( "application/octet-stream" );
  private final OkHttpClient client = new OkHttpClient( );


  private void go( String url , byte[] bytes , long chunk , String chunk_name ) throws Exception {

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

    //System.out.println( response.body( ).string( ) );

  }
}
