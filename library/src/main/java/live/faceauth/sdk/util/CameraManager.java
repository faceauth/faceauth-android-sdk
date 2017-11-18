package live.faceauth.sdk.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import live.faceauth.sdk.ui.GraphicFaceTracker;
import live.faceauth.sdk.ui.camera.CameraSource;
import live.faceauth.sdk.ui.camera.CameraSourcePreview;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraManager {

  private static final int RC_HANDLE_GMS = 9001;
  private static final String TAG = "CameraManager";
  private static final String TAG2 = "FACETRACKER";

  private GraphicOverlay mGraphicOverlay;
  private Context mAppContext;
  private WindowManager mWindowManager;
  private CameraSource mCameraSource;
  private CameraSourcePreview mPreview;
  private PictureClickListener mListener;
  private Activity mActivity;
  SparseArray<FaceInfo> mFaceInfo = new SparseArray<>();
  private int mCameraSourceId = CameraSource.CAMERA_FACING_FRONT;

  public CameraManager(Activity activity, GraphicOverlay graphicOverlay,
      WindowManager windowManager,
      CameraSourcePreview cameraSourcePreview,
      PictureClickListener listener) {
    mActivity = activity;
    mAppContext = activity.getApplicationContext();
    mListener = listener;
    mGraphicOverlay = graphicOverlay;
    mWindowManager = windowManager;
    mPreview = cameraSourcePreview;
  }

  /**
   * Creates and starts the camera.  Note that this uses a higher resolution in comparison
   * to other detection examples to enable the barcode detector to detect small barcodes
   * at long distances.
   */
  public void createCameraSource() {
    FaceDetector detector = new FaceDetector.Builder(mAppContext)
        .setClassificationType(FaceDetector.ALL_LANDMARKS)
        .build();

    detector.setProcessor(
        new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
            .build());

    if (!detector.isOperational()) {
      Log.w(TAG, "Face detector dependencies are not yet available.");
      Toast.makeText(
          mAppContext,
          "Something went wrong! Please restart the app and try again.",
          Toast.LENGTH_LONG
      ).show();
    }

    DisplayMetrics displayMetrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    int height = displayMetrics.heightPixels;
    int width = displayMetrics.widthPixels;

    mCameraSource = new CameraSource.Builder(mAppContext, detector)
        .setRequestedPreviewSize(height, width)
        .setFacing(mCameraSourceId)
        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        .setRequestedFps(30.0f)
        .build();
  }

  //==============================================================================================
  // Camera Source Preview
  //==============================================================================================

  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    // check that the device has play services available.
    int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mAppContext);
    if (code != ConnectionResult.SUCCESS) {
      Dialog dlg =
          GoogleApiAvailability.getInstance().getErrorDialog(mActivity, code, RC_HANDLE_GMS);
      dlg.show();
    }

    if (mCameraSource != null) {
      try {
        mPreview.start(mCameraSource, mGraphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }

  public void onResume() {
    startCameraSource();
  }

  public void onPause() {
    stopPreview();
  }

  public void stopPreview() {
    mPreview.stop();
  }

  public void onDestroy() {
    if (mCameraSource != null) {
      mCameraSource.release();
    }
  }

  public void captureImage() {
    mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
      @Override public void onPictureTaken(byte[] bytes) {
        mListener.onPictureClick(bytes);
        Log.d(TAG, "clicked");
      }
    });
  }

  public void toggleCamera() {
    if (mCameraSourceId == CameraSource.CAMERA_FACING_FRONT) {
      mCameraSourceId = CameraSource.CAMERA_FACING_BACK;
    } else {
      mCameraSourceId = CameraSource.CAMERA_FACING_FRONT;
    }
    stopPreview();
    createCameraSource();
    startCameraSource();
  }

  public void savePicture(byte[] data) {
    File pictureFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE);
    if (pictureFile == null) {
      Log.d(TAG, "Error creating media file, check storage permissions");
      return;
    }

    try {
      FileOutputStream fos = new FileOutputStream(pictureFile);
      fos.write(data);
      fos.close();
      stopPreview();
      Uri uri = Uri.fromFile(pictureFile);
      mListener.onPictureSave(uri);
    } catch (FileNotFoundException e) {
      Log.d(TAG, "File not found: " + e.getMessage());
    } catch (IOException e) {
      Log.d(TAG, "Error accessing file: " + e.getMessage());
    }
  }

  public void resetCamera() {
    startCameraSource();
  }

  //==============================================================================================
  // Graphic Face Tracker
  //==============================================================================================

  /**
   * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
   * uses this factory to create face trackers as needed -- one for each individual.
   */
  private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

    List<GraphicFaceTracker.FaceTrackerListener> mTrackerListeners = new ArrayList<>();

    @Override
    public Tracker<Face> create(Face face) {
      Log.i(TAG2, "tracker created: " + face.getId());

      GraphicFaceTracker.FaceTrackerListener listener =
          new GraphicFaceTracker.FaceTrackerListener() {

            @Override public void onNewItem(int baseFaceId, int faceId) {
              Log.i(TAG2, "tracker onNewItem: " + faceId);
              mFaceInfo.put(faceId, new FaceInfo(faceId));
              mListener.updateFaces(mFaceInfo);
            }

            @Override public void onDone(int faceId) {
              Log.i(TAG2, "tracker done: " + faceId);
              FaceInfo faceInfo = mFaceInfo.get(faceId);
              if (faceInfo != null) {
                faceInfo.log();
              }
              mFaceInfo.remove(faceId);
              mListener.updateFaces(mFaceInfo);
              mTrackerListeners.remove(this);
            }

            @Override public void onMissing(int faceId) {
              Log.i(TAG2, "tracker onMissing: " + faceId);
              mListener.updateFaces(mFaceInfo);
            }

            @Override public void onUpdate(Face face, int faceId) {
              mFaceInfo.get(faceId).updateFace(face);
              mListener.updateFaces(mFaceInfo);
            }
          };

      mTrackerListeners.add(listener);

      return new GraphicFaceTracker(mGraphicOverlay, listener, face.getId());
    }
  }

  public interface PictureClickListener {
    void onPictureClick(byte[] bytes);

    void onPictureSave(Uri uri);

    void updateFaces(SparseArray<FaceInfo> mListeners);
  }
}
