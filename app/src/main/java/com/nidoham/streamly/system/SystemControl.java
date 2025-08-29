package com.nidoham.streamly.system;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * SystemControl provides comprehensive status bar and system UI management functionality
 * for consistent appearance across different Android API levels and device configurations.
 */
public class SystemControl {
    
    private static final String TAG = "SystemControl";
    
    /**
     * Status bar appearance modes for enhanced control over system UI theming
     */
    public enum StatusBarMode {
        LIGHT_CONTENT,    // Light status bar with dark content
        DARK_CONTENT,     // Dark status bar with light content
        TRANSPARENT,      // Fully transparent status bar
        TRANSLUCENT       // Semi-transparent status bar overlay
    }
    
    /**
     * Navigation bar styling options for comprehensive system UI coordination
     */
    public enum NavigationBarMode {
        LIGHT,
        DARK,
        TRANSPARENT,
        MATCH_STATUS_BAR
    }

    /**
     * Orientation change management modes for comprehensive device rotation handling
     */
    public enum OrientationMode {
        PORTRAIT_ONLY,
        LANDSCAPE_ONLY,
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        AUTO_ROTATE,
        LOCKED_CURRENT,
        REVERSE_PORTRAIT,
        REVERSE_LANDSCAPE
    }

    /**
     * Configures status bar color with automatic content color adjustment based on background luminance
     * @param activity The target activity for status bar modification
     * @param colorRes Color resource identifier for status bar background
     */
    public static void setStatusBarColor(@NonNull Activity activity, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(activity, colorRes);
        setStatusBarColorValue(activity, color);
    }

