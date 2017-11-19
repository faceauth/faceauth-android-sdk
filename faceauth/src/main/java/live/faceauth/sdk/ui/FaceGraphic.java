package live.faceauth.sdk.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.google.android.gms.vision.face.Face;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private Paint mBoxPaint;
  private volatile Face mFace;
  private int mFaceId;

  FaceGraphic(GraphicOverlay overlay) {
    super(overlay);

    mBoxPaint = new Paint();
    mBoxPaint.setColor(Color.GREEN);
    mBoxPaint.setAlpha(115);
    mBoxPaint.setStyle(Paint.Style.STROKE);
    mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
  }

  void setId(int id) {
    mFaceId = id;
  }

  void updateFace(Face face) {
    mFace = face;
    postInvalidate();
  }

  @Override
  public void draw(Canvas canvas) {
    Face face = mFace;
    if (face == null || !FaceAuth.getConfig().showFaceRectangle) {
      return;
    }

    float x = translateX(face.getPosition().x + face.getWidth() / 2);
    float y = translateY(face.getPosition().y + face.getHeight() / 2);
    float xOffset = scaleX(face.getWidth() / 2.0f);
    float yOffset = scaleY(face.getHeight() / 2.0f);
    float left = x - xOffset;
    float top = y - yOffset;
    float right = x + xOffset;
    float bottom = y + yOffset;
    RectF r = new RectF(left, top, right, bottom);
    canvas.drawRoundRect(r, 8f, 8f, mBoxPaint);
  }
}