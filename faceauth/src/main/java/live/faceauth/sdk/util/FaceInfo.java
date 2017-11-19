package live.faceauth.sdk.util;

import android.support.annotation.StringRes;
import android.util.SparseArray;
import com.google.android.gms.vision.face.Face;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.R;

public class FaceInfo {

  private static final long TRACKING_TIME_THRESHOLD_MS = 1 * 1000;
  private static final float SMILING_PROB_THRESHOLD = 0.2f;
  private static final float HAS_SMILED = 1.0F - SMILING_PROB_THRESHOLD;
  private static final float HAS_FROWNED = 0.0F + SMILING_PROB_THRESHOLD;

  private static final String LOG_TAG = "FaceInfo";

  public final int faceId;
  private long mTrackingTimeStart;

  private float mMaxSmilingProbability = 0.0f;
  private float mMinSmilingProbability = 1.0f;

  private float mMaxLeftEyeOpenProbability = 0.0f;
  private float mMinLeftEyeOpenProbability = 1.0f;

  private float mMaxRightEyeOpenProbability = 0.0f;
  private float mMinRightEyeOpenProbability = 1.0f;

  private boolean mResetOnCheck = false;

  private long mTrackingTimeEnd;

  FaceInfo(int faceId) {
    this.faceId = faceId;
    mTrackingTimeStart = System.currentTimeMillis();
  }

  public void updateFace(Face face) {
    float smileProb = face.getIsSmilingProbability();
    float leftEyeOpenProb = face.getIsLeftEyeOpenProbability();
    float rightEyeOpenProb = face.getIsRightEyeOpenProbability();

    if (smileProb > 0) {
      mMinSmilingProbability = Math.min(mMinSmilingProbability, smileProb);
      mMaxSmilingProbability = Math.max(mMaxSmilingProbability, smileProb);
    }

    if (leftEyeOpenProb > 0) {
      mMinLeftEyeOpenProbability = Math.min(mMinLeftEyeOpenProbability, leftEyeOpenProb);
      mMaxLeftEyeOpenProbability = Math.max(mMaxLeftEyeOpenProbability, leftEyeOpenProb);
    }

    if (rightEyeOpenProb > 0) {
      mMinRightEyeOpenProbability = Math.min(mMinRightEyeOpenProbability, rightEyeOpenProb);
      mMaxRightEyeOpenProbability = Math.max(mMaxRightEyeOpenProbability, rightEyeOpenProb);
    }

    mTrackingTimeEnd = System.currentTimeMillis();
  }

  private boolean isStableFace() {
    return (mTrackingTimeEnd - mTrackingTimeStart) >= TRACKING_TIME_THRESHOLD_MS;
  }

  public void setResetOnCheck() {
    mResetOnCheck = true;
  }

  public boolean getResetOnCheck() {
    return mResetOnCheck;
  }

  public void reset() {
    mResetOnCheck = false;
    
    mTrackingTimeStart = System.currentTimeMillis();
    mTrackingTimeEnd = System.currentTimeMillis();

    mMaxSmilingProbability = 0.0f;
    mMinSmilingProbability = 1.0f;
  }

  private float getSmilingProbDiff() {
    return mMaxSmilingProbability - mMinSmilingProbability;
  }

  private float getLeftEyeOpenProbDiff() {
    return mMaxLeftEyeOpenProbability - mMinLeftEyeOpenProbability;
  }

  private float getRightEyeOpenProbDiff() {
    return mMaxRightEyeOpenProbability - mMinRightEyeOpenProbability;
  }

  private boolean isLiveFace() {
    // TODO: add more heuristics based on other landmarks

    if (FaceAuth.getConfig().enableLivenessDetection) {
      return getSmilingProbDiff() > SMILING_PROB_THRESHOLD;
    } else {
      return true;
    }
  }

  public boolean shouldCaptureImage() {
    return isLiveFace() && isStableFace();
  }

  @StringRes
  public static int getHintMessageId(SparseArray<FaceInfo> faceInfoArray, boolean processing) {
    final int messageResId;

    if (faceInfoArray.size() == 1 && !processing) {
      FaceInfo faceInfo = faceInfoArray.valueAt(0);

      messageResId = faceInfo.getHintMessageId();
    } else if (faceInfoArray.size() == 0) {
      messageResId = R.string.tracker_no_face;
    } else if (faceInfoArray.size() > 1) {
      messageResId = R.string.tracker_multiple_faces;
    } else {
      messageResId = R.string.tracker_processing_verification;
    }

    return messageResId;
  }

  @StringRes
  private int getHintMessageId() {
    final int messageResId;
    if (isLiveFace()) {
      messageResId = R.string.tracker_processing_verification;
    } else if (mMaxSmilingProbability > HAS_SMILED) {
      messageResId = R.string.tracker_dont_smile;
    } else if (mMinSmilingProbability < HAS_FROWNED) {
      messageResId = R.string.tracker_smile;
    } else {
      messageResId = R.string.tracker_change_expressions;
    }

    return messageResId;
  }

  void log() {
    long duration = mTrackingTimeEnd - mTrackingTimeStart;
    float smilingDiff = getSmilingProbDiff();
    float leftEyeDiff = getLeftEyeOpenProbDiff();
    float rightEyeDiff = getRightEyeOpenProbDiff();

    android.util.Log.d(LOG_TAG, "Tracked between: " + mTrackingTimeStart + "," + mTrackingTimeEnd);
    android.util.Log.d(LOG_TAG, "Tracked duration: " + duration);

    android.util.Log.d(LOG_TAG, "Smiling prob: " + mMinSmilingProbability + "," + mMaxSmilingProbability);
    android.util.Log.d(LOG_TAG, "Smiling diff: " + smilingDiff);

    android.util.Log.d(LOG_TAG, "Left eye open prob: " + mMinLeftEyeOpenProbability + "," + mMaxLeftEyeOpenProbability);
    android.util.Log.d(LOG_TAG, "Left eye open diff: " + leftEyeDiff);

    android.util.Log.d(LOG_TAG, "Right eye open prob: " + mMinRightEyeOpenProbability + "," + mMaxRightEyeOpenProbability);
    android.util.Log.d(LOG_TAG, "Right eye open diff: " + rightEyeDiff);
  }
}
