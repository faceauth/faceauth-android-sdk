package live.faceauth.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public class MatchResponse {

  public final boolean success;

  public final boolean isIdentical;

  public final double confidence;

  public MatchResponse(boolean success, boolean isIdentical, double confidence) {
    this.success = success;
    this.isIdentical = isIdentical;
    this.confidence = confidence;
  }

  public static MatchResponse parse(String jsonStr) throws JSONException {
    JSONObject json = new JSONObject(jsonStr);
    return new MatchResponse(
        json.getBoolean("success"),
        json.getBoolean("isIdentical"),
        json.getDouble("confidence")
    );
  }
}
