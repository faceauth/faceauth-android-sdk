package live.faceauth.sdk2.network;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import live.faceauth.sdk.FaceAuth;

public class FaceAuthService {
  private static final String REGISTER_NETWORK_FRAGMENT_TAG = "RegisterNetworkFragment";
  private static final String VERIFY_NETWORK_FRAGMENT_TAG = "VerifyNetworkFragment";

  private NetworkFragment registerNetworkFragment;
  private NetworkFragment verifyNetworkFragment;

  public FaceAuthService(AppCompatActivity activity) {
    registerNetworkFragment = NetworkFragment.getInstance(
        activity.getSupportFragmentManager(), REGISTER_NETWORK_FRAGMENT_TAG);
  }

  public void register(Bitmap image, DownloadCallback<NetworkFragment.Result> callback) {
    String url = FaceAuth.getConfig().baseUrl + "/api/v1/register";
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 80, bos);

    Request request = new Request("POST", url, bos.toByteArray());
    request.headers.put("Content-Type", "image/jpeg");
    registerNetworkFragment.setCallback(callback);
    registerNetworkFragment.execute(request);
  }

  public void verify(Bitmap image, String registeredId, DownloadCallback<String> callback) {

  }
}
