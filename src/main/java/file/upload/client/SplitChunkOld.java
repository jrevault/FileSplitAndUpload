package file.upload.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class SplitChunkOld {

  private static final Logger LOG = LoggerFactory.getLogger( SplitChunkOld.class );

  private static final int DEFAULT_CHUNK_SIZE_IN_MB = 10;
  private static final int DEFAULT_BUFFER_SIZE_IN_MB = 256;
  private static final int START_CHUNK = 1;

  private long sourceSize;
  private long chunkSize;
  private int bufferSize;

  private FileChannel sourceChannel;
  private FileChannel targetChannel;
  private ByteBuffer buffer;
  private long numberOfChunks;


  public SplitChunkOld(FileChannel sourceChannel, FileChannel targetChannel ) throws IOException {
    init( sourceChannel , targetChannel , DEFAULT_CHUNK_SIZE_IN_MB , DEFAULT_BUFFER_SIZE_IN_MB );
  }

  public SplitChunkOld(FileChannel sourceChannel , FileChannel targetChannel , int chunkSizeInMB ) throws IOException {
    init( sourceChannel , targetChannel, chunkSizeInMB , DEFAULT_BUFFER_SIZE_IN_MB );
  }

  public SplitChunkOld(FileChannel sourceChannel , FileChannel targetChannel , int chunkSizeInMB , int bufferSizeInMB ) throws IOException {
    init( sourceChannel , targetChannel , chunkSizeInMB , bufferSizeInMB );
  }

  private void init( FileChannel sourceChannel , FileChannel targetChannel , int chunkSizeInMB , int bufferSizeInMB ) throws IOException {
    this.sourceChannel = sourceChannel;
    this.targetChannel = targetChannel;
    // Go Bytes
    chunkSize = chunkSizeInMB * 1_048_576;
    bufferSize = bufferSizeInMB * 1_048_576;

    sourceSize = sourceChannel.size( );
    numberOfChunks = ( long ) Math.ceil( ( double ) sourceSize / ( double ) chunkSize );
    buffer = ByteBuffer.allocateDirect( bufferSize );

    LOG.info( "N°of chunks {}" , numberOfChunks );
    LOG.info( "Chunk size  {}" , chunkSize );
    LOG.info( "Buffer size {}" , bufferSize );
  }

  public void work( long chunk ) throws Exception {

    if ( chunk < START_CHUNK ) {
      LOG.info( "Start chunk is 0 or less..." );
      LOG.info( "How should I proceed ? A file chunk is at least the first (1) one, not 0 or less..." );
      throw new Exception( "Start chunk is 0 or less..." );
    }
    else if ( chunk > numberOfChunks ) {
      LOG.info( "Start chunk is more than the total number of chunks : can't work in this conditions!" );
      throw new Exception( "Start chunk is more than the total number of chunks" );
    }

    long totalBytesWritten = 0;       // total bytes written to output
    long outputChunkBytesWritten = 0; // number of bytes written to chunk so far

    try {

      try {
        // Resuming at the end of previous chunk
        sourceChannel.position( ( chunk - 1 ) * chunkSize );
        int bytesRead = sourceChannel.read( buffer );

        while ( bytesRead != -1 ) {
          if (! targetChannel.isOpen()) {
            break;
          }

          // Flip buffer : before we were writing inside buffer, after we are reading from buffer
          buffer.flip( );

          int bytesWrittenFromBuffer = 0; // number of bytes written from buffer

          while ( buffer.hasRemaining( ) ) {

            // maxmimum free space in chunk
            long chunkBytesFree = chunkSize - outputChunkBytesWritten;
            // maximum bytes that should be read from current byte buffer
            int bytesToWrite = ( int ) Math.min( buffer.remaining( ) , chunkBytesFree );

            buffer.limit( bytesWrittenFromBuffer + bytesToWrite ); // set limit in buffer up to where bytes can be read

            int bytesWritten = targetChannel.write( buffer );

            outputChunkBytesWritten += bytesWritten;
            bytesWrittenFromBuffer += bytesWritten;
            totalBytesWritten += bytesWritten;

            buffer.limit( bytesRead ); // reset limit

            if ( totalBytesWritten == sourceSize ) {
              LOG.info( "Finished writing last chunk" );
              closeChannel( targetChannel );
              break;
            }
            else if ( outputChunkBytesWritten == chunkSize ) {
              LOG.debug( "Chunk at capacity; closing()" );
              closeChannel( targetChannel );
              break;
            }
          }

          buffer.clear( );
          bytesRead = sourceChannel.read( buffer );
        }
      }
      finally {
        closeChannel( targetChannel );
      }
    }
    finally {
      closeChannel( sourceChannel );
    }

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