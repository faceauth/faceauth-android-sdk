package live.faceauth.sdk.models;

import java.util.UUID;

public class Face {

  public final UUID faceId;

  public final boolean detected;

  public Face(UUID faceId, boolean detected) {
    this.faceId = faceId;
    this.detected = detected;
  }
}
