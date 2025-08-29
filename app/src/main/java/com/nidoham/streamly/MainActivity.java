package com.nidoham.streamly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;
import com.nidoham.streamly.fragments.adapter.FragmentContainerAdapter;
import com.nidoham.streamly.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.os.Handler;
import android.os.Looper;
import com.nidoham.streamly.system.SystemControl;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FragmentContainerAdapter fragmentAdapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private Handler mainHandler;
    
    // State management
    private int currentPosition = 0;
    private boolean isNavigationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        
        mainHandler = new Handler(Looper.getMainLooper());
        SystemControl.applySeedColorTheme(this);
        
        // Restore state if available
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("current_position", 0);
        }
        
        initializeViews();
        setupFragmentAdapter();
        setupBottomNavigation();
        
        // Set initial position after all setup is complete
        mainHandler.post(() -> {
            if (viewPager != null && !isFinishing()) {
                viewPager.setCurrentItem(currentPosition, false);
                bottomNavigationView.setSelectedItemId(
                    FragmentContainerAdapter.getMenuItemForPosition(currentPosition)
                );
            }
        });
    }

    /**
     * Initialize view components and create ViewPager2 with proper configuration
     */
    private void initializeViews() {
        bottomNavigationView = binding.navigation;
        
        // Create ViewPager2 with optimized settings
        viewPager = new ViewPager2(this);
        viewPager.setId(View.generateViewId());
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setSaveEnabled(true);
        
        // Configure layout parameters for proper positioning
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.MATCH_PARENT,
            CoordinatorLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setBehavior(new com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior());
        viewPager.setLayoutParams(layoutParams);
        
        // Replace FragmentContainerView with ViewPager2 in layout hierarchy
        CoordinatorLayout rootLayout = (CoordinatorLayout) binding.getRoot();
        int contentIndex = rootLayout.indexOfChild(binding.content);
        rootLayout.removeView(binding.content);
        rootLayout.addView(viewPager, contentIndex);
    }

    /**
     * Configure fragment adapter with memory optimization settings
     */
    private void setupFragmentAdapter() {
        if (fragmentAdapter != null) {
            fragmentAdapter.clearCache();
        }
        
        fragmentAdapter = new FragmentContainerAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
        
        // Disable user input to control navigation exclusively through bottom nav
        viewPager.setUserInputEnabled(false);
        
        // Configure page change callback with null checks and state management
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isNavigationInProgress = (state != ViewPager2.SCROLL_STATE_IDLE);
            }
            
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (!isFinishing() && !isDestroyed()) {
                    currentPosition = position;
                    int menuItemId = FragmentContainerAdapter.getMenuItemForPosition(position);
                    
                    // Update bottom navigation selection on main thread
                    mainHandler.post(() -> {
                        if (bottomNavigationView != null && !isNavigationInProgress) {
                            bottomNavigationView.setSelectedItemId(menuItemId);
                        }
                    });
                }
            }
        };
        
        viewPager.registerOnPageChangeCallback(pageChangeCallback);
    }

    /**
     * Configure bottom navigation with proper state management and error handling
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isFinishing() || isDestroyed() || isNavigationInProgress) {
                return false;
            }
            
            int position = FragmentContainerAdapter.getPositionForMenuItem(item.getItemId());
            
            // Only navigate if position is different from current
            if (position != currentPosition && viewPager != null) {
                isNavigationInProgress = true;
                currentPosition = position;
                
                // Use handler to ensure UI thread execution
                mainHandler.post(() -> {
                    if (viewPager != null && !isFinishing()) {
                        viewPager.setCurrentItem(position, false);
                        isNavigationInProgress = false;
                    }
                });
            }
            
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", currentPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear any pending navigation operations
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Comprehensive cleanup to prevent memory leaks
        if (pageChangeCallback != null && viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            pageChangeCallback = null;
        }
        
        if (fragmentAdapter != null) {
            fragmentAdapter.clearCache();
            fragmentAdapter = null;
        }
        
        if (viewPager != null) {
            viewPager.setAdapter(null);
            viewPager = null;
        }
        
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler = null;
        }
        
        bottomNavigationView = null;
        
        if (binding != null) {
            binding = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Clear fragment cache during low memory conditions
        if (fragmentAdapter != null) {
            fragmentAdapter.clearCache();
        }
        System.gc(); // Suggest garbage collection
    }
}