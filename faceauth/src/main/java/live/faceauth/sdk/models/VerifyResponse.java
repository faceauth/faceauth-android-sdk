package live.faceauth.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

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

  public static VerifyResponse parse(String jsonStr) throws JSONException {
    JSONObject json = new JSONObject(jsonStr);
    return new VerifyResponse(
        json.getBoolean("success"),
        MatchResponse.parse(json.getString("match")),
        SpoofResponse.parse(json.getString("spoof"))
    );
  }
}
