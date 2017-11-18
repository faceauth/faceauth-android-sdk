package live.faceauth.sdk.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.FaceDetector;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.R;
import live.faceauth.sdk.models.Face;
import live.faceauth.sdk.models.RegisterResponse;
import live.faceauth.sdk.network.ApiHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import live.faceauth.sdk.util.ImageUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterConfirmActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = "RegisterConfirm";
  FaceView mFaceView;
  FaceDetector mFaceDetector;
  SparseArray<com.google.android.gms.vision.face.Face> mFaces;
  Bitmap mBitmap;
  ImageButton mCancelButton;
  ImageButton mConfirmButton;
  private ProgressDialog mProgressDialog;
  Uri mImageData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register_confirm);

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.processing));

    mCancelButton = findViewById(R.id.button_cancel);
    mConfirmButton = findViewById(R.id.button_confirm);

    mCancelButton.setOnClickListener(this);
    mConfirmButton.setOnClickListener(this);

    mFaceView = findViewById(R.id.faceView);
    mFaceDetector = new FaceDetector.Builder(this).setTrackingEnabled(false)
        .setLandmarkType(FaceDetector.FAST_MODE)
        .build();

    Intent intent = getIntent();
    mImageData = intent.getData();
    showSelectedPhoto(mImageData);
  }

  private void showSelectedPhoto(Uri imageUri) {
    try {
      InputStream stream = getContentResolver().openInputStream(imageUri);

      mBitmap = ImageUtil.getScaledBitmap(stream, 360);

      final Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
      mFaces = mFaceDetector.detect(frame);
      mFaceView.setContent(mBitmap, mFaces);

      Log.d("Faces detected: ", "" + mFaces.size());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override public void onClick(View view) {
    int id = view.getId();

    if (id == R.id.button_confirm) {
      processConfirm();
    } else if (id == R.id.button_cancel) {
      finish();
    }
  }

  private void processConfirm() {
    if (mFaces.size() > 0) {
      mProgressDialog.show();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
      InputStream bs = new ByteArrayInputStream(bos.toByteArray());
      register(bs);
      Log.i(TAG, "processing...");
    } else {
      Toast.makeText(
          RegisterConfirmActivity.this,
          R.string.register_failed_no_face,
          Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void register(InputStream stream) {
    ApiHelper.register(stream, new Callback<RegisterResponse>(){
      @Override
      public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
        mProgressDialog.hide();
        if (response.isSuccessful() && response.body().success && response.body().face.detected) {
          returnResult(response.body().face);
          Log.d("detect", "success.");
        } else {
          Toast.makeText(
              RegisterConfirmActivity.this,
              R.string.register_failed_no_face,
              Toast.LENGTH_LONG).show();
          finish();
          Log.d("detect", "unsuccessful: " + response.message());
        }
      }

      @Override public void onFailure(Call<RegisterResponse> call, Throwable t) {
        Log.d("detect", "failure", t);
        mProgressDialog.hide();
      }
    });
  }

  private void returnResult(@NonNull final Face face) {
    Intent data = new Intent();
    data.putExtra(FaceAuth.REGISTERED_FACE_ID, face.faceId.toString());
    data.setData(mImageData);
    setResult(RESULT_OK, data);
    finish();
  }
}
