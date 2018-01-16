package live.faceauth.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterResponse {

  public final boolean success;

  public final Face face;

  public RegisterResponse(boolean success, Face face) {
    this.success = success;
    this.face = face;
  }

  public static RegisterResponse parse(String jsonStr) throws JSONException {
    JSONObject json = new JSONObject(jsonStr);
    Face face = Face.parse(json.getString("face"));
    boolean success = json.getBoolean("success");
    return new RegisterResponse(success, face);
  }
}
