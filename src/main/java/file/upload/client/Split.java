package file.upload.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class Split {

  private static final Logger LOG = LoggerFactory.getLogger( Split.class );

  private static final int DEFAULT_CHUNK_SIZE_IN_MB = 10;
  private static final int DEFAULT_BUFFER_SIZE_IN_MB = 256;
  private static final int START_CHUNK = 1;

  //  private String source;
  private String target;

  private long sourceSize;
  private long chunkSize;
  private int bufferSize;

  private FileChannel sourceChannel;
  private ByteBuffer buffer;
  private long numberOfChunks;

  // Kloug : remove ?
  int namePadSize;

  public Split( String source , String target ) throws IOException {
    init( source , target , DEFAULT_CHUNK_SIZE_IN_MB , DEFAULT_BUFFER_SIZE_IN_MB );
  }

  public Split( String source , String target , int chunkSizeInMB ) throws IOException {
    init( source , target , chunkSizeInMB , DEFAULT_BUFFER_SIZE_IN_MB );
  }

  public Split( String source , String target , int chunkSizeInMB , int bufferSizeInMB ) throws IOException {
    init( source , target , chunkSizeInMB , bufferSizeInMB );
  }

  private void init( String source , String target , int chunkSizeInMB , int bufferSizeInMB ) throws IOException {
//    this.source = source;
    this.target = target;
    // Go Bytes
    chunkSize = chunkSizeInMB * 1_048_576;
    bufferSize = bufferSizeInMB * 1_048_576;

    sourceChannel = new FileInputStream( source ).getChannel( );
    sourceSize = sourceChannel.size( );
    numberOfChunks = ( long ) Math.ceil( ( double ) sourceSize / ( double ) chunkSize );
    buffer = ByteBuffer.allocateDirect( bufferSize );

    // Padsize is for naming .split.001 .split.123 etc.
    namePadSize = ( int ) Math.floor( Math.log10( numberOfChunks ) + 1 );

    LOG.info( "N°of chunks {}" , numberOfChunks );
    LOG.info( "Chunk size  {}" , chunkSize );
    LOG.info( "Buffer size {}" , bufferSize );
  }

  /**
   * Splits the file
   */
  @SuppressWarnings( "WeakerAccess" )
  public Map<Long, String> all() throws Exception {
    return between( START_CHUNK , numberOfChunks );
  }

  /**
   * Splits the file on the specified given chunk only
   */
  @SuppressWarnings( "WeakerAccess" )
  public Map<Long, String> only( long chunk ) throws Exception {
    return between( chunk , chunk );
  }

  /**
   * Splits the file starting at a specified chunk until the end of the file
   *
   * @param startChunk the starting chunk
   */
  @SuppressWarnings( "WeakerAccess" )
  public Map<Long, String> from( long startChunk ) throws Exception {
    return between( startChunk , numberOfChunks );
  }

  /**
   * Splits the file starting at beginning and ending at a specified chunk
   *
   * @param endChunk the ending chunk
   */
  @SuppressWarnings( "WeakerAccess" )
  public Map<Long, String> until( long endChunk ) throws Exception {
    return between( START_CHUNK , endChunk );
  }

  /**
   * Splits the file starting at a specified chunk until the next specified one
   *
   * @param startChunk the starting chunk
   * @param endChunk   the ending chunk
   */
  @SuppressWarnings( "WeakerAccess" )
  public Map<Long, String> between( long startChunk , long endChunk ) throws Exception {
    return work( startChunk , endChunk );
  }

  /**
   * Splits the file starting at a specified chunk until the next specified one
   *
   * @param startChunk the starting chunk
   * @param endChunk   the ending chunk
   */
  private Map<Long, String> work( long startChunk , long endChunk ) throws Exception {

    if ( startChunk < START_CHUNK ) {
      LOG.info( "Start chunk is 0 or less..." );
      LOG.info( "How should I proceed ? A file chunk is at least the first (1) one, not 0 or less..." );
      throw new Exception( "Start chunk is 0 or less..." );
    }
    else if ( startChunk > numberOfChunks ) {
      LOG.info( "Start chunk is more than the total number of chunks : can't work in this conditions!" );
      throw new Exception( "Start chunk is more than the total number of chunks" );
    }
    else if ( startChunk > endChunk ) {
      LOG.info( "Start chunk > end chunk : Try reverting arguments :)" );
      throw new Exception( "Start chunk > end chunk" );
    }

    long currentChunk = startChunk;
    long totalBytesRead = 0;          // total bytes read from channel
    long totalBytesWritten = 0;       // total bytes written to output
    long outputChunkBytesWritten = 0; // number of bytes written to chunk so far

    // Kloug : remove ?
    String outputFileFormat = "%s.%0" + namePadSize + "d";

    Map<Long, String> splitFiles = new HashMap<>( ( int ) numberOfChunks );

    try {

      FileChannel outputChannel = null; // output channel (split file) we are currently writing

      try {
        // Resuming at the end of previous chunk
        long position = ( currentChunk - 1 ) * chunkSize;
        LOG.info( "Resuming after chunk {} at position {}" , currentChunk , position );
        sourceChannel.position( position );
        int bytesRead = sourceChannel.read( buffer );

        while ( bytesRead != -1 ) {
          totalBytesRead += bytesRead;

          LOG.debug( "Read {} bytes from channel; total bytes read {}/{} " , bytesRead , totalBytesRead , sourceSize );

          // Flip buffer : before we were writing inside buffer, after we are reading from buffer
          buffer.flip( );

          int bytesWrittenFromBuffer = 0; // number of bytes written from buffer

          while ( buffer.hasRemaining( ) ) {
            if ( outputChannel == null ) {
              outputChunkBytesWritten = 0;
              String outputName = String.format( outputFileFormat , target , currentChunk );
              if ( currentChunk > endChunk ) {
                closeChannel( outputChannel );
                break;
              }
              currentChunk++;

              LOG.debug( "Creating new output channel {}" , outputName );
              outputChannel = new FileOutputStream( outputName ).getChannel( );
              splitFiles.put( currentChunk , outputName );
            }

            // maxmimum free space in chunk
            long chunkBytesFree = chunkSize - outputChunkBytesWritten;
            // maximum bytes that should be read from current byte buffer
            int bytesToWrite = ( int ) Math.min( buffer.remaining( ) , chunkBytesFree );

            LOG.debug( "Byte buffer has {} remaining bytes; chunk has {} bytes free; writing up to {} bytes to chunk" ,
                buffer.remaining( ) , chunkBytesFree , bytesToWrite
            );

            buffer.limit( bytesWrittenFromBuffer + bytesToWrite ); // set limit in buffer up to where bytes can be read

            int bytesWritten = outputChannel.write( buffer );

            outputChunkBytesWritten += bytesWritten;
            bytesWrittenFromBuffer += bytesWritten;
            totalBytesWritten += bytesWritten;

            LOG.debug( "Wrote {} to chunk; {} bytes written to chunk so far; {} bytes written from buffer so far; {} " +
                    "bytes written in total" ,
                bytesWritten , outputChunkBytesWritten , bytesWrittenFromBuffer , totalBytesWritten
            );

            buffer.limit( bytesRead ); // reset limit

            if ( totalBytesWritten == sourceSize ) {
              LOG.info( "Finished writing last chunk" );
              closeChannel( outputChannel );
              outputChannel = null;
              break;
            }
            else if ( outputChunkBytesWritten == chunkSize ) {
              LOG.debug( "Chunk at capacity; closing()" );

              closeChannel( outputChannel );
              outputChannel = null;
            }
          }

          buffer.clear( );
          bytesRead = sourceChannel.read( buffer );
        }
      }
      finally {
        closeChannel( outputChannel );
      }
    }
    finally {
      closeChannel( sourceChannel );
    }
    return splitFiles;

  }


  private static void closeChannel( FileChannel channel ) {
    if ( channel != null ) {
      try {
        channel.close( );
      }
      catch ( Exception ignore ) {
      }
    }
  }
}