package com.penkzhou.demo.livemenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.wx.wheelview.adapter.BaseWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;
    private WheelView<DishModel> wheelView;
    private BaseWheelAdapter<DishModel> adapter;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;
    public final List<DishModel> chooseList = new ArrayList<>();
    private FlowingDrawer drawer;
    private ImageView ivClose, ivChooseClose, saveImage, camera, delete;
    private TextView ivOpen, dishChooseButton, dishSubmitButton, dishChoosePrice, dishChooseNumber;
    private View chooseDishArea, chooseDishSubmit;
    private RecyclerView chooseListView;
    private ChooseDishAdapter chooseDishAdapter;
    private String currentPhoto;

    private AnchorNode currentChooseNode;

    private DishNode currentDishNode;

    private boolean isTakingPhoto = false;

    private List<String> modelList = Arrays.asList(new String[]{"cokecola.sfb", "model.sfb", "pizza.sfb", "Shishkebab_251.sfb"});
    private List<String> nameList = Arrays.asList(new String[]{"饮料", "日本寿司", "至尊披萨", "新疆羊肉串"});
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initArTexture();
    }


    //自定义纹理，去掉默认的圆点
    private void initArTexture() {
        Texture.Sampler sampler =
                Texture.Sampler.builder()
                        .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                        .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                        .build();

        Texture.builder()
                .setSource(this, R.drawable.dot)
                .setSampler(sampler)
                .build()
                .thenAccept(texture -> {
                    fragment.getArSceneView()
                            .getPlaneRenderer()
                            .getMaterial()
                            .thenAccept(material -> {
                                material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
                            });
                });
    }

    private void initViews() {
        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        camera = findViewById(R.id.dish_camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
        fragment.getArSceneView().getScene().setOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        initWheel();
        ivChooseClose = findViewById(R.id.choose_close);
        chooseListView = findViewById(R.id.choose_list);
        chooseDishArea = findViewById(R.id.dish_choose_list);
        dishChooseButton = findViewById(R.id.dish_choose_button);
        dishChooseButton.setOnClickListener(v -> {
            drawer.closeMenu(true);
            toggleChooseDishArea();
        });
        ivChooseClose.setOnClickListener(v -> {
            toggleChooseDishArea();
        });
        chooseDishAdapter = new ChooseDishAdapter(chooseList);
        chooseListView.setLayoutManager(new LinearLayoutManager(this));
        chooseListView.setAdapter(chooseDishAdapter);
        chooseDishSubmit = findViewById(R.id.dish_submit);
        dishChoosePrice = findViewById(R.id.dish_choose_price);
        dishSubmitButton = findViewById(R.id.dish_choose_submit_button);
        dishSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EndActivity.class);
                startActivity(intent);
            }
        });
        saveImage = findViewById(R.id.save_image);
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(currentPhoto)) {
                    return;
                }
                File photoFile = new File(currentPhoto);

                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                        photoFile);
                Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                intent.setDataAndType(photoURI, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });
        dishChooseNumber = findViewById(R.id.dish_choose_number);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentChooseNode != null && currentDishNode != null) {
                    DishModel dishModel = currentDishNode.getDishInfo();
                    int chooseIndex = chooseList.indexOf(dishModel);
                    if (chooseIndex > -1) {
                        if (dishModel.getChooseCount() > 1) {
                            int newCount = dishModel.getChooseCount() - 1;
                            dishModel.setChooseCount(newCount);
                        } else {
                            dishModel.setChooseCount(0);
                            chooseList.remove(dishModel);
                        }
                        refreshUIWithChooseList();
                    }
                    fragment.getArSceneView().getScene().removeChild(currentChooseNode);
                    currentDishNode = null;
                    currentChooseNode = null;
                }
            }
        });
        refreshUIWithChooseList();
    }

    private void toggleChooseDishArea() {
        if (chooseDishArea.getVisibility() == View.VISIBLE) {
            chooseDishArea.setVisibility(View.GONE);
            chooseDishArea.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_choose_dish_area_out));
        } else {
            chooseDishArea.setVisibility(View.VISIBLE);
            chooseDishArea.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_choose_dish_area_in));
        }
    }

    private void takePhoto() {
        if (isTakingPhoto) {
            Toast.makeText(MainActivity.this, "正在保存中", Toast.LENGTH_LONG);
            return;
        }
        isTakingPhoto = true;
        final String filename = Utils.generateFilename();
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
                currentPhoto = filename;
                try {
                    Utils.saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    isTakingPhoto = false;
                    return;
                }
                File photoFile = new File(filename);

                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                        photoFile);
                saveImage.post(new Runnable() {
                    @Override
                    public void run() {
                        saveImage.setImageURI(photoURI);
                    }
                });
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
            isTakingPhoto = false;
        }, new Handler(handlerThread.getLooper()));
    }


    private void addObject(DishModel model) {
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

    private void placeObject(ArFragment fragment, Anchor anchor, DishModel model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), Uri.parse(model.getModelPath()))
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable, model))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable, DishModel model) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        DishNode dishNode = new DishNode(model, fragment, renderable);
        dishNode.setParent(anchorNode);
        dishNode.setOnChooseListener(new DishNode.OnChooseListener() {
            @Override
            public void onChoose() {
                currentChooseNode = anchorNode;
                currentDishNode = dishNode;
                delete.setVisibility(View.VISIBLE);
            }
        });
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
        if (currentChooseNode == null && currentDishNode == null) {
            delete.setVisibility(View.GONE);
        } else {
            delete.setVisibility(View.VISIBLE);
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
        drawer = findViewById(R.id.drawerlayout);
        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeMenu(true);
            }
        });
        ivOpen = findViewById(R.id.iv_open);
        ivOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openMenu(true);
                if (chooseDishArea.getVisibility() == View.VISIBLE) {
                    toggleChooseDishArea();
                }
            }
        });
        drawer.post(new Runnable() {
            @Override
            public void run() {
                drawer.openMenu(true);
            }
        });

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
        style.selectedTextColor = getColor(R.color.yellow);
        wheelView.setStyle(style);
        wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener<DishModel>() {
            @Override
            public void onItemClick(int position, DishModel dishModel) {
                if (isHitting) {
                    addObject(dishModel);
                    int index = chooseList.indexOf(dishModel);
                    if (index >= 0) {
                        DishModel d = chooseList.get(index);
                        d.setChooseCount(d.getChooseCount() + 1);
                    } else {
                        dishModel.setChooseCount(dishModel.getChooseCount() + 1);
                        chooseList.add(dishModel);
                    }
                    refreshUIWithChooseList();
                } else {
                    Toast.makeText(MainActivity.this, "正在努力识别餐桌中...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void refreshUIWithChooseList() {
        adapter.notifyDataSetChanged();
        chooseDishAdapter.notifyDataSetChanged();
        if (chooseList != null && chooseList.size() > 0) {
            dishChooseButton.setTextColor(Color.parseColor("#FF9900"));
            dishChooseButton.setEnabled(true);
            dishSubmitButton.setEnabled(true);
            chooseDishSubmit.setBackgroundColor(Color.parseColor("#FF9900"));
            dishChoosePrice.setText(String.format("¥ %.0f", getPriceFromChooseList()));
            dishChooseNumber.setText(getNumberFromChooseList() + "");
            dishChooseNumber.setVisibility(View.VISIBLE);
        } else {
            dishChooseButton.setTextColor(Color.parseColor("#FFFFFF"));
            chooseDishSubmit.setBackgroundColor(Color.parseColor("#333333"));
            dishChooseButton.setEnabled(false);
            dishSubmitButton.setEnabled(false);
            dishChoosePrice.setText("¥ 0");
            dishChooseNumber.setVisibility(View.GONE);
        }
    }

    private float getPriceFromChooseList() {
        if (chooseList == null || chooseList.size() == 0) {
            return 0.0f;
        }
        float result = 0.0f;
        for (DishModel dishModel : chooseList) {
            result += (dishModel.getPrice() * dishModel.getChooseCount());
        }
        return result;
    }

    private int getNumberFromChooseList() {
        if (chooseList == null || chooseList.size() == 0) {
            return 0;
        }
        int result = 0;
        for (DishModel dishModel : chooseList) {
            result += dishModel.getChooseCount();
        }
        return result;
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
                price.setText(String.format("¥ %s", String.valueOf(model.getPrice())));
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
            dishModel.setName(nameList.get(i%4));
            dishModel.setModelPath(modelList.get(i%4));
            dishModel.setDesc("此菜只在本店有,多吃具有养生补气之疗效，实乃居家旅行必备之良品。");
            dishModel.setPrice(i + 20);
            s.add(dishModel);
        }
        return s;
    }
}
