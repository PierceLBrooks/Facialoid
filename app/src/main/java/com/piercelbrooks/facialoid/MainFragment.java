
// Author: Pierce Brooks

package com.piercelbrooks.facialoid;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.CameraXPreviewHelperListener;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.framework.TextureFrame;
import com.google.mediapipe.glutil.EglManager;
import com.piercelbrooks.common.BasicFragment;
import com.piercelbrooks.common.Family;
import com.piercelbrooks.common.Governor;
import com.piercelbrooks.common.Utilities;
import com.piercelbrooks.mlkit.facedetection.FaceContourCenter;
import com.piercelbrooks.mlkit.facedetection.FaceContourCenterGraphic;
import com.piercelbrooks.mlkit.common.FrameMetadata;
import com.piercelbrooks.mlkit.common.GraphicOverlay;
import com.piercelbrooks.mlkit.facedetection.FaceContourDetectorProcessor;
import com.piercelbrooks.mlkit.facedetection.FaceContourDetectorProcessorListener;
import com.tyorikan.voicerecordingvisualizer.RecordingSampler;
import com.tyorikan.voicerecordingvisualizer.VisualizerView;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends BasicFragment<MayoralFamily> implements
        RecordingSampler.CalculateVolumeListener,
        FaceContourDetectorProcessorListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        TextureFrameConsumer,
        CameraXPreviewHelperListener
{
    private static final String TAG = "OID-MainFrag";

    private static final String FACE_CONTOUR = "Face Contour";

    private static final String BINARY_GRAPH_NAME = "multihandtrackinggpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_hand_landmarks";
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;

    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
    // corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    private static final int PERMISSION_REQUESTS = 1;

    private static final float CENTER_RADIUS = 10.0f;

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;

    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;

    private RecordingSampler recordingSampler;
    private VisualizerView visualizer;

    private View main;

    private Paint paint;

    private FaceContourDetectorProcessor frames;

    private GraphicOverlay overlay;

    private String[] contourTypes;

    public MainFragment()
    {
        super();
        recordingSampler = null;
        visualizer = null;
        paint = new Paint();
        paint.setColor(ContextCompat.getColor((MainActivity)Governor.getInstance().getCitizen(Family.MUNICIPALITY), android.R.color.white));
    }

    @Override
    public @LayoutRes int getLayout()
    {
        return R.layout.main_fragment;
    }

    @Override
    public void createView(@NonNull View view)
    {
        String[] types = getResources().getStringArray(R.array.contours);
        contourTypes = Arrays.copyOf(types, types.length);

        main = view;

        previewDisplayView = new SurfaceView(getActivity());
        setupPreviewDisplayView();

        processor = new FrameProcessor(
                        getActivity(),
                        getManager().getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);

        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.d(TAG, "Received multi-hand landmarks packet.");
                    List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, LandmarkProto.NormalizedLandmarkList.parser());
                    /*
                    Log.d(
                            TAG,
                            "[TS:"
                                    + packet.getTimestamp()
                                    + "] "
                                    + getMultiHandLandmarksDebugString(multiHandLandmarks));
                                    */

                    if (overlay != null) {
                        overlay.clear();
                    }
                    try {
                        Canvas canvas = previewDisplayView.getHolder().lockCanvas();
                        //canvas.drawColor(Color.BLACK);
                        /*Matrix matrix = new Matrix();
                        matrix.postScale(((float)canvas.getWidth())/((float)originalCameraImage.getWidth()), ((float)canvas.getHeight())/((float)originalCameraImage.getHeight()));
                        canvas.drawBitmap(originalCameraImage, matrix, paint);*/
                        overlay.onDraw(canvas);
                        float width = canvas.getWidth();
                        float height = canvas.getHeight();
                        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
                            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                                canvas.drawCircle(landmark.getX() * width, landmark.getY() * height, 5.0f, paint);
                            }
                        }
                        previewDisplayView.getHolder().unlockCanvasAndPost(canvas);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });

        frames = new FaceContourDetectorProcessor(this);

        converter = new ExternalTextureConverter(getManager().getContext());
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        converter.addConsumer(this);
        /*if (PermissionHelper.cameraPermissionsGranted((MainActivity)Governor.getInstance().getCitizen(Family.MUNICIPALITY))) {
            startCamera();
        }*/

        visualizer = view.findViewById(R.id.visualizer);

        recordingSampler = new RecordingSampler();
        recordingSampler.setVolumeListener(this);  // for custom implements
        recordingSampler.setSamplingInterval(100); // voice sampling interval
        recordingSampler.link(visualizer);     // link to visualizer

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        } else {
            startCamera();
        }

        PermissionHelper.checkAndRequestCameraPermissions(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        converter.close();
    }

    @Override
    public void onBirth() {
        if ((recordingSampler == null) && (visualizer != null)) {
            recordingSampler = new RecordingSampler();
            recordingSampler.setVolumeListener(this);  // for custom implements
            recordingSampler.setSamplingInterval(100); // voice sampling interval
            recordingSampler.link(visualizer);     // link to visualizer
        }
    }

    @Override
    public void onDeath() {
        if (recordingSampler != null) {
            if (recordingSampler.isRecording()) {
                recordingSampler.release();
            }
        }
    }

    @Override
    public MayoralFamily getMayoralFamily() {
        return MayoralFamily.MAIN;
    }

    @Override
    public Class<?> getCitizenClass() {
        return MainFragment.class;
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = main.findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                //processor.getVideoSurfaceOutput().setSurface(previewDisplayView.getHolder().getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                // (Re-)Compute the ideal size of the camera-preview display (the area that the
                                // camera-preview frames get rendered onto, potentially with scaling and rotation)
                                // based on the size of the SurfaceView that contains the display.
                                Size viewSize = new Size(width, height);
                                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                                boolean isCameraRotated = cameraHelper.isCameraRotated();

                                // Connect the converter to the camera-preview frames as its input (via
                                // previewFrameTexture), and configure the output width and height as the computed
                                // display size.
                                converter.setSurfaceTextureAndAttachToGLContext(
                                        previewFrameTexture,
                                        isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                                        isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                //processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    private void startCamera() {
        cameraHelper = new CameraXPreviewHelper(this);
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    Log.i(TAG, "startCamera");
                    previewFrameTexture = surfaceTexture;
                    // Make the display view visible to start showing the preview. This triggers the
                    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
                    previewDisplayView.setVisibility(View.VISIBLE);
                });
        try {
            cameraHelper.startCamera((MainActivity) Governor.getInstance().getCitizen(Family.MUNICIPALITY), CAMERA_FACING, /*surfaceTexture=*/ null);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getMultiHandLandmarksDebugString(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr +=
                    "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                multiHandLandmarksStr +=
                        "\t\tLandmark ["
                                + landmarkIndex
                                + "]: ("
                                + landmark.getX()
                                + ", "
                                + landmark.getY()
                                + ", "
                                + landmark.getZ()
                                + ")\n";
                ++landmarkIndex;
            }
            ++handIndex;
        }
        return multiHandLandmarksStr;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    ((MainActivity)getMunicipality()).getPackageManager()
                            .getPackageInfo(((MainActivity)getMunicipality()).getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(((MainActivity)getMunicipality()), permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(((MainActivity)getMunicipality()), permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    ((MainActivity)getMunicipality()), allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            startCamera();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    public void onFrame(Bitmap originalCameraImage,
                        List<FirebaseVisionFace> faces,
                        FrameMetadata frameMetadata,
                        GraphicOverlay graphicOverlay) {
        //Log.i(TAG, Utilities.getIdentifier(originalCameraImage));
        ArrayList<FaceContourCenter> centers = new ArrayList<FaceContourCenter>();
        //graphicOverlay.setTranslationX((((float)((ViewGroup)graphicOverlay.getParent()).getWidth())*0.5f)-(0.5f*((float)graphicOverlay.getWidth())));
        if ((contourTypes == null) || (overlay == null)) {
            return;
        }
        for (int i = 0; i < contourTypes.length; ++i) {
            centers.add(new FaceContourCenter(Integer.parseInt(contourTypes[i])));
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            List<FirebaseVisionFaceContour> contours = getContours(face, contourTypes);
            for (int j = 0; j < contours.size(); ++j) {
                FirebaseVisionFaceContour contour = contours.get(j);
                List<FirebaseVisionPoint> points = contour.getPoints();
                for (int k = 0; k < points.size(); ++k) {
                    FirebaseVisionPoint point = points.get(k);
                    centers.get(j).point.x += point.getX().intValue();
                    centers.get(j).point.y += point.getY().intValue();
                    ++centers.get(j).points;
                }
            }
        }
        for (int i = 0; i < centers.size(); ++i) {
            FaceContourCenter center = centers.get(i);
            int color = FaceContourCenterGraphic.getColor(center.type);
            if (color == Color.BLACK) {
                continue;
            }
            if (center.points == 0) {
                continue;
            }
            center.point.x /= center.points;
            center.point.y /= center.points;
            graphicOverlay.add(new FaceContourCenterGraphic(graphicOverlay, (float)center.point.x, (float)center.point.y, CENTER_RADIUS, color));
        }
    }

    private List<FirebaseVisionFaceContour> getContours(FirebaseVisionFace face, String[] types) {
        ArrayList<FirebaseVisionFaceContour> contours = new ArrayList<FirebaseVisionFaceContour>();
        for (int i = 0; i < types.length; ++i) {
            contours.add(face.getContour(Integer.parseInt(types[i])));
        }
        return contours;
    }

    @Override
    public void onCalculateVolume(int volume) {
        //Log.d(TAG, String.valueOf(volume));
    }

    private EglManager getManager() {
        return ((MainActivity) Governor.getInstance().getCitizen(Family.MUNICIPALITY)).getManager();
    }

    @Override
    public void onNewFrame(TextureFrame frame) {
        if (overlay == null) {
            overlay = new GraphicOverlay(frame.getWidth(), frame.getHeight());
        }
    }

    @Override
    public void onNewBitmap(ByteBuffer bitmap, int width, int height) {
        frames.process(
                bitmap,
                new FrameMetadata.Builder()
                        .setWidth(width)
                        .setHeight(height)
                        .setRotation(cameraHelper.isCameraRotated()?1:0)
                        .setCameraFacing((CAMERA_FACING.equals(CameraHelper.CameraFacing.FRONT)?1:0))
                        .build(),
                overlay);
    }

    @Override
    public void onAnalyze(FirebaseVisionImage image, int rotation, int width, int height) {
        frames.detectInVisionImage(
                null,
                image,
                new FrameMetadata.Builder()
                        .setWidth(width)
                        .setHeight(height)
                        .setRotation(rotation)
                        .setCameraFacing((CAMERA_FACING.equals(CameraHelper.CameraFacing.FRONT)?1:0))
                        .build(),
                overlay);
    }
}
