package com.google.android.gms.location.sample.locationupdatesforegroundservice.ui;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v4.view.WindowInsetsCompat;

import com.google.android.gms.location.sample.locationupdatesforegroundservice.MyApp;

import static android.os.Build.VERSION.SDK_INT;

public class WindowInsets {
    private WindowInsetsCompat windowInsetsCompat;
    private boolean isFullScreen = false;
    
    /**
     * Constructs a new WindowInsetsCompat, copying all values from a source WindowInsetsCompat.
     *
     * @param src source from which values are copied
     */
    public WindowInsets(WindowInsetsCompat src) {
        this.windowInsetsCompat = src;
    }

    /**
     * Returns the left system window inset in pixels.
     *
     * <p>The system window inset represents the area of a full-screen window that is
     * partially or fully obscured by the status bar, navigation bar, IME or other system windows.
     * </p>
     *
     * @return The left system window inset
     */
    public int getSystemWindowInsetLeft() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.getSystemWindowInsetLeft();
        } else {
            return 0;
        }
    }

    /**
     * Returns the top system window inset in pixels.
     *
     * <p>The system window inset represents the area of a full-screen window that is
     * partially or fully obscured by the status bar, navigation bar, IME or other system windows.
     * </p>
     *
     * @return The top system window inset
     */
    public int getSystemWindowInsetTop() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.getSystemWindowInsetTop();
        } else if (!isFullScreen) {
            return getStatusbarHeight();
        } else {
            return 0;
        }
    }

    public static int getStatusbarHeight() {
        Resources res = MyApp.getInstance().getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return res.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Returns the right system window inset in pixels.
     *
     * <p>The system window inset represents the area of a full-screen window that is
     * partially or fully obscured by the status bar, navigation bar, IME or other system windows.
     * </p>
     *
     * @return The right system window inset
     */
    public int getSystemWindowInsetRight() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.getSystemWindowInsetRight();
        } else {
            return 0;
        }
    }

    /**
     * Returns the bottom system window inset in pixels.
     *
     * <p>The system window inset represents the area of a full-screen window that is
     * partially or fully obscured by the status bar, navigation bar, IME or other system windows.
     * </p>
     *
     * @return The bottom system window inset
     */
    public int getSystemWindowInsetBottom() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.getSystemWindowInsetBottom();
        } else {
            return 0;
        }
    }

    /**
     * Returns true if this WindowInsets has nonzero system window insets.
     *
     * <p>The system window inset represents the area of a full-screen window that is
     * partially or fully obscured by the status bar, navigation bar, IME or other system windows.
     * </p>
     *
     * @return true if any of the system window inset values are nonzero
     */
    public boolean hasSystemWindowInsets() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.hasSystemWindowInsets();
        } else {
            return false;
        }
    }

    /**
     * Returns true if this WindowInsets has any nonzero insets.
     *
     * @return true if any inset values are nonzero
     */
    public boolean hasInsets() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.hasInsets();
        } else {
            return false;
        }
    }

    /**
     * Check if these insets have been fully consumed.
     *
     * <p>Insets are considered "consumed" if the applicable <code>consume*</code> methods
     * have been called such that all insets have been set to zero. This affects propagation of
     * insets through the view hierarchy; insets that have not been fully consumed will continue
     * to propagate down to child views.</p>
     *
     * <p>The result of this method is equivalent to the return value of
     * {@link android.view.View#fitSystemWindows(android.graphics.Rect)}.</p>
     *
     * @return true if the insets have been fully consumed.
     */
    public boolean isConsumed() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.isConsumed();
        } else {
            return false;
        }
    }

    /**
     * Returns true if the associated window has a round shape.
     *
     * <p>A round window's left, top, right and bottom edges reach all the way to the
     * associated edges of the window but the corners may not be visible. Views responding
     * to round insets should take care to not lay out critical elements within the corners
     * where they may not be accessible.</p>
     *
     * @return True if the window is round
     */
    public boolean isRound() {
        if (SDK_INT >= 20) {
            return windowInsetsCompat.isRound();
        } else {
            return false;
        }
    }

    /**
     * Returns a copy of this WindowInsets with the system window insets fully consumed.
     *
     * @return A modified copy of this WindowInsets
     */
    public WindowInsetsCompat consumeSystemWindowInsets() {
        if (SDK_INT >= 20) {
            return new WindowInsetsCompat(windowInsetsCompat.consumeSystemWindowInsets());
        } else {
            return null;
        }
    }

    /**
     * Returns a copy of this WindowInsets with selected system window insets replaced
     * with new values.
     *
     * @param left New left inset in pixels
     * @param top New top inset in pixels
     * @param right New right inset in pixels
     * @param bottom New bottom inset in pixels
     * @return A modified copy of this WindowInsets
     */
    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        if (SDK_INT >= 20) {
            return new WindowInsetsCompat(
                    windowInsetsCompat.replaceSystemWindowInsets(left, top, right, bottom));
        } else {
            return null;
        }
    }

    /**
     * Returns a copy of this WindowInsets with selected system window insets replaced
     * with new values.
     *
     * @param systemWindowInsets New system window insets. Each field is the inset in pixels
     *                           for that edge
     * @return A modified copy of this WindowInsets
     */
    public WindowInsetsCompat replaceSystemWindowInsets(Rect systemWindowInsets) {
        if (SDK_INT >= 21) {
            return new WindowInsetsCompat(
                    windowInsetsCompat.replaceSystemWindowInsets(systemWindowInsets));
        } else {
            return null;
        }
    }

    /**
     * Returns the top stable inset in pixels.
     *
     * <p>The stable inset represents the area of a full-screen window that <b>may</b> be
     * partially or fully obscured by the system UI elements.  This value does not change
     * based on the visibility state of those elements; for example, if the status bar is
     * normally shown, but temporarily hidden, the stable inset will still provide the inset
     * associated with the status bar being shown.</p>
     *
     * @return The top stable inset
     */
    public int getStableInsetTop() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.getStableInsetTop();
        } else {
            return 0;
        }
    }

    /**
     * Returns the left stable inset in pixels.
     *
     * <p>The stable inset represents the area of a full-screen window that <b>may</b> be
     * partially or fully obscured by the system UI elements.  This value does not change
     * based on the visibility state of those elements; for example, if the status bar is
     * normally shown, but temporarily hidden, the stable inset will still provide the inset
     * associated with the status bar being shown.</p>
     *
     * @return The left stable inset
     */
    public int getStableInsetLeft() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.getStableInsetLeft();
        } else {
            return 0;
        }
    }

    /**
     * Returns the right stable inset in pixels.
     *
     * <p>The stable inset represents the area of a full-screen window that <b>may</b> be
     * partially or fully obscured by the system UI elements.  This value does not change
     * based on the visibility state of those elements; for example, if the status bar is
     * normally shown, but temporarily hidden, the stable inset will still provide the inset
     * associated with the status bar being shown.</p>
     *
     * @return The right stable inset
     */
    public int getStableInsetRight() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.getStableInsetRight();
        } else {
            return 0;
        }
    }


    /**
     * Returns the bottom stable inset in pixels.
     *
     * <p>The stable inset represents the area of a full-screen window that <b>may</b> be
     * partially or fully obscured by the system UI elements.  This value does not change
     * based on the visibility state of those elements; for example, if the status bar is
     * normally shown, but temporarily hidden, the stable inset will still provide the inset
     * associated with the status bar being shown.</p>
     *
     * @return The bottom stable inset
     */
    public int getStableInsetBottom() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.getStableInsetBottom();
        } else {
            return 0;
        }
    }

    /**
     * Returns true if this WindowInsets has nonzero stable insets.
     *
     * <p>The stable inset represents the area of a full-screen window that <b>may</b> be
     * partially or fully obscured by the system UI elements.  This value does not change
     * based on the visibility state of those elements; for example, if the status bar is
     * normally shown, but temporarily hidden, the stable inset will still provide the inset
     * associated with the status bar being shown.</p>
     *
     * @return true if any of the stable inset values are nonzero
     */
    public boolean hasStableInsets() {
        if (SDK_INT >= 21) {
            return windowInsetsCompat.hasStableInsets();
        } else {
            return false;
        }
    }

    /**
     * Returns a copy of this WindowInsets with the stable insets fully consumed.
     *
     * @return A modified copy of this WindowInsetsCompat
     */
    public WindowInsetsCompat consumeStableInsets() {
        if (SDK_INT >= 21) {
            return new WindowInsetsCompat(windowInsetsCompat.consumeStableInsets());
        } else {
            return null;
        }
    }
    
    
}
