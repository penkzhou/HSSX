package com.penkzhou.demo.livemenu;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class ChooseDishAdapter extends RecyclerView.Adapter<ChooseDishAdapter.ChooseDishViewHolder> {

    private List<DishModel> chooseList;

    public ChooseDishAdapter(List<DishModel> chooseList) {
        this.chooseList = chooseList;
    }

    @NonNull
    @Override
    public ChooseDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_dish_item, parent, false);
        return new ChooseDishViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseDishViewHolder holder, int position) {
        DishModel dishModel = chooseList.get(position);
        if (dishModel != null) {
            holder.bind(dishModel);
        }
    }

    @Override
    public int getItemCount() {
        return chooseList == null ? 0 : chooseList.size();
    }

    static class ChooseDishViewHolder extends RecyclerView.ViewHolder {

        public ChooseDishViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(DishModel dishModel) {
            if (itemView instanceof TextView) {
                TextView textView = (TextView) itemView;
                textView.setText(String.format("%s ¥ %.0f x %s", dishModel.getName(), dishModel.getPrice(), dishModel.getChooseCount()));
            }

        }
    }
}
