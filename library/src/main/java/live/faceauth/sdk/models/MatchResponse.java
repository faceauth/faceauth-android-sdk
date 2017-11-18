package live.faceauth.sdk.models;

public class MatchResponse {

  public final boolean success;

  public final boolean isIdentical;

  public final double confidence;

  public MatchResponse(boolean success, boolean isIdentical, double confidence) {
    this.success = success;
    this.isIdentical = isIdentical;
    this.confidence = confidence;
  }
}
