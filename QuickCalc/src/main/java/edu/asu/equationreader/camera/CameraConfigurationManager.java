package edu.asu.equationreader.camera;


import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public final class CameraConfigurationManager {

  private static final String TAG = "CameraConfiguration";
  // This is bigger than the size of a small screen, which is still supported. The routine
  // below will still select the default (presumably 320x240) size for these. This prevents
  // accidental selection of very low resolution on some devices.
  private static final int MIN = 470 * 320; // normal screen
  private static final int MAX = 800 * 600; // more than large/HD screen

  private Context context;
  private Point sResolution;
  private Point cResolution;

  public CameraConfigurationManager(Context context) {
    this.context = context;
  }

  /** A safe way to get an instance of the Camera object. */
  public Camera getCameraInstance(String focusMode, int previewFormat) {
    Camera camera = null;
    try {
      camera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance

      fromCamera(camera);
      setDesiredCameraParameters(camera, focusMode, previewFormat);

      // Set camera parameters
      Camera.Parameters params = camera.getParameters();
      params.setFocusMode(focusMode);
      params.setPreviewFormat(previewFormat);

      camera.setParameters(params);
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
      Log.e("TAG",e.toString());
    }
    return camera; // returns null if camera is unavailable
  }


  /**
   * Reads, one time, values from the camera that are needed by the app.
   */
  public void fromCamera(Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    int width = display.getWidth();
    int height = display.getHeight();

    sResolution = new Point(width, height);
    Log.i(TAG, "Screen resolution: " + sResolution);
    cResolution = sizeValue(parameters, sResolution);
    Log.i(TAG, "Camera resolution: " + cResolution);
  }

  private void setDesiredCameraParameters(Camera camera, String focusMode, int format) {
    Camera.Parameters parameters = camera.getParameters();

    if (parameters == null) {
      Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
      return;
    }

    parameters.setFocusMode(focusMode);
    parameters.setPreviewFormat(format);
    parameters.setPreviewSize(cResolution.x, cResolution.y);
    camera.setParameters(parameters);

  }

  public Point getcResolution() {
    return cResolution;
  }

  Point getsResolution() {
    return sResolution;
  }

  private Point sizeValue(Camera.Parameters parameters, Point screenResolution) {

    // Sort by size, descending
    List<Camera.Size> cameraSize = new ArrayList<Camera.Size>(parameters.getSupportedPreviewSizes());
    Collections.sort(cameraSize, new Comparator<Camera.Size>() {
      @Override
      public int compare(Camera.Size a, Camera.Size b) {
        int aPixels = a.height * a.width;
        int bPixels = b.height * b.width;
        if (bPixels < aPixels) {
          return -1;
        }
        if (bPixels > aPixels) {
          return 1;
        }
        return 0;
      }
    });

    if (Log.isLoggable(TAG, Log.INFO)) {
      StringBuilder stringBuilder = new StringBuilder();
      for (Camera.Size supportedPreviewSize : cameraSize) {
        stringBuilder.append(supportedPreviewSize.width).append('x')
        .append(supportedPreviewSize.height).append(' ');
      }
      Log.i(TAG, "Supported preview sizes: " + stringBuilder);
    }

    Point bestSize = null;
    float ratio = (float) screenResolution.x / (float) screenResolution.y;

    float diff = Float.POSITIVE_INFINITY;
    for (Camera.Size supportedPreviewSize : cameraSize) {
      int width = supportedPreviewSize.width;
      int height = supportedPreviewSize.height;
      int pixels = width * height;
      if (pixels < MIN || pixels > MAX) {
        continue;
      }
      boolean isCandidatePortrait = width < height;
      int flippedWidth = isCandidatePortrait ? height : width;
      int flippedHeight = isCandidatePortrait ? width : height;
      if (flippedWidth == screenResolution.x && flippedHeight == screenResolution.y) {
        Point exactPoint = new Point(width, height);
        Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
        return exactPoint;
      }
      float aspectRatio = (float) flippedWidth / (float) flippedHeight;
      float newDiff = Math.abs(aspectRatio - ratio);
      if (newDiff < diff) {
        bestSize = new Point(width, height);
        diff = newDiff;
      }
    }

    if (bestSize == null) {
      Camera.Size defaultSize = parameters.getPreviewSize();
      bestSize = new Point(defaultSize.width, defaultSize.height);
      Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
    }

    Log.i(TAG, "Found best approximate preview size: " + bestSize);
    return bestSize;
  }


}
