package live.faceauth.sdk2.network;

import android.net.NetworkInfo;

public interface DownloadCallback<T> {
  interface Progress {
    int ERROR = -1;
    int CONNECT_SUCCESS = 0;
    int GET_INPUT_STREAM_SUCCESS = 1;
    int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
    int PROCESS_INPUT_STREAM_SUCCESS = 3;
  }

  void onComplete(T result);

  NetworkInfo getActiveNetworkInfo();

  void onProgressUpdate(int progressCode, int percentComplete);

  void finishDownloading();
}