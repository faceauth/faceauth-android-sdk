package live.faceauth.sdk2.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

public class NetworkFragment extends Fragment {
  private DownloadCallback<Result> mCallback;
  private DownloadTask mDownloadTask;

  public static NetworkFragment getInstance(FragmentManager fragmentManager, String tag) {
    NetworkFragment networkFragment = (NetworkFragment) fragmentManager
        .findFragmentByTag(tag);
    if (networkFragment == null) {
      networkFragment = new NetworkFragment();
      fragmentManager.beginTransaction().add(networkFragment, tag).commit();
    }
    return networkFragment;
  }

  public void setCallback(DownloadCallback<Result> callback) {
    mCallback = callback;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallback = null;
  }

  @Override
  public void onDestroy() {
    cancelRequest();
    super.onDestroy();
  }

  public void execute(Request request) {
    cancelRequest();
    mDownloadTask = new DownloadTask();
    mDownloadTask.execute(request);
  }

  public void cancelRequest() {
    if (mDownloadTask != null) {
      mDownloadTask.cancel(true);
      mDownloadTask = null;
    }
  }

  public static class Result {
    public String mResultValue;
    public Exception mException;
    public Result(String resultValue) {
      mResultValue = resultValue;
    }
    public Result(Exception exception) {
      mException = exception;
    }
  }

  private class DownloadTask extends AsyncTask<Request, Integer, NetworkFragment.Result> {

    @Override
    protected void onPreExecute() {
      if (mCallback != null) {
        NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
            (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
          mCallback.onComplete(new Result(new IOException("No internet connection.")));
          cancel(true);
        }
      }
    }

    @Override
    protected Result doInBackground(Request... requests) {
      try {
        return performRequest(requests[0]);
      }  catch(Exception e) {
        return new Result(e);
      }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      if (values.length >= 2) {
        mCallback.onProgressUpdate(values[0], values[1]);
      }
    }

    @Override
    protected void onPostExecute(Result result) {
      if (result != null && mCallback != null) {
        if (result.mException != null) {
          mCallback.onComplete(result);
        } else if (result.mResultValue != null) {
          mCallback.onComplete(result);
        }
        mCallback.finishDownloading();
      }
    }

    private Result performRequest(Request request) throws IOException {
      InputStream stream = null;
      HttpsURLConnection connection = null;
      String result = null;
      try {
        URL url = new URL(request.url);
        String method = request.method;

        connection = (HttpsURLConnection) url.openConnection();
        connection.setReadTimeout(3000);
        connection.setConnectTimeout(3000);
        connection.setRequestMethod(method);
        connection.setDoInput(true);

        if (request.postData != null) {
          connection.setDoOutput(true);
          DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
          byte[] postData       = request.postData;
          wr.write( postData );
        }

        if (request.headers != null) {
          for (HashMap.Entry<String, String> pair : request.headers.entrySet()) {
            connection.setRequestProperty(pair.getKey(), pair.getValue());
          }
        }

        connection.connect();
        publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpsURLConnection.HTTP_OK) {
          throw new IOException("HTTP error code: " + responseCode);
        }
        stream = connection.getInputStream();
        publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
        if (stream != null) {
          result = readStream(stream, 500);
          publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS, 0);
        }
      } finally {
        if (stream != null) {
            stream.close();
        }
        if (connection != null) {
          connection.disconnect();
        }
      }
      return new Result(result);
    }

    private String readStream(InputStream stream, int maxLength) throws IOException {
      String result = null;
      InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
      char[] buffer = new char[maxLength];
      int numChars = 0;
      int readSize = 0;
      while (numChars < maxLength && readSize != -1) {
        numChars += readSize;
        int pct = (100 * numChars) / maxLength;
        publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, pct);
        readSize = reader.read(buffer, numChars, buffer.length - numChars);
      }
      if (numChars != -1) {
        numChars = Math.min(numChars, maxLength);
        result = new String(buffer, 0, numChars);
      }
      return result;
    }
  }
}