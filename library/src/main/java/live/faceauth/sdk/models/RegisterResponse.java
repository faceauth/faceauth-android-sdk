package live.faceauth.sdk.models;

public class RegisterResponse {

  public final boolean success;

  public final Face face;

  public RegisterResponse(boolean success, Face face) {
    this.success = success;
    this.face = face;
  }
}
