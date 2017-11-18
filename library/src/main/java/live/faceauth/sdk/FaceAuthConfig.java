package live.faceauth.sdk;

public class FaceAuthConfig {
    /**
     * The threshold for considering a match successful during
     * authentication.
     */
    public double matchThreshold = 0.5;

    /**
     * If true, the user can retry if the authentication fails.
     */
    public boolean allowAuthRetry = true;

    /**
     * If true, no capture button is shown during the authentication flow.
     * Instead a picture is taken automatically.
     */
    public boolean takePictureAutomatically = true;

    /**
     * If true, a camera sound is played during registration.
     */
    public boolean registrationCameraSound = true;

    /**
     * If true, a camera sound is played during authentication when the picture
     * is taken.
     */
    public boolean authenticationCameraSound = false;

    /**
     * If true, liveness detection is performed during authentication.
     */
    public boolean enableLivenessDetection = true;

    /**
     * If true, a rectangle is shown around the face in the live camera feed.
     */
    public boolean showFaceRectangle = true;
}
