package live.faceauth.sdk.models;

public class VerifyResponse {

  public final boolean success;

  public final MatchResponse match;

  public final SpoofResponse spoof;

  public VerifyResponse(boolean success, MatchResponse match,
      SpoofResponse spoof) {
    this.success = success;
    this.match = match;
    this.spoof = spoof;
  }
}
