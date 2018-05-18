package com.penkzhou.demo.livemenu;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WritingArFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new TextView(getContext());
    }

//    @Override
//    public String[] getAdditionalPermissions() {
//        String[] additionalPermissions = super.getAdditionalPermissions();
//        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
//        String[] permissions = new String[permissionLength + 1];
//        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        if (permissionLength > 0) {
//            System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.length);
//        }
//        return permissions;
//    }
}