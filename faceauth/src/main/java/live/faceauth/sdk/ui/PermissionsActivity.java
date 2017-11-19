package live.faceauth.sdk.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.Arrays;
import live.faceauth.sdk.R;
import live.faceauth.sdk.util.PermissionUtil;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

  private static final int RC_HANDLE_PERMS = 2;
  private static final String TAG = "PermissionsActivity";
  private static final int REQUEST_NEXT_ACTIVITY = 4;

  private View mPermissionRationaleView;
  private Intent mIntent;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_permissions);

    mPermissionRationaleView = findViewById(R.id.permission_rationale);

    findViewById(R.id.permission_retry_button).setOnClickListener(this);

    mIntent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
    // Check for the camera permission before accessing the camera.  If the
    // permission is not granted yet, request permission.
    List<String> permissions = PermissionUtil.checkMissingPermissions(this, CAMERA,
        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
    if (permissions.isEmpty()) {
      startNextActivity();
    } else {
      requestPermissions(permissions.toArray(new String[0]));
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {

    if (requestCode == RC_HANDLE_PERMS) {
      List<String> deniedPermissions =
          PermissionUtil.checkDeniedPermissions(permissions, grantResults);
      if (deniedPermissions.isEmpty()) {
        Log.d(TAG, "Camera permission granted - initialize the camera source");
        // we have permission, so start next activity
        startNextActivity();
      } else {
        // TODO (siddhant): show settings button if user clicked "never show again"
        showPermissionRationale(deniedPermissions.toArray(new String[0]));
      }
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
      final Intent result) {
    switch (requestCode) {
      case REQUEST_NEXT_ACTIVITY:
        setResult(resultCode, result);
        finish();
        break;
      default:
        super.onActivityResult(requestCode, resultCode, result);
        break;
    }
  }

  private void startNextActivity() {
    if (mIntent != null) {
      startActivityForResult(mIntent, REQUEST_NEXT_ACTIVITY);
    } else {
      Log.w(TAG, "No next intent passed");
      finish();
    }
  }

  private void requestPermissions(final String[] requestPermissions) {
    Log.w(TAG, "Permissions not granted. Requesting permission: " + Arrays.toString(
        requestPermissions));

    ActivityCompat.requestPermissions(this, requestPermissions, RC_HANDLE_PERMS);
  }

  private void showPermissionRationale(final String[] requestPermissions) {
    mPermissionRationaleView.setVisibility(View.VISIBLE);
  }

  private void hidePermissionRationale() {
    mPermissionRationaleView.setVisibility(View.GONE);
  }

  @Override public void onClick(View v) {
    if (v.getId() == R.id.permission_retry_button) {
      List<String> permissions = PermissionUtil.checkMissingPermissions(this, CAMERA,
          WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
      if (!permissions.isEmpty()) {
        hidePermissionRationale();
        requestPermissions(permissions.toArray(new String[0]));
      }
    }
  }
}
