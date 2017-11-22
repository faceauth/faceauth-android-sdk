# FaceAuth SDK API Documentation
## Installation
1. Add this line to app’s gradle file `app/build.gradle`:
```
implementation 'live.faceauth.sdk:faceauth:0.4.3'
```
2. Get an API key for FaceAuth SDK at [https://faceauth.live/register](https://faceauth.live/register) and add it as meta-data in AndroidManifest:
```
<meta-data android:name="faceauth-api-key" android:value="<FACE_AUTH_KEY>" />
```
3. Initialize FaceAuth SDK in the MainActivity or MainApplication `onCreate` :
```
FaceAuth.getInstance().initialize(context);
```


## Face Registration

**Implementation**

1. To register a face, call the register method:
```
FaceAuth.getInstance().register(activity);
```
2. Override onActivityResult and call `FaceAuth.getInstance().handleRegistration(...)` to handle registration result:
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  FaceAuth.getInstance().handleRegistration(requestCode, resultCode, data,
      new FaceAuth.RegistrationCallback() {
        @Override public void onSuccess(UUID registeredFaceId, Uri imageUri) {
          // handle successful registration here
          // Store the faceId on your server against the user Id.
        }

        @Override public void onError(Exception e) {
          // handle failed regsitration here
        }
      });

  super.onActivityResult(requestCode, resultCode, data);
}
```

## Face Authentication

**Implementation**

1. To authenticate with a face, call the authenticate method with registeredFaceId received in register step:
    `FaceAuth.getInstance().authenticate(activity, registeredFaceId)`;


2. Also add a method in onActivityResult
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent result) {
  FaceAuth.getInstance().handleAuthentication(requestCode, resultCode, result,
      new FaceAuth.AuthenticationCallback() {
        @Override public void onSuccess(int confidence, double score) {
          // successfully authenticated
          // log in the user
        }

        @Override public void onFailure(int confidence, double score) {
          // failed to authenticate because face didn't match
        }

        @Override public void onError(Exception e) {
          // handle failed authentication here
        }
      });

  super.onActivityResult(requestCode, resultCode, result);
}
```

## Examples

Please see demo app: [FaceAuth Demo](https://github.com/faceauth/faceauth-android-example)
