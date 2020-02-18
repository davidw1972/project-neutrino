/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.flinkhub.ui.Cells;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flinkhub.fhnet.Constants;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.MessagesController;
import org.flinkhub.messenger2.R;
import org.flinkhub.messenger2.UserConfig;
import org.flinkhub.ui.ChatActivity;
import org.telegram.tgnet.TLRPC;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Components.LayoutHelper;

import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class DialogsEmptyCell extends LinearLayout {

    private TextView emptyTextView1;
    private TextView emptyTextView2;
    private TextView startFlinkhubBotChatButton;
    private int currentType;

    private int currentAccount = UserConfig.selectedAccount;

    public DialogsEmptyCell(Context context) {
        super(context);

        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        setOnTouchListener((v, event) -> true);

        emptyTextView1 = new TextView(context);
        emptyTextView1.setTextColor(Theme.getColor(Theme.key_chats_nameMessage_threeLines));
        emptyTextView1.setText(LocaleController.getString("NoGroupChats", R.string.NoGroupChats));
        emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyTextView1.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTextView1.setGravity(Gravity.CENTER);
        addView(emptyTextView1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 4, 52, 0));

        emptyTextView2 = new TextView(context);
        String help = LocaleController.getString("NoGroupChatsHelp", R.string.NoGroupChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        emptyTextView2.setText(help);
        emptyTextView2.setTextColor(Theme.getColor(Theme.key_chats_message));
        emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        emptyTextView2.setGravity(Gravity.CENTER);
        emptyTextView2.setLineSpacing(AndroidUtilities.dp(2), 1);
        addView(emptyTextView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 7, 52, 0));

        startFlinkhubBotChatButton = new TextView(context);
        startFlinkhubBotChatButton.setText(LocaleController.getString("StartMessagingWithFlinkhubBot", R.string.StartMessagingWithFlinkhubBot).toUpperCase());
        startFlinkhubBotChatButton.setGravity(Gravity.CENTER);
        startFlinkhubBotChatButton.setTextColor(0xffffffff);
        startFlinkhubBotChatButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        startFlinkhubBotChatButton.setBackgroundResource(R.drawable.regbtn_states);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(startFlinkhubBotChatButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(startFlinkhubBotChatButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            startFlinkhubBotChatButton.setStateListAnimator(animator);
        }
        startFlinkhubBotChatButton.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        addView(startFlinkhubBotChatButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 10, 20, 10, 10));
    }

    public void setOnCTAClickListener(View.OnClickListener listener) {
        startFlinkhubBotChatButton.setOnClickListener(listener);
    }

    public void setType(int value) {
        currentType = value;
        String help;
        if (currentType == 0) {
            help = LocaleController.getString("NoGroupChatsHelp", R.string.NoGroupChatsHelp);
            if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
                help = help.replace('\n', ' ');
            }
        } else {
            help = LocaleController.getString("NoGroupChatsHelp", R.string.NoGroupChatsHelp);
            if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
                help = help.replace('\n', ' ');
            }
        }
        emptyTextView2.setText(help);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (totalHeight == 0) {
            totalHeight = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight() - (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
        }
        if (currentType == 0) {
            ArrayList<TLRPC.RecentMeUrl> arrayList = MessagesController.getInstance(currentAccount).hintDialogs;
            if (!arrayList.isEmpty()) {
                totalHeight -= AndroidUtilities.dp(72) * arrayList.size() + arrayList.size() - 1 + AndroidUtilities.dp(12 + 38);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(166), MeasureSpec.EXACTLY));
        }
    }
}
