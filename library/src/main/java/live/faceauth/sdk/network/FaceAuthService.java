package live.faceauth.sdk.network;

import live.faceauth.sdk.models.RegisterResponse;
import live.faceauth.sdk.models.VerifyResponse;
import java.util.UUID;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FaceAuthService {

  @POST("register")
  Call<RegisterResponse> register(@Body RequestBody body);

  @POST("verify")
  Call<VerifyResponse> verify(@Header("x-registered-face-id") UUID registeredFaceId,
      @Body RequestBody body);
}
