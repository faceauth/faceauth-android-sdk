package live.faceauth.sdk.network;

import live.faceauth.sdk.FaceAuth;
import live.faceauth.sdk.models.RegisterResponse;
import live.faceauth.sdk.models.VerifyResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHelper {

  private static final String BASE_URL = "https://faceauth.live/api/";

  private static FaceAuthService service() {

    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
          @Override
          public Response intercept(Chain chain) throws IOException {
            Request.Builder request = chain.request().newBuilder();
            request
                .addHeader("x-faceauth-api-key", FaceAuth.getInstance().getApiKey())
                .addHeader("Content-Type", "image/png");

            return chain.proceed(request.build());
          }
        })
        .build();

    final Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build();

    return retrofit.create(FaceAuthService.class);
  }

  public static void register(InputStream is, Callback<RegisterResponse> callback) {
    service()
        .register(getResponseBody(is))
        .enqueue(callback);
  }

  public static void verify(UUID registeredFaceId, InputStream is,
      Callback<VerifyResponse> callback) {
    service()
        .verify(registeredFaceId, getResponseBody(is))
        .enqueue(callback);
  }

  private static RequestBody getResponseBody(InputStream is) {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final byte[] bytes = new byte[1024];
    int bytesRead;

    try {
      while ((bytesRead = is.read(bytes)) > 0) {
        byteArrayOutputStream.write(bytes, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return RequestBody
        .create(MediaType.parse("image/png"), byteArrayOutputStream.toByteArray());
  }
}
