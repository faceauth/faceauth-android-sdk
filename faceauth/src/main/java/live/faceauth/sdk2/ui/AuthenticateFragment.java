package live.faceauth.sdk2.ui;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.List;
import live.faceauth.sdk.R;
import live.faceauth.sdk.ui.camera.CameraSourcePreview;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import live.faceauth.sdk.util.CameraManager;
import live.faceauth.sdk.util.PermissionUtil;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AuthenticateFragment extends Fragment {
  private FrameLayout rootView;
  private CameraManager mCameraManager;

  public AuthenticateFragment() {}

  public static AuthenticateFragment newInstance() {
    return new AuthenticateFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    rootView = (FrameLayout) inflater.inflate(R.layout.activity_authenticate, container, false);

    GraphicOverlay mGraphicOverlay = rootView.findViewById(R.id.faceOverlay);
    CameraSourcePreview preview = rootView.findViewById(R.id.preview);

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
