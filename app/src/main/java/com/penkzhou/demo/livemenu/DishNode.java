package com.penkzhou.demo.livemenu;

import android.view.MotionEvent;
import android.widget.TextView;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class DishNode extends TransformableNode implements Node.OnTapListener{
    private DishModel dishInfo;
    private ArFragment context;
    private Node infoCard;
    private Renderable modelRenderable;
    private boolean isCreate = true;



    public DishNode(DishModel dishInfo, ArFragment context, Renderable modelRenderable) {
        super(context.getTransformationSystem());
        this.dishInfo = dishInfo;
        this.context = context;
        this.modelRenderable = modelRenderable;
        setOnTapListener(this);
    }

    @Override
    public void onActivate() {
        if (getScene() == null) {
            return;
        }
        if (infoCard == null) {
            infoCard = new Node();
            infoCard.setParent(this);
            infoCard.setEnabled(false);
            infoCard.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));
            ViewRenderable.builder()
                    .setView(context.getContext(), R.layout.dish_info_card)
                    .build()
                    .thenAccept( (renderable) -> {
                        infoCard.setRenderable(renderable);
                        TextView dishName =  renderable.getView().findViewById(R.id.dish_name);
                        TextView dishDesc =  renderable.getView().findViewById(R.id.dish_desc);
                        TextView dishPrice =  renderable.getView().findViewById(R.id.dish_price);
                        dishName.setText(dishInfo.getName());
                        dishDesc.setText(dishInfo.getDesc());
                        dishPrice.setText(String.format("¥%.1f",dishInfo.getPrice()));
                    });
        }
        if (isCreate) {
            setRenderable(modelRenderable);
            isCreate = false;
        }

    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        if (infoCard == null) {
            return;
        }

        if (getScene() == null) {
            return;
        }
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = infoCard.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        infoCard.setWorldRotation(lookRotation);

    }

    @Override
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (infoCard == null) {
            return;
        }
        infoCard.setEnabled(!infoCard.isEnabled());
    }
}
