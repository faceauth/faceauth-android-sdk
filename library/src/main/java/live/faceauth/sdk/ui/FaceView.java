package live.faceauth.sdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;

/**
 * View which displays a bitmap containing a face along with overlay graphics that identify the
 * locations of detected facial landmarks.
 */
public class FaceView extends View {
  private Bitmap mBitmap;
  private SparseArray<Face> mFaces;
  private double shiftTop = 0;

  public FaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  void setContent(Bitmap bitmap, SparseArray<Face> faces) {
    mBitmap = bitmap;
    mFaces = faces;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if ((mBitmap != null) && (mFaces != null)) {
      double scale = drawBitmap(canvas);
      drawFaceAnnotations(canvas, scale);
    }
  }

  private double drawBitmap(Canvas canvas) {
    double viewWidth = canvas.getWidth();
    double viewHeight = canvas.getHeight();
    double imageWidth = mBitmap.getWidth();
    double imageHeight = mBitmap.getHeight();
    //double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
    double scale = viewWidth / imageWidth;
    shiftTop = (viewHeight - (imageHeight * scale)) / 2;

    Rect destBounds = new Rect(0, (int) shiftTop, (int)(imageWidth * scale), (int)(imageHeight * scale + shiftTop));
    canvas.drawBitmap(mBitmap, null, destBounds, null);
    return scale;
  }

  private void drawFaceAnnotations(Canvas canvas, double scale) {
    Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setAlpha(115);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(5);

    for (int i = 0; i < mFaces.size(); ++i) {
      Face face = mFaces.valueAt(i);
      double x = (face.getPosition().x + face.getWidth() / 2) * scale;
      double y = (face.getPosition().y + face.getHeight() / 2) * scale;
      double xOffset = (face.getWidth() / 2.0f) * scale;
      double yOffset = (face.getHeight() / 2.0f) * scale;

      double left = x - xOffset;
      double top = y - yOffset;
      double right = x + xOffset;
      double bottom = y + yOffset;

      RectF r = new RectF((float) left, (float) (top + shiftTop), (float) right, (float) (bottom + shiftTop));
      canvas.drawRoundRect(r, 8f, 8f, paint);
    }
  }
}