package live.faceauth.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

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

  public static SpoofResponse parse(String jsonStr) throws JSONException {
    JSONObject json = new JSONObject(jsonStr);
    return new SpoofResponse(
        json.getBoolean("success"),
        json.getString("message"),
        json.getBoolean("isReal"),
        json.getDouble("confidence")
    );
  }
}
