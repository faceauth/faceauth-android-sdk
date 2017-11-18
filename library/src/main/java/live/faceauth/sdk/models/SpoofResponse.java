package live.faceauth.sdk.models;

public class SpoofResponse {

  public final boolean success;

  public final String message;

  public final boolean isReal;

  public final double confidence;

  public SpoofResponse(boolean success, String message, boolean isReal, double confidence) {
    this.success = success;
    this.message = message;
    this.isReal = isReal;
    this.confidence = confidence;
  }
}
