package live.faceauth.sdk.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.R;
import live.faceauth.sdk.ui.camera.CameraSourcePreview;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import live.faceauth.sdk.util.CameraManager;
import live.faceauth.sdk.util.FaceInfo;
import live.faceauth.sdk.util.PermissionUtil;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,
    CameraManager.PictureClickListener {

  private static final String TAG = "RegisterActivity";
  private static final int REQUEST_PICK_IMAGE = 4;
  private static final int REQUEST_FACE_CONFIRM_REQUEST = 5;

  private GraphicOverlay mGraphicOverlay;
  private ProgressDialog mProgressDialog;
  private CameraManager mCameraManager;
  private boolean mIsGalleryMode = false;
  private TextView mTopMessaging;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_register);
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.processing));

    CameraSourcePreview preview = findViewById(R.id.preview);
    mGraphicOverlay = findViewById(R.id.faceOverlay);
    ImageButton captureButton = findViewById(R.id.button_capture);
    ImageButton chooseImage = findViewById(R.id.button_choose_image);
    ImageButton toggleCamera = findViewById(R.id.toggle_camera);

    mTopMessaging = findViewById(R.id.top_messaging);

    captureButton.setOnClickListener(this);
    chooseImage.setOnClickListener(this);
    toggleCamera.setOnClickListener(this);


    mCameraManager = new CameraManager(this, mGraphicOverlay, getWindowManager(),
        preview, this);

    if (!FaceAuth.getConfig().registrationCameraSound) {
      // TODO (siddhant): Disable the camera shound here.
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
    if (!mIsGalleryMode) {
      mCameraManager.onResume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCameraManager.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mCameraManager.onDestroy();
  }

  private void captureImage() {
    showProgress();
    mCameraManager.captureImage();
  }

  private void startRegisterConfirmActivity(Uri uri) {
    Intent confirmIntent = new Intent(this, RegisterConfirmActivity.class);
    confirmIntent.setData(uri);
    startActivityForResult(confirmIntent, REQUEST_FACE_CONFIRM_REQUEST);
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
    } else if (view.getId() == R.id.button_choose_image) {
      mIsGalleryMode = true;
      Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
      photoPickerIntent.setType("image/*");
      startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    } else if (view.getId() == R.id.toggle_camera) {
      mCameraManager.toggleCamera();
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent result) {
    switch (requestCode) {
      case REQUEST_PICK_IMAGE:
        if (resultCode == RESULT_OK) {
          startRegisterConfirmActivity(result.getData());
          // onActivityResult gets called before onResume, so don't reset mIsGalleryMode here. That
          // will be rest in RegisterConfirmActivity result
        } else {
          mIsGalleryMode = false;
          //mCameraManager.resetCamera();
        }
        break;
      case REQUEST_FACE_CONFIRM_REQUEST:
        mIsGalleryMode = false;
        if (resultCode == RESULT_OK) {
          setResult(RESULT_OK, result);
          finish();
        }
        break;
      default:
        super.onActivityResult(requestCode, resultCode, result);
        break;
    }
  }

  @Override public void onPictureClick(byte[] bytes) {
    mCameraManager.savePicture(bytes);
  }

  @Override public void onPictureSave(Uri uri) {
    hideProgress();
    startRegisterConfirmActivity(uri);
  }

  @Override
  public void updateFaces(final SparseArray<FaceInfo> mListeners) {
  }
}