    /**
     * Configures status bar color with explicit color value and intelligent content theming
     * @param activity The target activity for status bar modification
     * @param color Direct color value for status bar background
     */
    private static void setStatusBarColorValue(@NonNull Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            
            // Automatically determine appropriate content color based on background luminance
            boolean isLightBackground = isColorLight(color);
            setStatusBarContentColor(activity, isLightBackground ? StatusBarMode.DARK_CONTENT : StatusBarMode.LIGHT_CONTENT);
        }
    }

    /**
     * Applies the seed color theme to status bar with automatic content adjustment
     * This method specifically configures the status bar using R.color.seed for consistent app branding
     * @param activity The target activity for seed color application
     */
    public static void applySeedColorTheme(@NonNull Activity activity) {
        try {
            setStatusBarColor(activity, com.nidoham.streamly.R.color.seed);
        } catch (Resources.NotFoundException e) {
            // Fallback to a default dark color if seed color is not found
            setStatusBarColorValue(activity, Color.parseColor("#1976D2"));
        }
    }

    /**
     * Establishes status bar appearance mode with comprehensive API level compatibility
     * @param activity The target activity for status bar styling
     * @param mode Desired status bar appearance configuration
     */
    public static void setStatusBarMode(@NonNull Activity activity, @NonNull StatusBarMode mode) {
        Window window = activity.getWindow();
        
        switch (mode) {
            case TRANSPARENT:
                setTransparentStatusBar(activity);
                break;
                
            case TRANSLUCENT:
                setTranslucentStatusBar(activity);
                break;
                
            case LIGHT_CONTENT:
            case DARK_CONTENT:
                setStatusBarContentColor(activity, mode);
                break;
        }
    }

    /**
     * Configures status bar content color appearance for optimal visibility and aesthetic coordination
     * @param activity The target activity for content color adjustment
     * @param mode Content color mode selection
     */
    private static void setStatusBarContentColor(@NonNull Activity activity, @NonNull StatusBarMode mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ implementation using WindowInsetsController
            WindowInsetsController insetsController = activity.getWindow().getInsetsController();
            if (insetsController != null) {
                if (mode == StatusBarMode.DARK_CONTENT) {
                    insetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                } else {
                    insetsController.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ implementation using system UI visibility flags
            View decorView = activity.getWindow().getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            
            if (mode == StatusBarMode.DARK_CONTENT) {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            
            decorView.setSystemUiVisibility(systemUiVisibility);
        }
    }

    /**
     * Establishes fully transparent status bar with edge-to-edge content rendering capability
     * @param activity The target activity for transparent status bar configuration
     */
    public static void setTransparentStatusBar(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            } else {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        }
    }

    /**
     * Configures translucent status bar overlay for enhanced visual depth while maintaining system integration
     * @param activity The target activity for translucent status bar setup
     */
    public static void setTranslucentStatusBar(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Coordinates navigation bar appearance to complement status bar styling for unified system UI theming
     * @param activity The target activity for navigation bar styling
     * @param mode Navigation bar appearance configuration
     */
    public static void setNavigationBarMode(@NonNull Activity activity, @NonNull NavigationBarMode mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            
            switch (mode) {
                case TRANSPARENT:
                    window.setNavigationBarColor(Color.TRANSPARENT);
                    break;
                    
                case LIGHT:
                    window.setNavigationBarColor(Color.WHITE);
                    setNavigationBarContentColor(activity, true);
                    break;
                    
                case DARK:
                    window.setNavigationBarColor(Color.BLACK);
                    setNavigationBarContentColor(activity, false);
                    break;
                    
                case MATCH_STATUS_BAR:
                    int statusBarColor = window.getStatusBarColor();
                    window.setNavigationBarColor(statusBarColor);
                    setNavigationBarContentColor(activity, isColorLight(statusBarColor));
                    break;
            }
        }
    }

    /**
     * Adjusts navigation bar content color for optimal button visibility and user interaction clarity
     * @param activity The target activity for navigation bar content styling
     * @param lightContent Whether to use light content on navigation bar
     */
    private static void setNavigationBarContentColor(@NonNull Activity activity, boolean lightContent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = activity.getWindow().getInsetsController();
            if (insetsController != null) {
                if (lightContent) {
                    insetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                } else {
                    insetsController.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = activity.getWindow().getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            
            if (lightContent) {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            
            decorView.setSystemUiVisibility(systemUiVisibility);
        }
    }

    /**
     * Enables immersive fullscreen experience with gesture-based system UI access for media consumption scenarios
     * @param activity The target activity for immersive mode configuration
     */
    public static void enableImmersiveMode(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    /**
     * Disables immersive mode and restores standard system UI visibility for normal application interaction
     * @param activity The target activity for immersive mode deactivation
     */
    public static void disableImmersiveMode(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), true);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
            controller.show(WindowInsetsCompat.Type.systemBars());
        } else {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /**
     * Configures device orientation behavior with comprehensive rotation control options
     * @param activity The target activity for orientation configuration
     * @param mode The desired orientation behavior mode
     */
    public static void setOrientationMode(@NonNull Activity activity, @NonNull OrientationMode mode) {
        switch (mode) {
            case PORTRAIT_ONLY:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case LANDSCAPE_ONLY:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case SENSOR_PORTRAIT:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case SENSOR_LANDSCAPE:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case AUTO_ROTATE:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            case LOCKED_CURRENT:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                break;
            case REVERSE_PORTRAIT:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case REVERSE_LANDSCAPE:
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
    }

    /**
     * Locks device orientation to current position for stable user interface during specific operations
     * @param activity The target activity for orientation locking
     */
    public static void lockCurrentOrientation(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            int currentOrientation = activity.getResources().getConfiguration().orientation;
            if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        }
    }

    /**
     * Enables automatic orientation rotation based on device sensor input and user preferences
     * @param activity The target activity for orientation unlocking
     */
    public static void unlockOrientation(@NonNull Activity activity) {
        activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Determines current device orientation state for responsive layout management
     * @param context Application context for configuration access
     * @return True if device is in landscape orientation, false for portrait
     */
    public static boolean isLandscapeOrientation(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == 
               android.content.res.Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Retrieves current screen rotation value for advanced orientation handling
     * @param activity The target activity for rotation information
     * @return Screen rotation constant (Surface.ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270)
     */
    public static int getCurrentRotation(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.getDisplay().getRotation();
        } else {
            return activity.getWindowManager().getDefaultDisplay().getRotation();
        }
    }

    /**
     * Handles orientation change events with proper status bar adjustment for consistent theming
     * This method should be called from onConfigurationChanged to maintain visual consistency
     * @param activity The target activity experiencing configuration changes
     */
    public static void handleOrientationChange(@NonNull Activity activity) {
        // Reapply seed color theme to maintain consistency across orientation changes
        applySeedColorTheme(activity);
        
        // Adjust system UI flags if needed for new orientation
        if (isLandscapeOrientation(activity)) {
            // Optional: Apply landscape-specific system UI adjustments
            setNavigationBarMode(activity, NavigationBarMode.MATCH_STATUS_BAR);
        } else {
            // Optional: Apply portrait-specific system UI adjustments
            setNavigationBarMode(activity, NavigationBarMode.DARK);
        }
    }

    /**
     * Configures orientation-specific status bar behavior for optimal user experience
     * @param activity The target activity for orientation-aware status bar setup
     */
    public static void setupOrientationAwareStatusBar(@NonNull Activity activity) {
        applySeedColorTheme(activity);
        
        // Configure different behavior based on current orientation
        if (isLandscapeOrientation(activity)) {
            // In landscape, consider using translucent or transparent status bar for media content
            setStatusBarMode(activity, StatusBarMode.TRANSLUCENT);
        } else {
            // In portrait, maintain standard status bar appearance with seed color
            applySeedColorTheme(activity);
        }
    }

    /**
     * Calculates color luminance to determine appropriate content color contrast for accessibility compliance
     * @param color The color value for luminance analysis
     * @return True if the color is considered light, false for dark colors
     */
    private static boolean isColorLight(@ColorInt int color) {
        if (color == Color.TRANSPARENT) return true;
        
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;
        
        // Apply gamma correction for accurate luminance calculation
        red = red <= 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = green <= 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = blue <= 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        
        double luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        return luminance > 0.5;
    }

    /**
     * Retrieves current status bar height for layout calculations and proper content positioning
     * @param context Application context for resource access
     * @return Status bar height in pixels, or 0 if unable to determine
     */
    public static int getStatusBarHeight(@NonNull Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * Determines navigation bar height for layout adjustments and proper content positioning
     * @param context Application context for resource access
     * @return Navigation bar height in pixels, or 0 if not available
     */
    public static int getNavigationBarHeight(@NonNull Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }
}