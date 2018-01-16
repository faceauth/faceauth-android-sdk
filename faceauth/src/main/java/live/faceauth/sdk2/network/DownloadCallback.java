package live.faceauth.sdk2.network;

public abstract class DownloadCallback<T> {
  void onComplete(T result) {}
}