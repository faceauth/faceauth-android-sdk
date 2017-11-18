package live.faceauth.sdk.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.FaceAuthConfig;
import live.faceauth.sdk.R;
import live.faceauth.sdk.models.MatchResponse;
import live.faceauth.sdk.models.SpoofResponse;
import live.faceauth.sdk.models.VerifyResponse;
import live.faceauth.sdk.ui.camera.CameraSourcePreview;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import live.faceauth.sdk.util.CameraManager;
import live.faceauth.sdk.util.FaceInfo;
import live.faceauth.sdk.network.ApiHelper;
import live.faceauth.sdk.util.ImageUtil;
import live.faceauth.sdk.util.PermissionUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AuthenticateActivity extends AppCompatActivity implements View.OnClickListener,
    CameraManager.PictureClickListener {

  private static final String TAG = "AuthenticateActivity";

  private GraphicOverlay mGraphicOverlay;
  private ProgressDialog mProgressDialog;
  private CameraManager mCameraManager;
  private UUID mRegisteredFaceId;
  private TextView mTopMessaging;
  private boolean mProcessing = false;
  private boolean mAutoClickMode = false;
  private SparseArray<FaceInfo> mFaceInfo = new SparseArray<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_authenticate);

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage("Processing...");

    CameraSourcePreview preview = findViewById(R.id.preview);
    mGraphicOverlay = findViewById(R.id.faceOverlay);
    ImageButton captureButton = findViewById(R.id.button_capture);
    mTopMessaging = findViewById(R.id.top_messaging);

    captureButton.setOnClickListener(this);

    mRegisteredFaceId = UUID.fromString(getIntent().getStringExtra(FaceAuth.REGISTERED_FACE_ID));

    mAutoClickMode = FaceAuth.getConfig().takePictureAutomatically;

    if (mAutoClickMode) {
      captureButton.setVisibility(View.GONE);
    } else {
      captureButton.setVisibility(View.VISIBLE);
    }

    mCameraManager = new CameraManager(this, mGraphicOverlay, getWindowManager(),
        preview, this);

    if (!FaceAuth.getConfig().authenticationCameraSound) {
      // TODO (siddhant): Disable the camera sound here.
    }

    // Check for the camera permission before accessing the camera.  If the
    // permission is not granted yet, request permission.
    List<String> permissions = PermissionUtil.checkMissingPermissions(this, CAMERA,
        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
    if (permissions.isEmpty()) {
      mCameraManager.createCameraSource();
    } else {
      finish();
    }
  }

  private void showProgress() {
    mProgressDialog.show();
  }

  private void hideProgress() {
    mProgressDialog.hide();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mCameraManager.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCameraManager.onPause();
  }

  @Override
  protected void onDestroy() {
    mCameraManager.onDestroy();
    super.onDestroy();
  }

  private void captureImage() {
    showProgress();
    mCameraManager.captureImage();
  }

  @Override public void onClick(View view) {
    if (view.getId() == R.id.button_capture) {
      final List<String> requestPermissions = PermissionUtil.checkMissingPermissions(this, CAMERA,
          WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);

      if (requestPermissions.isEmpty()) {
        captureImage();
      } else {
        finish();
      }
    }
  }

  @Override public void onPictureClick(byte[] bytes) {
    android.util.Log.d(TAG, "onPictureClick");
    mCameraManager.stopPreview();
    processAuthentication(bytes);
  }

  @Override public void onPictureSave(Uri uri) {
  }

  final Runnable mUpdateFaceRunnable = new Runnable() {
    @Override public void run() {
      if (mAutoClickMode) {

        if (mFaceInfo.size() == 1 && !mProcessing) {
          // capture image

          FaceInfo faceInfo = mFaceInfo.valueAt(0);
          if (faceInfo.getResetOnCheck()) {
            faceInfo.reset();
          }

          if (faceInfo.shouldCaptureImage()) {
              captureImage();
              mProcessing = true;
              faceInfo.setResetOnCheck();
          }
        }

        final int messageId = FaceInfo.getHintMessageId(mFaceInfo, mProcessing);
        mTopMessaging.setText(messageId);
      }
    }
  };

  @Override
  @WorkerThread
  public void updateFaces(final SparseArray<FaceInfo> faceInfo) {
    if (mAutoClickMode) {
      mFaceInfo = faceInfo;
      runOnUiThread(mUpdateFaceRunnable);
    }
  }

  private void processAuthentication(byte[] data) {
    final Bitmap bitmap = ImageUtil.getScaledBitmap(data, 360);

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

    final InputStream bs = new ByteArrayInputStream(bos.toByteArray());
    verify(bs);
  }

  private void verify(InputStream stream) {
    ApiHelper.verify(mRegisteredFaceId, stream, new Callback<VerifyResponse>() {
      @Override
      public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
        if (response.isSuccessful() && response.body().success &&
            response.body().match.success && response.body().spoof.success) {

          final VerifyResponse verifyResponse = response.body();
          handleAuthResult(verifyResponse.match, verifyResponse.spoof);

          android.util.Log.d("verify", "success");
        } else {
          android.util.Log.d("verify", "unsuccessful");
          finishProcessing();
        }
      }

      @Override public void onFailure(Call<VerifyResponse> call, Throwable t) {
        android.util.Log.d("verify", "failure", t);
        finishProcessing();
      }
    });
  }

  private void finishProcessing() {
    mCameraManager.resetCamera();
    hideProgress();
    mProcessing = false;
  }

  private void handleAuthResult(@NonNull MatchResponse matchResponse,
      @NonNull SpoofResponse spoofResponse) {

    hideProgress();

    final FaceAuthConfig config = FaceAuth.getConfig();
    final boolean isSuccess =
        spoofResponse.isReal && matchResponse.confidence > config.matchThreshold;

    final double matchConfidence = matchResponse.confidence;
    final double realConfidence = spoofResponse.confidence;

    if (!isSuccess && config.allowAuthRetry) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.face_auth_failed)
          .setMessage(getString(R.string.auth_failed_no_face))
          .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
              finishProcessing();
            }
          })
          .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              returnAuthResult(false, matchConfidence, realConfidence);
            }
          })
          .show();
    } else {
      returnAuthResult(isSuccess, matchConfidence, realConfidence);
    }
  }

  private void returnAuthResult(boolean isSuccess, double confidence, double realConfidence) {
    Intent data = new Intent();
    data.putExtra(FaceAuth.AUTH_RESULT, isSuccess);
    data.putExtra(FaceAuth.AUTH_CONFIDENCE_RESULT, confidence);
    data.putExtra(FaceAuth.AUTH_SPOOF_SCORE, realConfidence);
    setResult(RESULT_OK, data);
    finish();
  }
}
