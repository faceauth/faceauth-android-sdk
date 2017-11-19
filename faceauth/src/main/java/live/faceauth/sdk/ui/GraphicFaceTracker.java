package live.faceauth.sdk.ui;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import live.faceauth.sdk.ui.camera.GraphicOverlay;
import java.lang.ref.WeakReference;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class GraphicFaceTracker extends Tracker<Face> {
  private GraphicOverlay mOverlay;
  private FaceGraphic mFaceGraphic;
  private int mFaceId;

  private WeakReference<FaceTrackerListener> mFaceTrackerListenerRef;

  public GraphicFaceTracker(GraphicOverlay overlay, FaceTrackerListener listener, int faceId) {
    mOverlay = overlay;
    mFaceGraphic = new FaceGraphic(overlay);
    mFaceTrackerListenerRef = new WeakReference<>(listener);
  }

  /**
   * Start tracking the detected face instance within the face overlay.
   */
  @Override
  public void onNewItem(int faceId, Face item) {
    mFaceGraphic.setId(faceId);
    mFaceId = faceId;
    if (mFaceTrackerListenerRef != null && mFaceTrackerListenerRef.get() != null){
      mFaceTrackerListenerRef.get().onNewItem(mFaceId, faceId);
    }
  }

  /**
   * Update the position/characteristics of the face within the overlay.
   */
  @Override
  public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
    mOverlay.add(mFaceGraphic);
    mFaceGraphic.updateFace(face);
    if (mFaceTrackerListenerRef != null && mFaceTrackerListenerRef.get() != null){
      mFaceTrackerListenerRef.get().onUpdate(face, mFaceId);
    }
  }

  /**
   * Hide the graphic when the corresponding face was not detected.  This can happen for
   * intermediate frames temporarily (e.g., if the face was momentarily blocked from
   * view).
   */
  @Override
  public void onMissing(FaceDetector.Detections<Face> detectionResults) {
    mOverlay.remove(mFaceGraphic);
    if (mFaceTrackerListenerRef != null && mFaceTrackerListenerRef.get() != null){
      mFaceTrackerListenerRef.get().onMissing(mFaceId);
    }
  }

  /**
   * Called when the face is assumed to be gone for good. Remove the graphic annotation from
   * the overlay.
   */
  @Override
  public void onDone() {
    mOverlay.remove(mFaceGraphic);
    if (mFaceTrackerListenerRef != null && mFaceTrackerListenerRef.get() != null){
      mFaceTrackerListenerRef.get().onDone(mFaceId);
    }
  }


  public interface FaceTrackerListener {

    void onNewItem(int baseFaceId, int faceId);

    void onDone(int faceId);

    void onMissing(int faceId);

    void onUpdate(Face face, int faceId);
  }
}
