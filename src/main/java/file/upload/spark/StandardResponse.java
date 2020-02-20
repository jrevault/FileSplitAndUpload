package file.upload.spark;

import com.google.gson.JsonElement;

public class StandardResponse {


  public StatusResponse status;
  public String message;
  public JsonElement data;

  public StandardResponse( StatusResponse status ) {
    this.status = status;
  }

  public StandardResponse( StatusResponse status , String message ) {
    this.status = status;
    this.message = message;
  }

  public StandardResponse( StatusResponse status , JsonElement data ) {
    this.status = status;
    this.data = data;
  }
}
