package com.nidoham.streamly.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;
import android.graphics.Color;

public class LibraryFragment extends BaseFragment {
    
    private MaterialTextView textView;
    
    public static LibraryFragment newInstance() {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                            @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        textView = new MaterialTextView(requireContext());
        textView.setText("Library Fragment");
        textView.setTextSize(24);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(android.view.Gravity.CENTER);
        return textView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (isViewValid()) {
            loadLibraryContent();
        }
    }
    
    private void loadLibraryContent() {
        if (textView != null && isViewValid()) {
            textView.setText("Library Fragment - Loaded");
        }
    }
    
    @Override
    protected void onCleanupResources() {
        textView = null;
    }
}