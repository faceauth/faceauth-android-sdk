package live.faceauth.sdk2.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;
import live.faceauth.sdk.R;
import live.faceauth.sdk.ui.camera.CameraSourcePreview;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import live.faceauth.sdk.util.CameraManager;
import live.faceauth.sdk.util.PermissionUtil;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RegisterFragment extends Fragment {
  private FrameLayout rootView;
  private CameraManager mCameraManager;

  public RegisterFragment() {}

  public static RegisterFragment newInstance() {
    return new RegisterFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    rootView = (FrameLayout) inflater.inflate(R.layout.activity_register, container, false);

    CameraSourcePreview preview = rootView.findViewById(R.id.preview);
    GraphicOverlay mGraphicOverlay = rootView.findViewById(R.id.faceOverlay);
    ImageButton captureButton = rootView.findViewById(R.id.button_capture);
    ImageButton chooseImage = rootView.findViewById(R.id.button_choose_image);
    ImageButton toggleCamera = rootView.findViewById(R.id.toggle_camera);
    TextView mTopMessaging = rootView.findViewById(R.id.top_messaging);

    mCameraManager = new CameraManager(
        getActivity(),
        mGraphicOverlay,
        getActivity().getWindowManager(),
        preview,
        new CameraManager.SimplePictureClickListener() {
          @Override public void onPictureClick(byte[] bytes) {
            mCameraManager.savePicture(bytes);
          }

          @Override public void onPictureSave(Uri uri) {
            super.onPictureSave(uri);
          }
        });


    List<String> permissions = PermissionUtil.checkMissingPermissions(getActivity(), CAMERA,
        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
    if (permissions.isEmpty()) {
      mCameraManager.createCameraSource();
    }

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    mCameraManager.onResume();
  }
}
