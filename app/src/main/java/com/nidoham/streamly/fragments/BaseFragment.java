package com.nidoham.streamly.fragments;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.lang.ref.WeakReference;

public abstract class BaseFragment extends Fragment {
    
    private WeakReference<View> viewRef;
    private boolean isViewCreated = false;
    
    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewRef = new WeakReference<>(view);
        isViewCreated = true;
    }
    
    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
        isViewCreated = false;
        onCleanupResources();
    }
    
    protected boolean isViewValid() {
        return isViewCreated && viewRef != null && viewRef.get() != null && !isDetached();
    }
    
    protected View getViewSafe() {
        return isViewValid() ? viewRef.get() : null;
    }
    
    /**
     * Override this method to cleanup resources specific to each fragment
     */
    protected abstract void onCleanupResources();
}