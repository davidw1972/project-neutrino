/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

public class Intro {
    public static native void onDrawFrame();
    public static native void setScrollOffset(float a_offset);
    public static native void setPage(int page);
    public static native void setDate(float a);
    public static native void setIcTextures(int a_ic_bubble_dot, int a_ic_bubble, int a_ic_cam_lens, int a_ic_cam, int a_ic_pencil, int a_ic_pin, int a_ic_smile_eye, int a_ic_smile, int a_ic_videocam);
    public static native void setTelegramTextures(int a_telegram_sphere);
    public static native void setLearnTextures(int learn_image);
    public static native void setGrowNetworkTextures(int network_image);
    public static native void setSecureTextures(int secure_image);
    public static native void onSurfaceCreated();
    public static native void onSurfaceChanged(int a_width_px, int a_height_px, float a_scale_factor, int a1);
}
