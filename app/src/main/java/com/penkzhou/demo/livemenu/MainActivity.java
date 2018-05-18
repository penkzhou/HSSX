package com.penkzhou.demo.livemenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.wx.wheelview.adapter.BaseWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;
    private WheelView<DishModel> wheelView;
    private BaseWheelAdapter<DishModel> adapter;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
        fragment.getArSceneView().getScene().setOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        initializeGallery();
        initWheel();
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = fragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open in Photos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private void initializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,1);

        TextView andy = new TextView(this);
        andy.setText("andy");
        andy.setTextSize(24);
        andy.setLayoutParams(layoutParams);
        andy.setOnClickListener(view ->{addObject(Uri.parse("Bowl_of_Rice_01.sfb"));});
        gallery.addView(andy);

        TextView cabin = new TextView(this);
        cabin.setText("cabin");
        cabin.setTextSize(24);
        cabin.setLayoutParams(layoutParams);
        cabin.setOnClickListener(view ->addObject(Uri.parse("Glass_Of_Wine_01.sfb")));
        gallery.addView(cabin);

        TextView house = new TextView(this);
        house.setText("house");
        house.setTextSize(24);
        house.setLayoutParams(layoutParams);
        house.setOnClickListener(view ->{addObject(Uri.parse("ARK_COFFEE_CUP.sfb"));});
        gallery.addView(house);

        TextView igloo = new TextView(this);
        igloo.setText("igloo");
        igloo.setTextSize(24);
        igloo.setLayoutParams(layoutParams);
        igloo.setOnClickListener(view ->{addObject(Uri.parse("cokecola.sfb"));});
        gallery.addView(igloo);
    }

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if ((trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
                    placeObject(fragment, hit.createAnchor(), model);
                    break;

                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        DishInfo dishInfo = new DishInfo();
        dishInfo.setDesc("此菜只在本店有,多吃具有养生补气之疗效，实乃居家旅行必备之良品，多吃可保身体健康。");
        dishInfo.setName("浏阳农家小炒肉");
        dishInfo.setPrice(45.0f);
        DishNode dishNode = new DishNode(dishInfo, fragment, renderable);
        dishNode.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        dishNode.select();
    }

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }
        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        Point point = getScreenCenter();
        List<HitResult> hitResultList;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hitResultList = frame.hitTest(point.x, point.y);
            for (HitResult hit : hitResultList) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private void initWheel() {
        wheelView = findViewById(R.id.view_wheel);
        initAdapter();
        wheelView.setWheelAdapter(adapter); // 文本数据源
        wheelView.setSkin(WheelView.Skin.None); // common皮肤
        wheelView.setWheelData(getData());  // 数据集合
        wheelView.setWheelSize(7);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.selectedTextZoom = 1.5f;
        style.backgroundColor = getColor(R.color.black);
        style.textColor = getColor(R.color.white);
        style.selectedTextColor = getColor(R.color.red);
        wheelView.setStyle(style);
        wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener<DishModel>() {
            @Override
            public void onItemClick(int position, DishModel dishModel) {
                Log.e("aaaaa", dishModel.toString() + "---" + position);
            }
        });
    }

    void initAdapter() {
        adapter = new BaseWheelAdapter<DishModel>() {
            @Override
            protected View bindView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish, parent, false);
                }
                TextView name = ViewHolder.get(convertView, R.id.tv_dish_name);
                TextView price = ViewHolder.get(convertView, R.id.tv_price);
                TextView count = ViewHolder.get(convertView, R.id.tv_count);
                DishModel model = getItem(position);
                name.setText(model.getName());
                price.setText(String.valueOf(model.getPrice()));
                if (model.getChooseCount() == 0) {
                    count.setVisibility(View.GONE);
                } else {
                    count.setVisibility(View.VISIBLE);
                    count.setText(String.valueOf(model.getChooseCount()));
                }
                return convertView;
            }
        };
    }

    private List<DishModel> getData() {
        List<DishModel> s = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DishModel dishModel = new DishModel();
            dishModel.setName("小炒肉" + i);
            dishModel.setPrice(i + 20);
            dishModel.setChooseCount(i == 5 ? 0 : i);
            s.add(dishModel);
        }
        return s;
    }
}
