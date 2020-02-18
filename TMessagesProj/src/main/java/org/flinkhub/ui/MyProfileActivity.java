/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.flinkhub.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.flinkhub.PhoneFormat.PhoneFormat;
import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.UserData;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.Experience;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.MediaDataController;
import org.flinkhub.messenger2.ImageLoader;
import org.flinkhub.messenger2.ImageLocation;
import org.flinkhub.messenger2.Tracker;
import org.flinkhub.messenger2.UserObject;
import org.flinkhub.messenger2.ApplicationLoader;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.FileLoader;
import org.flinkhub.ui.ActionBar.AlertDialog;
import org.flinkhub.ui.Cells.ExperienceCell;
import org.flinkhub.ui.Components.AlertsCreator;
import org.flinkhub.ui.Components.EditTextBoldCursor;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.flinkhub.messenger2.FileLog;
import org.flinkhub.messenger2.MessagesController;
import org.flinkhub.messenger2.MessagesStorage;
import org.flinkhub.messenger2.NotificationCenter;
import org.flinkhub.messenger2.R;
import org.flinkhub.messenger2.UserConfig;
import org.flinkhub.messenger2.MessageObject;
import org.flinkhub.ui.ActionBar.ThemeDescription;
import org.flinkhub.ui.Cells.EmptyCell;
import org.flinkhub.ui.Cells.GraySectionCell;
import org.flinkhub.ui.Cells.HeaderCell;
import org.flinkhub.ui.Cells.SettingsSearchCell;
import org.flinkhub.ui.Cells.ShadowSectionCell;
import org.flinkhub.ui.Cells.TextCell;
import org.flinkhub.ui.Cells.TextDetailCell;
import org.flinkhub.ui.Cells.TextInfoPrivacyCell;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.ActionBarMenuItem;
import org.flinkhub.ui.Components.AvatarDrawable;
import org.flinkhub.ui.Components.EmptyTextProgressView;
import org.flinkhub.ui.Components.ImageUpdater;
import org.flinkhub.ui.Components.BackupImageView;
import org.flinkhub.ui.ActionBar.BaseFragment;
import org.flinkhub.ui.Components.CombinedDrawable;
import org.flinkhub.ui.Components.LayoutHelper;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Components.RadialProgressView;
import org.flinkhub.ui.Components.RecyclerListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MyProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ImageUpdater.ImageUpdaterDelegate, ExperienceCell.EditExperienceHandler {

    private MyProfileActivity self;
    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private FrameLayout avatarContainer;
    private BackupImageView avatarImage;
    private View avatarOverlay;
    private AnimatorSet avatarAnimation;
    private RadialProgressView avatarProgressView;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AnimatorSet writeButtonAnimation;
    private ImageUpdater imageUpdater;
    private View extraHeightView;
    private View shadowView;
    private AvatarDrawable avatarDrawable;
    private ActionBarMenuItem otherItem;
    private EmptyTextProgressView emptyView;

    private TLRPC.FileLocation avatar;
    private TLRPC.FileLocation avatarBig;

    private int userId;
    private TLRPC.UserFull userInfo;
    private UserDataObj fhUserData = null;

    private int extraHeight;

    private int overscrollRow;
    private int settingsSectionRow;
    private int settingsHeaderRow;
    private int numberRow;
    private int usernameRow;
    private int cityRow;
    private int universityRow;
    private int bioRow;
    private int rowCount;

    private int educationHeaderRow;
    private int educationListRow;
    private int educationLoadingRow;
    private int educationAddRow;
    private int educationSectionRow;

    private int experienceHeaderRow;
    private int experienceListRow;
    private int experienceLoadingRow;
    private int experienceAddRow;
    private int experienceSectionRow;

    private final static int edit_name = 1;
    private final static int logout = 2;
    private final static int search_button = 3;

    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
            if (fileLocation == null) {
                return null;
            }
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                TLRPC.FileLocation photoBig = user.photo.photo_big;
                if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                    int[] coords = new int[2];
                    avatarImage.getLocationInWindow(coords);
                    PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                    object.viewX = coords[0];
                    object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                    object.parentView = avatarImage;
                    object.imageReceiver = avatarImage.getImageReceiver();
                    object.dialogId = UserConfig.getInstance(currentAccount).getClientUserId();
                    object.thumb = object.imageReceiver.getBitmapSafe();
                    object.size = -1;
                    object.radius = avatarImage.getImageReceiver().getRoundRadius();
                    object.scale = avatarContainer.getScaleX();
                    return object;
                }
            }
            return null;
        }

        @Override
        public void willHidePhotoViewer() {
            avatarImage.getImageReceiver().setVisible(true, true);
        }
    };

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        self = this;
        imageUpdater = new ImageUpdater();
        imageUpdater.parentFragment = this;
        imageUpdater.delegate = this;
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fhUserInfoDidLoad);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);

        MediaDataController.getInstance(currentAccount).checkFeaturedStickers();
        userInfo = MessagesController.getInstance(currentAccount).getUserFull(UserConfig.getInstance(currentAccount).getClientUserId());
        MessagesController.getInstance(currentAccount).loadUserInfo(UserConfig.getInstance(currentAccount).getCurrentUser(), true, classGuid);

        userId = UserConfig.getInstance(currentAccount).getClientUserId();
        fhUserData = UserData.getUser(userId, currentAccount);

        updateRowsIds();

        if (fhUserData != null) {
            Tracker.logProfileView(fhUserData.getUser_id(), true);
        }

        return true;
    }

    private void updateRowsIds() {
        overscrollRow = -1;
        numberRow = -1;
        usernameRow = -1;
        cityRow = -1;
        universityRow = -1;
        bioRow = -1;
        settingsSectionRow = -1;
        settingsHeaderRow = -1;
        experienceHeaderRow = -1;
        experienceListRow = -1;
        experienceLoadingRow = -1;
        experienceAddRow = -1;
        experienceSectionRow = -1;
        educationHeaderRow = -1;
        educationListRow = -1;
        educationLoadingRow = -1;
        educationAddRow = -1;
        educationSectionRow = -1;

        rowCount = 0;

        settingsSectionRow = rowCount++;

        experienceHeaderRow = rowCount++;

        if (UserData.isLoading(userId)) {
            experienceLoadingRow = rowCount++;
        } else if (fhUserData.hasExperience()) {
            experienceListRow = rowCount++;
        }

        experienceAddRow = rowCount++;
        experienceSectionRow = rowCount++;

        educationHeaderRow = rowCount++;

        if (UserData.isLoading(userId)) {
            educationLoadingRow = rowCount++;
        } else if (fhUserData.hasEducation()) {
            educationListRow = rowCount++;
        }

        educationAddRow = rowCount++;
        educationSectionRow = rowCount++;

        settingsHeaderRow = rowCount++;

//        overscrollRow = rowCount++;
        numberRow = rowCount++;
        usernameRow = rowCount++;
        cityRow = rowCount++;
        bioRow = rowCount++;
//        universityRow = rowCount++;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fhUserInfoDidLoad);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
        imageUpdater.clear();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == edit_name) {
                    presentFragment(new ChangeNameActivity());
                } else if (id == logout) {
                    presentFragment(new LogoutActivity());
                }
            }
        });

        int scrollTo;
        int scrollToPosition = 0;
        Object writeButtonTag = null;
        if (listView != null) {
            scrollTo = layoutManager.findFirstVisibleItemPosition();
            View topView = layoutManager.findViewByPosition(scrollTo);
            if (topView != null) {
                scrollToPosition = topView.getTop();
            } else {
                scrollTo = -1;
            }
            writeButtonTag = writeButton.getTag();
        } else {
            scrollTo = -1;
        }

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView.setTag(Theme.key_windowBackgroundGray);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        listView.setOnItemClickListener((view, position) -> {
            if (position == usernameRow) {
                presentFragment(new ChangeUsernameActivity());
            } else if (position == bioRow) {
                if (userInfo != null) {
                    presentFragment(new ChangeBioActivity());
                }
            } else if (position == numberRow) {
                presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
            } else if (position == cityRow) {
                if (fhUserData != null) {
                    presentFragment(new ChangeLocationActivity(fhUserData));
                }
            } else if (position == universityRow) {
                if (fhUserData != null) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("showBackButton", true);
                    bundle.putSerializable("fhUserData", fhUserData);
                    presentFragment(new SelectUniversityActivity(bundle));
                }
            }
        });

        emptyView = new EmptyTextProgressView(context);
        emptyView.showTextView();
        emptyView.setTextSize(18);
        emptyView.setVisibility(View.GONE);
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        extraHeightView.setPivotY(0);
        extraHeightView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarContainer = new FrameLayout(context);
        avatarContainer.setPivotX(LocaleController.isRTL ? AndroidUtilities.dp(42) : 0);
        avatarContainer.setPivotY(0);
        frameLayout.addView(avatarContainer, LayoutHelper.createFrame(42, 42, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), (LocaleController.isRTL ? 0 : 64), 0, (LocaleController.isRTL ? 112 : 0), 0));
        avatarContainer.setOnClickListener(v -> {
            if (avatar != null) {
                return;
            }
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                if (user.photo.dc_id != 0) {
                    user.photo.photo_big.dc_id = user.photo.dc_id;
                }
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        });

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarImage.setContentDescription(LocaleController.getString("AccDescrProfilePicture", R.string.AccDescrProfilePicture));
        avatarContainer.addView(avatarImage, LayoutHelper.createFrame(42, 42));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x55000000);

        avatarProgressView = new RadialProgressView(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                if (avatarImage != null && avatarImage.getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * avatarImage.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, AndroidUtilities.dp(21), paint);
                }
                super.onDraw(canvas);
            }
        };
        avatarProgressView.setSize(AndroidUtilities.dp(26));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarContainer.addView(avatarProgressView, LayoutHelper.createFrame(42, 42));

        showAvatarProgress(false, false);

        nameTextView = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                setPivotX(LocaleController.isRTL ? getMeasuredWidth() : 0);
            }
        };
        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setPivotY(0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 48 : 118, 0, LocaleController.isRTL ? 166 : 96, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(Theme.getColor(Theme.key_profile_status));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 48 : 118, 0, LocaleController.isRTL ? 166 : 96, 0));

        writeButton = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_profile_actionBackground), Theme.getColor(Theme.key_profile_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        writeButton.setBackgroundDrawable(drawable);
        writeButton.setImageResource(R.drawable.menu_camera_av);
        writeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_actionIcon), PorterDuff.Mode.MULTIPLY));
        writeButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            writeButton.setStateListAnimator(animator);
            writeButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(writeButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, LocaleController.isRTL ? 16 : 0, 0, LocaleController.isRTL ? 0 : 16, 0));
        writeButton.setOnClickListener(v -> {
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
            if (user == null) {
                user = UserConfig.getInstance(currentAccount).getCurrentUser();
            }
            if (user == null) {
                return;
            }
            imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> MessagesController.getInstance(currentAccount).deleteUserPhoto(null));
        });
        writeButton.setContentDescription(LocaleController.getString("AccDescrChangeProfilePicture", R.string.AccDescrChangeProfilePicture));

        if (scrollTo != -1) {
            layoutManager.scrollToPositionWithOffset(scrollTo, scrollToPosition);

            if (writeButtonTag != null) {
                writeButton.setTag(0);
                writeButton.setScaleX(0.2f);
                writeButton.setScaleY(0.2f);
                writeButton.setAlpha(0.0f);
                writeButton.setVisibility(View.GONE);
            }
        }

        needLayout();

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager.getItemCount() == 0) {
                    return;
                }
                int height = 0;
                View child = recyclerView.getChildAt(0);
                if (child != null && avatarContainer.getVisibility() == View.VISIBLE) {
                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void didUploadPhoto(final TLRPC.InputFile file, final TLRPC.PhotoSize bigSize, final TLRPC.PhotoSize smallSize) {
        AndroidUtilities.runOnUIThread(() -> {
            if (file != null) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.file = file;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
                    if (error == null) {
                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                        if (user == null) {
                            user = UserConfig.getInstance(currentAccount).getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            MessagesController.getInstance(currentAccount).putUser(user, false);
                        } else {
                            UserConfig.getInstance(currentAccount).setCurrentUser(user);
                        }
                        TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        } else if (small != null) {
                            user.photo.photo_small = small.location;
                        }

                        if (photo != null) {
                            if (small != null && avatar != null) {
                                File destFile = FileLoader.getPathToAttach(small, true);
                                File src = FileLoader.getPathToAttach(avatar, true);
                                src.renameTo(destFile);
                                String oldKey = avatar.volume_id + "_" + avatar.local_id + "@50_50";
                                String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                                ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUser(user, false), true);
                            }
                            if (big != null && avatarBig != null) {
                                File destFile = FileLoader.getPathToAttach(big, true);
                                File src = FileLoader.getPathToAttach(avatarBig, true);
                                src.renameTo(destFile);
                            }
                        }

                        MessagesStorage.getInstance(currentAccount).clearUserPhotos(user.id);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, false, true);
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        avatar = null;
                        avatarBig = null;
                        updateUserData();
                        showAvatarProgress(false, true);
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                        UserConfig.getInstance(currentAccount).saveConfig(true);
                    });
                });
            } else {
                avatar = smallSize.location;
                avatarBig = bigSize.location;
                avatarImage.setImage(ImageLocation.getForLocal(avatar), "50_50", avatarDrawable, null);
                showAvatarProgress(true, false);
            }
        });
    }

    private void showAvatarProgress(boolean show, boolean animated) {
        if (avatarProgressView == null) {
            return;
        }
        if (avatarAnimation != null) {
            avatarAnimation.cancel();
            avatarAnimation = null;
        }
        if (animated) {
            avatarAnimation = new AnimatorSet();
            if (show) {
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f));
            } else {
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f));
            }
            avatarAnimation.setDuration(180);
            avatarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (avatarAnimation == null || avatarProgressView == null) {
                        return;
                    }
                    if (!show) {
                        avatarProgressView.setVisibility(View.INVISIBLE);
                    }
                    avatarAnimation = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    avatarAnimation = null;
                }
            });
            avatarAnimation.start();
        } else {
            if (show) {
                avatarProgressView.setAlpha(1.0f);
                avatarProgressView.setVisibility(View.VISIBLE);
            } else {
                avatarProgressView.setAlpha(0.0f);
                avatarProgressView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        imageUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (imageUpdater != null && imageUpdater.currentPicturePath != null) {
            args.putString("path", imageUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (imageUpdater != null) {
            imageUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        } else if (id == NotificationCenter.userInfoDidLoad) {
            Integer uid = (Integer) args[0];
            if (uid == UserConfig.getInstance(currentAccount).getClientUserId() && listAdapter != null) {
                userInfo = (TLRPC.UserFull) args[1];
            }

            if (fhUserData == null) {
                fhUserData = UserData.getUser(uid, currentAccount);
            }
        } else if (id == NotificationCenter.emojiDidLoad) {
            if (listView != null) {
                listView.invalidateViews();
            }
        } else if (id == NotificationCenter.fhUserInfoDidLoad) {
            int uid = (Integer) args[0];
            if (userInfo != null && uid == userInfo.user.id) {
                fhUserData = (UserDataObj) args[1];
                updateRowsIds();

                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }

                if (fhUserData != null) {
                    Tracker.logProfileView(fhUserData.getUser_id(), true);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
        setParentActivityTitle(LocaleController.getString("Settings", R.string.Settings));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();

        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                extraHeightView.setTranslationY(newTop);
            }
            layoutParams = (FrameLayout.LayoutParams) emptyView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                emptyView.setLayoutParams(layoutParams);
            }
        }

        if (avatarContainer != null) {
            int currentExtraHeight;
            if (avatarContainer.getVisibility() == View.VISIBLE) {
                currentExtraHeight = extraHeight;
            } else {
                currentExtraHeight = 0;
            }

            float diff = currentExtraHeight / (float) AndroidUtilities.dp(88);
            extraHeightView.setScaleY(diff);
            shadowView.setTranslationY(newTop + currentExtraHeight);

            writeButton.setTranslationY((actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + currentExtraHeight - AndroidUtilities.dp(29.5f));

            final boolean setVisible = diff > 0.2f;
            boolean currentVisible = writeButton.getTag() == null;
            if (setVisible != currentVisible) {
                if (setVisible) {
                    writeButton.setTag(null);
                    writeButton.setVisibility(View.VISIBLE);
                } else {
                    writeButton.setTag(0);
                }
                if (writeButtonAnimation != null) {
                    AnimatorSet old = writeButtonAnimation;
                    writeButtonAnimation = null;
                    old.cancel();
                }
                writeButtonAnimation = new AnimatorSet();
                if (setVisible) {
                    writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimator.ofFloat(writeButton, "scaleX", 1.0f),
                            ObjectAnimator.ofFloat(writeButton, "scaleY", 1.0f),
                            ObjectAnimator.ofFloat(writeButton, "alpha", 1.0f)
                    );
                } else {
                    writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimator.ofFloat(writeButton, "scaleX", 0.2f),
                            ObjectAnimator.ofFloat(writeButton, "scaleY", 0.2f),
                            ObjectAnimator.ofFloat(writeButton, "alpha", 0.0f)
                    );
                }
                writeButtonAnimation.setDuration(150);
                writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                            writeButton.setVisibility(setVisible ? View.VISIBLE : View.GONE);
                            writeButtonAnimation = null;
                        }
                    }
                });
                writeButtonAnimation.start();
            }

            avatarContainer.setScaleX((42 + 18 * diff) / 42.0f);
            avatarContainer.setScaleY((42 + 18 * diff) / 42.0f);
            avatarProgressView.setSize(AndroidUtilities.dp(26 / avatarContainer.getScaleX()));
            avatarProgressView.setStrokeWidth(3 / avatarContainer.getScaleX());
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            avatarContainer.setTranslationY((float) Math.ceil(avatarY));
            nameTextView.setTranslationY((float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            onlineTextView.setTranslationY((float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);
            nameTextView.setScaleX(1.0f + 0.12f * diff);
            nameTextView.setScaleY(1.0f + 0.12f * diff);

            if (LocaleController.isRTL) {
                avatarContainer.setTranslationX(AndroidUtilities.dp(47 + 48) * diff);
                nameTextView.setTranslationX((21 + 48) * AndroidUtilities.density * diff);
                onlineTextView.setTranslationX((21 + 48) * AndroidUtilities.density * diff);
            } else {
                avatarContainer.setTranslationX(-AndroidUtilities.dp(47) * diff);
                nameTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
                onlineTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
            }
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
        if (user == null) {
            return;
        }
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photoBig = user.photo.photo_big;
        }
        avatarDrawable = new AvatarDrawable(user, true);

        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        if (avatarImage != null) {
            avatarImage.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));
            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));

            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig), false);
        }

        if (fhUserData == null) {
            fhUserData = UserData.getUser(user.id, currentAccount);
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == overscrollRow) {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(88));
                    }
                    break;
                }
                case 1:
                case 7: {
                    View sectionCell = holder.itemView;
                    sectionCell.setTag(position);
                    Drawable drawable;
                    drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);
                    CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                    combinedDrawable.setFullsize(true);
                    sectionCell.setBackgroundDrawable(combinedDrawable);
                    break;
                }
                case 2: {
//                    TextCell textCell = (TextCell) holder.itemView;
                    break;
                }
                case 4: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;

                    if (position == settingsHeaderRow) {
                        headerCell.setText(LocaleController.getString("Account", R.string.Account));
                    } else if (position == educationHeaderRow) {
                        headerCell.setText(LocaleController.getString("UserEducationHeading", R.string.UserEducationHeading));
                    } else if (position == experienceHeaderRow) {
                        headerCell.setText(LocaleController.getString("UserExperienceHeading", R.string.UserExperienceHeading));
                    }

                    break;
                }
                case 6: {
                    TextDetailCell textCell = (TextDetailCell) holder.itemView;
                    if (position == numberRow) {
                        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
                        String value;
                        if (user != null && user.phone != null && user.phone.length() != 0) {
                            value = PhoneFormat.getInstance().format("+" + user.phone);
                        } else {
                            value = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                        }
                        textCell.setTextAndValue(value, LocaleController.getString("TapToChangePhone", R.string.TapToChangePhone), true);
                    } else if (position == usernameRow) {
                        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
                        String value;
                        if (user != null && !TextUtils.isEmpty(user.username)) {
                            value = "@" + user.username;
                        } else {
                            value = LocaleController.getString("UsernameEmpty", R.string.UsernameEmpty);
                        }
                        textCell.setTextAndValue(value, LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == bioRow) {
                        String value;
                        if (userInfo == null || !TextUtils.isEmpty(userInfo.about)) {
                            if (userInfo == null) {
                                textCell.setTextWithEmojiAndValue(LocaleController.getString("UserBio", R.string.UserBio), LocaleController.getString("Loading", R.string.Loading), true);
                            } else {
                                textCell.setTextWithEmojiAndValue(userInfo.about, LocaleController.getString("UserBio", R.string.UserBio), true);
                            }
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("UserBio", R.string.UserBio), LocaleController.getString("UserBioDetail", R.string.UserBioDetail), true);
                        }
                    } else if (position == cityRow) {
                        if (fhUserData != null) {
                            String locationStr = fhUserData.getLocationStr();
                            if (locationStr.length() > 0) {
                                textCell.setTextAndValue(fhUserData.getLocationStr(), LocaleController.getString("UserCityName", R.string.UserCityName), true);
                            } else {
                                textCell.setTextAndValue(LocaleController.getString("UserCityName", R.string.UserCityName), LocaleController.getString("UserCityDetail", R.string.UserCityDetail), true);
                            }
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("UserCityName", R.string.UserCityName), LocaleController.getString("Loading", R.string.Loading), true);
                        }
                    } else if (position == universityRow) {
                        if (fhUserData != null) {
                            Company universityCompany = fhUserData.getCompany();
                            String universityStr = "";

                            if (universityCompany != null) {
                                universityStr = universityCompany.getCompanyName();
                            }

                            if (universityStr.length() > 0) {
                                textCell.setTextAndValue(LocaleController.getString("UserUniversityName", R.string.UserUniversityName), universityStr, true);
                            } else {
                                textCell.setTextAndValue(LocaleController.getString("UserUniversityName", R.string.UserUniversityName), LocaleController.getString("UserUniversityInfo", R.string.UserUniversityInfo), true);
                            }
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("UserUniversityName", R.string.UserUniversityName), LocaleController.getString("Loading", R.string.Loading), true);
                        }
                    }
                    break;
                }
                case 12: {
                    if (fhUserData != null) {
                        ExperienceCell expCell = (ExperienceCell) holder.itemView;

                        if (position == educationListRow) {
                            expCell.setData(fhUserData.getEducation());
                        } else if (position == experienceListRow) {
                            expCell.setData(fhUserData.getExperience());
                        }

                        expCell.setEditable(true);
                        expCell.setClickHandler(self);
                    }

                    break;
                }

                case 13: {
                    TextView btnAdd = (TextView) holder.itemView;
                    btnAdd.setGravity(Gravity.LEFT);
                    btnAdd.setTextColor(Theme.getColor(Theme.key_chats_actionBackground));
                    btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    btnAdd.setFocusable(true);
                    btnAdd.setClickable(true);
                    btnAdd.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));

                    if (position == educationAddRow) {
                        btnAdd.setText(LocaleController.getString("UserEducationAdd", R.string.UserEducationAdd));
                    } else if (position == experienceAddRow) {
                        btnAdd.setText(LocaleController.getString("UserExperienceAdd", R.string.UserExperienceAdd));
                    }

                    btnAdd.setOnClickListener((view) -> {
                        Log.e(Constants.TAG, "Click happened");
                        if (position == educationAddRow) {
                            presentFragment(new EditExperienceActivity(fhUserData, null, Experience.EDUCATION));
                        } else if (position == experienceAddRow) {
                            presentFragment(new EditExperienceActivity(fhUserData, null, Experience.WORK_EXP));
                        }
                    });
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == numberRow || position == usernameRow || position == bioRow ||
                    position == cityRow || position == universityRow ||
                    position == educationListRow || position == experienceListRow ||
                    position == educationAddRow || position == experienceAddRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new ShadowSectionCell(mContext, 88);
                    break;
                case 2:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderCell(mContext, 23);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, 10);
                    cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    cell.getTextView().setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
                    cell.getTextView().setMovementMethod(null);
                    cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 1:
                            case 3:
                                abi = "arm-v7a";
                                break;
                            case 2:
                            case 4:
                                abi = "x86";
                                break;
                            case 5:
                            case 7:
                                abi = "arm64-v8a";
                                break;
                            case 6:
                            case 8:
                                abi = "x86_64";
                                break;
                            case 0:
                            case 9:
                                abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                break;
                        }
                        cell.setText(LocaleController.formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                    view = cell;
                    break;
                case 6: {
                    view = new TextDetailCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case 7: {
                    view = new ShadowSectionCell(mContext);
                    break;
                }
                case 12: {
                    view = new ExperienceCell(mContext);
                    break;
                }
                case 13: {
                    view = new TextView(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == overscrollRow) {
                return 0;
            } else if (position == settingsSectionRow) {
                return 1;
            } else if (position == educationSectionRow || position == experienceSectionRow) {
                return 7;
            } else if (position == numberRow || position == usernameRow ||
                    position == bioRow || position == cityRow || position == universityRow) {
                return 6;
            } else if (position == settingsHeaderRow || position == educationHeaderRow ||
                    position == experienceHeaderRow) {
                return 4;
            } else if (position == educationListRow || position == experienceListRow) {
                return 12;
            } else if (position == educationAddRow || position == experienceAddRow) {
                return 13;
            } else {
                return 2;
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, HeaderCell.class, TextDetailCell.class, TextCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_windowBackgroundGray),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(extraHeightView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_title),
                new ThemeDescription(onlineTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_status),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),
                new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3),

                new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{avatarDrawable}, null, Theme.key_avatar_backgroundInProfileBlue),

                new ThemeDescription(writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon),
                new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground),
                new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),
        };
    }

    @Override
    public void editClicked(Experience experience) {
        presentFragment(new EditExperienceActivity(fhUserData, experience, experience.getExperienceType()));
    }

    @Override
    public void deleteClicked(Experience experience) {
        // TODO Show alert

        Log.e(Constants.TAG, "User wants to delete education");

        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setOnCancelListener(dialog -> {
            // TODO Try cancellation
        });

        (new Thread(() -> {
            UserDataObj result;

            if (experience.getExperienceType() == Experience.WORK_EXP) {
                result = UserData.deleteExperience(fhUserData.getUser_id(), experience.getId());
            } else if (experience.getExperienceType() == Experience.EDUCATION) {
                result = UserData.deleteEducation(fhUserData.getUser_id(), experience.getId());
            } else {
                result = null;
            }

            if (result == null) {
                result = new UserDataObj();
                result.setErrorMessage("An unknown error has occurred");
            }

            if (result.isCorrect()) {
                fhUserData = result;

                getParentActivity().runOnUiThread(() -> {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.fhUserInfoDidLoad, fhUserData.getUser_id(), fhUserData, null);

                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                });
            } else {
                final String errorMessage = result.getErrorMessage();

                getParentActivity().runOnUiThread(() -> {
                    String errorHeading = "Could not delete Education";

                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }

                    AlertsCreator.showSimpleAlert(this, errorHeading, errorMessage);
                });
            }
        })).start();

        progressDialog.show();
    }
}
