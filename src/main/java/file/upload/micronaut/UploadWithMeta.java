package file.upload.micronaut;

import io.micronaut.http.multipart.StreamingFileUpload;

import java.util.Map;

public class UploadWithMeta {
  StreamingFileUpload file;
  Map<String, Object> metadata;

  public void setFile( StreamingFileUpload file ) {
    this.file = file;
  }

  public void setMetadata( Map<String, Object> metadata ) {
    this.metadata = metadata;
  }
}