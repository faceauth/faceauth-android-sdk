package live.faceauth.sdk.models;

import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class Face {

  public final UUID faceId;

  public final boolean detected;

  public Face(UUID faceId, boolean detected) {
    this.faceId = faceId;
    this.detected = detected;
  }

  public static Face parse(String jsonStr) throws JSONException {
    JSONObject json = new JSONObject(jsonStr);
    UUID faceId = UUID.fromString(json.getString("faceId"));
    boolean detected = json.getBoolean("detected");
    return new Face(faceId, detected);
  }
}
