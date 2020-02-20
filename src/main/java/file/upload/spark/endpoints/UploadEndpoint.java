package file.upload.spark.endpoints;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.servlet.MultipartConfigElement;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class UploadEndpoint extends CommonEndpoint {

  private static final String CONTEXT_PATH = "/upload";


  public UploadEndpoint() {
  }

  @Override
  public void configure( Service spark , String basePath ) {
    this.spark = spark;
    String path = basePath + CONTEXT_PATH;

    spark.post( path + "/1" , this::upload_1 );

  }

  public String upload_1( Request req , Response res ) {

    long start = System.currentTimeMillis( );

    String filename = "/Users/julienrevaultdallonnes/Downloads/TEMP/" + System.currentTimeMillis( ) + ".tmp";
    final Path path = Paths.get( filename );

    req.attribute( "org.eclipse.jetty.multipartConfig" , new MultipartConfigElement( "/temp" ) );
    try ( InputStream is = req.raw( ).getPart( "upload_file" ).getInputStream( ) ) {
//            copy_0(is, path);
//            copy_1(is, path);
//            copy_2(is, path);
      copy_3( is , path );
    }
    catch ( Exception e ) {
      e.printStackTrace( );
    }

    float duration = ( System.currentTimeMillis( ) - start ) / 1000;
    float size = path.toFile( ).length( ) / 1024 / 1024;
    float throughput = size / duration;

    Map<String, String> stats = new HashMap<>( 4 );
    stats.put( "Full size" , size + " mb" );
    stats.put( "Duration" , duration + " s" );
    stats.put( "Throughput" , throughput + " mb/s" );
    stats.put( "Final file" , filename );
    stats.put( "Method" , "copy_3" );

    return send( res , stats );
  }

  private void copy_0( InputStream inputStream , Path destination ) throws Exception {
    try ( FileOutputStream output = new FileOutputStream( destination.toFile( ) ) ) {
      IOUtils.copyLarge( inputStream , output );
    }
  }

  private void copy_1( InputStream inputStream , Path destination ) throws Exception {

    Files.copy(
        inputStream ,
        destination ,
        StandardCopyOption.REPLACE_EXISTING
    );

  }

  private void copy_2( InputStream inputStream , Path destination ) throws Exception {
    byte[] buffer;
    buffer = new byte[ inputStream.available( ) ];
    inputStream.read( buffer );

    Files.write( destination , buffer );
  }

  private void copy_3( InputStream inputStream , Path destination ) throws Exception {

    FileUtils.copyInputStreamToFile( inputStream , destination.toFile( ) );
  }
}
