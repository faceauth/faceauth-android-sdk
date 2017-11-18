package live.faceauth.sdk.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class PermissionUtil {

  public static List<String> checkMissingPermissions(@NonNull Context context,
      @NonNull String... permissions) {
    List<String> requiredPermissions = new ArrayList<>();

    for (String permission : permissions) {
      if (PermissionChecker.checkSelfPermission(context, permission) == PERMISSION_DENIED) {
        requiredPermissions.add(permission);
      }
    }

    return requiredPermissions;
  }

  public static List<String> checkDeniedPermissions(String[] permissions, int[] results) {
    List<String> deniedPermissions = new ArrayList<>();

    for (int i = 0; i < permissions.length; i++) {
      if (results[i] == PERMISSION_DENIED) {
        deniedPermissions.add(permissions[i]);
      }
    }

    return deniedPermissions;
  }
}
