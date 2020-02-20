package file.upload.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties( "my" )
public class MyProperties {

  @NotBlank
  public String destination;

  public Path getDestination() {
    return Paths.get( destination );
  }

  public Path getDestination( String file_name ) {
    return Paths.get( destination , file_name );
  }
}
