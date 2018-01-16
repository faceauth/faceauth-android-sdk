package live.faceauth.sdk2.network;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.models.RegisterResponse;
import live.faceauth.sdk.models.VerifyResponse;
import live.faceauth.sdk.util.ImageUtil;
import org.json.JSONException;

public class FaceAuthService {
  private static final String REGISTER_NETWORK_FRAGMENT_TAG = "RegisterNetworkFragment";
  private static final String VERIFY_NETWORK_FRAGMENT_TAG = "VerifyNetworkFragment";

  private NetworkFragment registerNetworkFragment;
  private NetworkFragment verifyNetworkFragment;
  private AppCompatActivity caller;

  public FaceAuthService(AppCompatActivity activity) {
    caller = activity;
    registerNetworkFragment = NetworkFragment.getInstance(
        activity.getSupportFragmentManager(), REGISTER_NETWORK_FRAGMENT_TAG);
    verifyNetworkFragment = NetworkFragment.getInstance(
        activity.getSupportFragmentManager(), VERIFY_NETWORK_FRAGMENT_TAG);
  }

  public void register(final Uri imageUri, final FaceAuth.RegistrationCallback callback) {
    String url = FaceAuth.getConfig().baseUrl + "/api/v1/register";
    byte[] imageBytes;
    try {
      imageBytes = getImageBytes(imageUri);
    } catch (FileNotFoundException e) {
      callback.onError(e);
      return;
    }
    Request request = new Request("POST", url, imageBytes);
    request.headers.put("Content-Type", "image/jpeg");
    request.headers.put("x-faceauth-api-key", "testing-key-123");
    registerNetworkFragment.setCallback(new DownloadCallback<NetworkFragment.Result>() {
      @Override public void onComplete(NetworkFragment.Result result) {
        if (result.mResultValue != null) {
          try {
            RegisterResponse response = RegisterResponse.parse(result.mResultValue);
            callback.onSuccess(response.face.faceId, imageUri);
          } catch (JSONException e) {
            Log.e("FaceAuthService", e.getMessage(), e);
            callback.onError(e);
          }
        } else {
          callback.onError(result.mException);
        }
      }
    });
    registerNetworkFragment.execute(request);
  }

  public void verify(final Uri imageUri, String registeredId, final FaceAuth.AuthenticationCallback callback) {
    String url = FaceAuth.getConfig().baseUrl + "/api/v1/verify";
    byte[] imageBytes;
    try {
      imageBytes = getImageBytes(imageUri);
    } catch (FileNotFoundException e) {
      callback.onError(e);
      return;
    }
    Request request = new Request("POST", url, imageBytes);
    request.headers.put("Content-Type", "image/jpeg");
    request.headers.put("x-faceauth-api-key", "testing-key-123");
    request.headers.put("x-registered-face-id", registeredId);
    verifyNetworkFragment.setCallback(new DownloadCallback<NetworkFragment.Result>() {
      @Override void onComplete(NetworkFragment.Result result) {
        if (result.mResultValue != null) {
          try {
            VerifyResponse response = VerifyResponse.parse(result.mResultValue);
            callback.onSuccess((int)(response.match.confidence * 100), response.spoof.confidence);
          } catch (JSONException e) {
            Log.e("FaceAuthService", e.getMessage(), e);
            callback.onError(e);
          }
        } else {
          callback.onError(result.mException);
        }
      }
    });
    verifyNetworkFragment.execute(request);
  }

  private byte[] getImageBytes(Uri imageUri) throws FileNotFoundException{
    InputStream stream = caller.getContentResolver().openInputStream(imageUri);
    Bitmap image = ImageUtil.getScaledBitmap(stream, 360);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 80, bos);
    return bos.toByteArray();
  }

}
