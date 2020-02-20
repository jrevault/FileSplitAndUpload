package file.upload.spark;

public enum StatusResponse {

  OK( "ok" ),
  KO( "ko" );

  public String status;

  StatusResponse( String status ) {
    this.status = status;
  }

}
