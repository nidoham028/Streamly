package com.nidoham.streamly.fragments.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.nidoham.streamly.fragments.HomeFragment;
import com.nidoham.streamly.fragments.CommunityFragment;
import com.nidoham.streamly.fragments.SubscriptionFragment;
import com.nidoham.streamly.fragments.LibraryFragment;
import com.nidoham.streamly.R;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Memory-safe FragmentContainerAdapter with proper fragment lifecycle management
 * and memory leak prevention mechanisms for bottom navigation fragments.
 */
public class FragmentContainerAdapter extends FragmentStateAdapter {
    
    private static final int FRAGMENT_COUNT = 4;
    private final Map<Integer, WeakReference<Fragment>> fragmentCache = new HashMap<>();
    
    // Fragment position constants
    public static final int HOME_POSITION = 0;
    public static final int COMMUNITY_POSITION = 1;
    public static final int SUBSCRIPTION_POSITION = 2;
    public static final int LIBRARY_POSITION = 3;
    
    public FragmentContainerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Check cache first to prevent unnecessary fragment recreation
        WeakReference<Fragment> cachedFragmentRef = fragmentCache.get(position);
        if (cachedFragmentRef != null) {
            Fragment cachedFragment = cachedFragmentRef.get();
            if (cachedFragment != null && !cachedFragment.isDetached()) {
                return cachedFragment;
            }
        }

        Fragment fragment = createFragmentInstance(position);
        fragmentCache.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    /**
     * Creates the appropriate fragment instance based on position
     * @param position The fragment position
     * @return New fragment instance
     */
    private Fragment createFragmentInstance(int position) {
        switch (position) {
            case HOME_POSITION:
                return HomeFragment.newInstance();
            case COMMUNITY_POSITION:
                return CommunityFragment.newInstance();
            case SUBSCRIPTION_POSITION:
                return SubscriptionFragment.newInstance();
            case LIBRARY_POSITION:
                return LibraryFragment.newInstance();
            default:
                return HomeFragment.newInstance();
        }
    }
    
    @Override
    public int getItemCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public long getItemId(int position) {
        // Provide stable IDs for fragment lifecycle management
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < FRAGMENT_COUNT;
    }
    
    /**
     * Maps bottom navigation menu item IDs to fragment positions
     * @param menuItemId The menu item ID from bottom navigation
     * @return The corresponding fragment position
     */
    public static int getPositionForMenuItem(int menuItemId) {
        if (menuItemId == R.id.nav_home) {
            return HOME_POSITION;
        } else if (menuItemId == R.id.nav_community) {
            return COMMUNITY_POSITION;
        } else if (menuItemId == R.id.nav_subscription) {
            return SUBSCRIPTION_POSITION;
        } else if (menuItemId == R.id.nav_library) {
            return LIBRARY_POSITION;
        } else {
            return HOME_POSITION;
        }
    }
    
    /**
     * Maps fragment positions to menu item IDs
     * @param position The fragment position
     * @return The corresponding menu item ID
     */
    public static int getMenuItemForPosition(int position) {
        switch (position) {
            case HOME_POSITION:
                return R.id.nav_home;
            case COMMUNITY_POSITION:
                return R.id.nav_community;
            case SUBSCRIPTION_POSITION:
                return R.id.nav_subscription;
            case LIBRARY_POSITION:
                return R.id.nav_library;
            default:
                return R.id.nav_home;
        }
    }

    /**
     * Clears fragment cache to prevent memory leaks
     * Should be called when the adapter is no longer needed
     */
    public void clearCache() {
        fragmentCache.clear();
    }
}