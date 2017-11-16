package com.example.ashishkumar.camera2api;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Camera2API extends AppCompatActivity {
    private static final String TAG = "Camera2 API example";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private String cameraId;
    protected CameraDevice cameraDevice;
    public int mwidth,mheight;
    protected int whichCamera = 0;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    public SurfaceTexture surfaceTexture;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    public static int MAX_CAMERA_NUMBER=2;
    private Runnable postSaveCallback;
    private Runnable postCaptureCallback;
    TextView countdownText;
    private static final String FORMAT = "%02d:%02d:%02d:%03d";
    public CountDownTimer timer;
    private boolean isUpdatePreviewDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate started");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera2_api);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        Log.d(TAG, "textureView.setSurfaceTextureListener(textureListener) start ");
        textureView.setSurfaceTextureListener(textureListener);
        Log.d(TAG, "textureView.setSurfaceTextureListener(textureListener) end ");
        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        startBackgroundThread();
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //CameraManager manager1 = openCamera(0);
               /// if (manager1 == null){
               //     Log.d(TAG, "Camera failed to open");
               //     return;
               // }
                //CameraManager manager2 = openCamera(1);
                //ArrayList<CameraManager> allManagers = new ArrayList<CameraManager>();
                Log.d(TAG, "onClick started");
                takePicture();
               // Log.d(TAG, "Finished");
/*
                countdownText = (TextView) findViewById(R.id.countdown);
                 new CountDownTimer(1000000, 1200) { // adjust the milli seconds here
                    public void onTick(long millisUntilFinished) {

                        long hr = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                        long min = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished - TimeUnit.HOURS.toMillis(hr));
                        long sec = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
                        long ms = TimeUnit.MILLISECONDS.toMillis(millisUntilFinished - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));

                        countdownText.setText("Countdown = " + String.format(FORMAT, hr, min, sec, ms));

*/
                        //flag = 1;
/*
                        new CountDownTimer(1000, 500){
                            public void onTick(long mil) {}
                            public void onFinish() {
                                //takePicture();
                            }
                        }.start();
                        //wait for some time

*/
                        //if ( both_camera done)
        /*                if (flag != 2) {
                            takePicture();
                            flag++;
                        } else {
                            //process_image();
                            flag=0;

                        }
                    }

                    public void onFinish() {

                        countdownText.setText("done!");
                    }
                }.start();*/


            }
        });

    }
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int sWidth, int sHeight) {
            //open your camera here
            Log.e(TAG, "Before Open Camera in onSurfaceTextureAvailable");
            opencamera(whichCamera);
            surfaceTexture = surface;
            mwidth = sWidth;
            mheight = sHeight;
            Log.e(TAG, "After Open Camera in onSurfaceTextureAvailable");
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //Log.e(TAG, " in onSurfaceTextureSizeChanged");
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.e(TAG, " in onSurfaceTextureDestroyed");
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Log.e(TAG, " in onSurfaceTextureUpdated");
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            Log.e(TAG, "create camera preview");


            postCaptureCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"inside run of runnable postCaptureCallback ");

                            //createCameraPreview();
                            if(isUpdatePreviewDone==true)
                                takePicture();
                            //isUpdatePreviewDone = true;



                        }
                    };
            createCameraPreview();
            if (isUpdatePreviewDone=true)
            prepare_next();


        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }

    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Toast.makeText(Camera2API.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        Log.d(TAG,"in startBackgroundThread");
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    protected synchronized void runInBackground(final Runnable r) {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.post(r);
        }
    }

    protected synchronized void openNextCamInBackground(final Runnable r) {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.post(r);
        }
    }
    protected void takePicture() {
        Log.e(TAG, "take picture start  ");
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                //width = jpegSizes[0].getWidth();
                //height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(reader.getSurface());

            //captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
            //captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, (int)100);
            // Orientation
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory()+"/DCIM/perception"+whichCamera+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {

                        image = reader.acquireLatestImage();
                        Log.e(TAG, "save image captured done for camera " + whichCamera);
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        Log.e(TAG, "save image captured done for camera " + whichCamera);
                        
                        postSaveCallback =
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG,"inside run of runnable postSaveCallback ");
                                        switch_camera();
                                        isUpdatePreviewDone = true;
                                        //createCameraPreview();
                                        //takePicture();
                                        //isUpdatePreviewDone = true;



                                    }
                                };

                        save_image();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e(TAG, " image captured fail ");
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        Log.e(TAG, " in image saving function for camera " + whichCamera);
                        output.write(bytes);


                        //switch_camera();


                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }

            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    Log.e(TAG, "after Capture Completed  ");
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(Camera2API.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    //switch_camera();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        Log.e(TAG, "capture session configured  ");
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);



                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "Capture session failed  ");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //galleryAddPic(file);

        Log.e(TAG, "take picture end  ");


    }
    protected void createCameraPreview() {
        Log.e(TAG, "in createCameraPreview");
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    Log.e(TAG, "in createCameraPreview onConfigured");
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Camera2API.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void opencamera(int openWhichCamera) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "trying to open camera cam"+whichCamera);
        try {
            cameraId = manager.getCameraIdList()[openWhichCamera];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Camera2API.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                Log.d(TAG, "Requested permissions");
                return;
            }
            Log.e(TAG, "manager.openCamera being called cam" + whichCamera);
            manager.openCamera(cameraId, stateCallback, null);

            Log.e(TAG, "manager.openCamera being call ended cam" + whichCamera);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "opening Camera done");

    }
    protected void updatePreview() {
        Log.e(TAG, "updatePreview start");
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        //captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        //captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_COOL_WHITE_FLUORESCENT);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "updatePreview end");
        //


    }
    private void closeCamera() {
        isUpdatePreviewDone = false;
        if (null != cameraDevice) {
            Log.e(TAG, "closecamera begin cam" + whichCamera);
            cameraDevice.close();
            Log.e(TAG, "closecamera end cam"+whichCamera);
            cameraDevice = null;
        }

        if (null != imageReader) {
            Log.e(TAG, "imagereader close ");
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ///grantResults[0]=REQUEST_CAMERA_PERMISSION;
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(Camera2API.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        //openCamera(whichCamera);
        //textureView.setSurfaceTextureListener(textureListener);
        if (textureView.isAvailable()) {
            Log.e(TAG, "textureView.isAvailable() start camera ");
            textureView.getSurfaceTextureListener().onSurfaceTextureAvailable(surfaceTexture,mwidth,mheight);
            //

        } else {
            Log.e(TAG, "textureView.isAvailable() not available set listener for textureview ");
            textureView.setSurfaceTextureListener(textureListener);

        }

        //textureView.setSurfaceTextureListener(textureListener);
        //openCamera(whichCamera);


    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        textureView.getSurfaceTextureListener().onSurfaceTextureDestroyed(surfaceTexture);
        super.onPause();
    }/*
    private void galleryAddPic(File mCurrentPath) {
        Log.e(TAG, "Path : " + String.valueOf(mCurrentPath));
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(String.valueOf(mCurrentPath));

        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/


    /**
     * Helpful debugging method:  Dump all supported camera formats to log.  You don't need to run
     * this for normal operation, but it's very helpful when porting this code to different
     * hardware.
     */
    public static void dumpFormatInfo(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Log.d(TAG, "No cameras found");
        }
        String id = camIds[0];
        Log.d(TAG, "Using camera id " + id);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            StreamConfigurationMap configs = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            for (int format : configs.getOutputFormats()) {
                Log.d(TAG, "Getting sizes for format: " + format);
                for (Size s : configs.getOutputSizes(format)) {
                    Log.d(TAG, "\t" + s.toString());
                }
            }
            int[] effects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
            for (int effect : effects) {
                Log.d(TAG, "Effect available: " + effect);
            }
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting characteristics.");
        }
    }


    public void switch_camera() {
        isUpdatePreviewDone=false;
        closeCamera();


        whichCamera = whichCamera + 1;
        if (whichCamera > MAX_CAMERA_NUMBER - 1)
            whichCamera = 0;
        if (textureView.isAvailable()) {
            Log.e(TAG, "textureView.isAvailable() start camera ");
            textureView.getSurfaceTextureListener().onSurfaceTextureAvailable(surfaceTexture, mwidth, mheight);
            //

        } else {
            Log.e(TAG, "textureView.isAvailable() not available set listener for textureview ");
            textureView.setSurfaceTextureListener(textureListener);

        }
        //opennextCamInBackground


    }
    protected void  save_image() {

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "in image processing save_image");
                        readyForNextImage();
                    }

                });
    }
    protected void readyForNextImage() {
        if (postSaveCallback != null) {
            Log.d(TAG,"ready for next image call postSaveCallback ");
            postSaveCallback.run();
        }
    }


    protected void  prepare_next() {

        openNextCamInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "in prepare_next run()");
                        readyAfterCaptureSession();
                    }

                });
    }

    protected void readyAfterCaptureSession() {
        if (postCaptureCallback != null) {
            Log.d(TAG,"ready for takepicture call postCaptureCallback ");
            postCaptureCallback.run();
        }
    }
}