package live.faceauth.sdk2.network;

import java.util.HashMap;

public class Request {
  public String method;
  public String url;
  public HashMap<String, String> headers;
  public byte[] postData;

  public Request(String method, String url, byte[] postData) {
    this.method = method;
    this.url = url;
    this.postData = postData;
    this.headers = new HashMap<>();
  }
}