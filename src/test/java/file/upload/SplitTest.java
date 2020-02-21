package file.upload;

import file.upload.client.Split;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class SplitTest {

  @Test
  void full_9M() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.all( );

      assertEquals( 5 , countFiles( outDir , baseName ) );
    }
    catch ( Exception e ) {
      fail( e );
    }

  }

  @Test
  void from_last_1() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int chunk = 5;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.from( chunk );

      assertEquals( 1 , countFiles( outDir , baseName ) );
    }
    catch ( Exception e ) {
      fail( e );
    }
  }

  @Test
  void from_last_2() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int chunk = 4;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.from( chunk );

      assertEquals( 2 , countFiles( outDir , baseName ) );
    }
    catch ( Exception e ) {
      fail( e );
    }
  }

  @Test
  void between_2_and_4() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int startChunk = 2;
    int endChunk = 4;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.between( startChunk , endChunk );

      assertEquals( 3 , countFiles( outDir , baseName ) );
    }
    catch ( Exception e ) {
      fail( e );
    }
  }

  @Test
  void between_4_and_2() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int startChunk = 4;
    int endChunk = 2;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.between( startChunk , endChunk );

      fail( "Should not succeed" );
      assertEquals( 3 , countFiles( outDir , baseName ) );
    }
    catch ( Exception e ) {
      assertEquals( "Start chunk > end chunk" , e.getMessage( ) );
    }
  }

  @Test
  void from_after_last_chunk() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int startChunk = 3333333;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.from( startChunk );

      fail( "Should not succeed" );
    }
    catch ( Exception e ) {
      assertEquals( "Start chunk is more than the total number of chunks" , e.getMessage( ) );
    }
  }

  @Test
  void from_before_first_chunk() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int startChunk = -12;
    int chunkSizeInMB = 2;
//    String target = "/Users/julienrevaultdallonnes/Downloads/TEMP/2G.fastq.split";
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.from( startChunk );

      fail( "Should not succeed" );
    }
    catch ( Exception e ) {
      assertEquals( "Start chunk is 0 or less..." , e.getMessage( ) );
    }
  }

  @Test
  void source_does_not_exists() {
    String source = "/wrong_path_to_file";
    int chunkSizeInMB = 2;
    String outDir = "D:/DEV/GO/src/data/TEMP/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.all( );

      fail( "Should not succeed" );
    }
    catch ( Exception e ) {
      assertTrue( e instanceof FileNotFoundException );
      assertTrue( e.getMessage( ).contains( "wrong_path_to_file" ) );
    }
  }

  @Test
  void destination_folder_does_not_exists() {
    String source = "D:/DEV/GO/src/data/9M.csv";
    int chunkSizeInMB = 2;
    String outDir = "/wrong_path_to_folder/";
    String baseName = "9M.test";
    try {
      Split split = new Split( source , outDir + baseName , chunkSizeInMB );
      split.all( );

      fail( "Should not succeed" );
    }
    catch ( Exception e ) {
      assertTrue( e instanceof FileNotFoundException );
      assertTrue( e.getMessage( ).contains( "wrong_path_to_folder" ) );
    }
  }


  // ********************** Usefull methods
  private int countFiles( String outDir , String baseName ) {
    FileFilter fileFilter = f -> f.getName( ).startsWith( baseName );
    File[] files = Paths.get( outDir ).toFile( ).listFiles( fileFilter );
    assert files != null;
    Arrays.stream( files ).forEach( File::delete );
    return files.length;
  }
}