
// Author: Pierce Brooks

package com.piercelbrooks.facialoid;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.piercelbrooks.common.BasicFragment;
import com.piercelbrooks.mlkit.common.CameraSource;
import com.piercelbrooks.mlkit.common.CameraSourcePreview;
import com.piercelbrooks.mlkit.facedetection.FaceContourCenter;
import com.piercelbrooks.mlkit.facedetection.FaceContourCenterGraphic;
import com.piercelbrooks.mlkit.common.FrameMetadata;
import com.piercelbrooks.mlkit.common.GraphicOverlay;
import com.piercelbrooks.mlkit.facedetection.FaceContourDetectorProcessor;
import com.piercelbrooks.mlkit.facedetection.FaceContourDetectorProcessorListener;
import com.tyorikan.voicerecordingvisualizer.RecordingSampler;
import com.tyorikan.voicerecordingvisualizer.VisualizerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends BasicFragment<MayoralFamily> implements RecordingSampler.CalculateVolumeListener,
        FaceContourDetectorProcessorListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "OID-MainFrag";

    private static final String FACE_CONTOUR = "Face Contour";

    private static final int PERMISSION_REQUESTS = 1;

    private static final float CENTER_RADIUS = 10.0f;

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel;
    private String[] contourTypes;
    private RecordingSampler recordingSampler;
    private VisualizerView visualizer;

    public MainFragment()
    {
        super();
        selectedModel = FACE_CONTOUR;
        cameraSource = null;
        preview = null;
        graphicOverlay = null;
        contourTypes = null;
        recordingSampler = null;
        visualizer = null;
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

        preview = view.findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = view.findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = view.findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        options.add(FACE_CONTOUR);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>((MainActivity)getMunicipality(), R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = view.findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }

        visualizer = view.findViewById(R.id.visualizer);

        recordingSampler = new RecordingSampler();
        recordingSampler.setVolumeListener(this);  // for custom implements
        recordingSampler.setSamplingInterval(100); // voice sampling interval
        recordingSampler.link(visualizer);     // link to visualizer
    }

    @Override
    public void onBirth()
    {
        startCameraSource();

        if ((recordingSampler == null) && (visualizer != null)) {
            recordingSampler = new RecordingSampler();
            recordingSampler.setVolumeListener(this);  // for custom implements
            recordingSampler.setSamplingInterval(100); // voice sampling interval
            recordingSampler.link(visualizer);     // link to visualizer
        }
    }

    @Override
    public void onDeath()
    {
        if (preview != null) {
            preview.stop();
        }

        if (recordingSampler != null) {
            if (recordingSampler.isRecording()) {
                recordingSampler.release();
            }
        }
    }

    @Override
    public MayoralFamily getMayoralFamily()
    {
        return MayoralFamily.MAIN;
    }

    @Override
    public Class<?> getCitizenClass()
    {
        return MainFragment.class;
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        if (preview != null) {
            preview.stop();
        }
        startCameraSource();
        if (recordingSampler != null) {
            recordingSampler.startRecording();
        }
    }

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource((MainActivity)getMunicipality(), graphicOverlay);
        }

        try {
            switch (model) {
                case FACE_CONTOUR:
                    Log.i(TAG, "Using Face Contour Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceContourDetectorProcessor(this));
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    ((MainActivity)getMunicipality()).getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
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
            createCameraSource(selectedModel);
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
    public void onFrame(@Nullable Bitmap originalCameraImage,
                        @NonNull List<FirebaseVisionFace> faces,
                        @NonNull FrameMetadata frameMetadata,
                        @NonNull GraphicOverlay graphicOverlay) {
        ArrayList<FaceContourCenter> centers = new ArrayList<FaceContourCenter>();
        graphicOverlay.setTranslationX((((float)((ViewGroup)graphicOverlay.getParent()).getWidth())*0.5f)-(0.5f*((float)graphicOverlay.getWidth())));
        if (contourTypes == null) {
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
}
