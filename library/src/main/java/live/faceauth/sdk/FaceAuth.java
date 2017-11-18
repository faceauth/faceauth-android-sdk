package live.faceauth.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import android.util.Log;
import live.faceauth.sdk.ui.AuthenticateActivity;
import live.faceauth.sdk.ui.PermissionsActivity;
import live.faceauth.sdk.ui.RegisterActivity;
import java.util.UUID;

public class FaceAuth {

  public static final int REGISTER_FACE_REQUEST = 5001;
  public static final int AUTHENTICATE_FACE_REQUEST = 5002;

  public static final String REGISTERED_FACE_ID = "uuid_faceid_result";
  public static final String AUTO_CLICK_MODE = "auto_click_mode";
  public static final String AUTH_RESULT = "auth_result";
  public static final String AUTH_CONFIDENCE_RESULT = "auth_confidence_result";
  public static final String AUTH_SPOOF_SCORE = "auth_spoof_score";

  private static final String FACEAUTH_API_KEY = "faceauth-api-key";

  private static final Object mLock = new Object();
  private static final String TAG = "FaceAuth";
  private static FaceAuth sInstance;

  private FaceAuthConfig mConfig;

  private String mApiKey;

  @NonNull
  public static FaceAuth getInstance() {
    if (sInstance == null) {
      synchronized (mLock) {
        sInstance = new FaceAuth();
      }
    }
    return sInstance;
  }

  private static String readApiKey(Context context) {
    try {
      final ApplicationInfo ai = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

      return ai.metaData.getString(FACEAUTH_API_KEY);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
    } catch (NullPointerException e) {
      Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
    }
    return "";
  }

  private FaceAuth() {
    this.mConfig = new FaceAuthConfig();
  }

  /**
   * Provide the endpoint and key for connecting to Azure
   *
   */
  public void initialize(Context context) {
    final String apiKey = readApiKey(context);

    if (apiKey != null && !apiKey.isEmpty()) {
      Log.d(TAG, "FaceAuth initialization successful.");
    } else {
      Log.d(TAG, "Failed to read api key. Make sure to add it in Android Manifest as meta-data.");
    }
    this.mApiKey = apiKey;
  }

  /**
   * Modify the configuration (like threshold, messages etc.)
   *
   * @param config The configuration to be used.
   */
  public void configure(FaceAuthConfig config) {
    this.mConfig = config;
  }

  /**
   * Get the current configuration
   */
  public static FaceAuthConfig getConfig() {
    return getInstance().mConfig;
  }

  /**
   * Invoke the face registration flow.
   *
   * @param caller The calling activity
   */
  public void register(Activity caller) {
    // TODO (aakashns): Set the necessary parameters
    Intent registerIntent = new Intent(caller, RegisterActivity.class);

    Intent intent = new Intent(caller, PermissionsActivity.class);
    intent.putExtra(Intent.EXTRA_INTENT, registerIntent);
    caller.startActivityForResult(intent, REGISTER_FACE_REQUEST);
  }

  /**
   * Invoke the face authentication flow.
   *
   * @param caller The calling activity
   * @param autoMode Auto click image and try to verify
   */
  public void authenticate(Activity caller, String registeredFaceId, boolean autoMode) {
    Intent authIntent = new Intent(caller, AuthenticateActivity.class);
    authIntent.putExtra(REGISTERED_FACE_ID, registeredFaceId);
    authIntent.putExtra(AUTO_CLICK_MODE, autoMode);

    Intent intent = new Intent(caller, PermissionsActivity.class);
    intent.putExtra(Intent.EXTRA_INTENT, authIntent);
    // TODO (aakashns): Set the necessary parameters
    caller.startActivityForResult(intent, AUTHENTICATE_FACE_REQUEST);
  }

  /**
   * Handle the result from register in onActivityResult
   */
  public boolean handleRegistration(
      int requestCode,
      int resultCode,
      Intent result,
      RegistrationCallback callback
  ) {
    if (requestCode == REGISTER_FACE_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        String faceId = result.getStringExtra(FaceAuth.REGISTERED_FACE_ID);
        callback.onSuccess(UUID.fromString(faceId), result.getData());
      } else {
        callback.onError(new Exception("Something went wrong"));
      }
      return true;
    }
    return false;
  }

  /**
   * Handle the result from the authenticate in onActivityResult
   */
  public boolean handleAuthentication(
      int requestCode,
      int resultCode,
      Intent result,
      AuthenticationCallback callback
  ) {
    if (requestCode == AUTHENTICATE_FACE_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        boolean isIdentical = result.getBooleanExtra(AUTH_RESULT, false);
        double confidence = result.getDoubleExtra(AUTH_CONFIDENCE_RESULT, 0.0f);
        double realConfidence = result.getDoubleExtra(AUTH_SPOOF_SCORE, 0.0f);
        int conf = (int) (confidence * 100);
        double scr = (int) (realConfidence * 100);
        if (isIdentical) {
          callback.onSuccess(conf, scr);
        } else {
          callback.onFailure(conf, scr);
        }
      } else {
        callback.onError(new Exception("Something went wrong"));
      }
      return true;
    }

    return false;
  }

  public String getApiKey() {
    return mApiKey;
  }

  /**
   * Callbacks for registration
   */
  public interface RegistrationCallback {

    /**
     * The face was successfully registered and a face ID was returned.
     */
    void onSuccess(UUID registeredFaceId, Uri imageUri);

    /**
     * Something went wrong while trying to register the face, or the user cancelled
     */
    void onError(Exception e);
  }

  /**
   * Callbacks for authentication
   */
  public interface AuthenticationCallback {

    /**
     * The authentication was successful
     */
    void onSuccess(int confidence, double score);

    /**
     * The authentication was unsuccessful
     */
    void onFailure(int confidence, double score);

    /**
     * Something went wrong while trying to authenticate the face, or the user cancelled
     */
    void onError(Exception e);
  }
}
