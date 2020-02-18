package org.flinkhub.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flinkhub.fhnet.UserData;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.FileLog;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.MessagesController;
import org.flinkhub.messenger2.NotificationCenter;
import org.flinkhub.messenger2.R;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.ActionBarMenu;
import org.flinkhub.ui.ActionBar.AlertDialog;
import org.flinkhub.ui.ActionBar.BaseFragment;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.ActionBar.ThemeDescription;
import org.flinkhub.ui.Components.AlertsCreator;
import org.flinkhub.ui.Components.EditTextBoldCursor;
import org.flinkhub.ui.Components.LayoutHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.tgnet.TLRPC;

public class ChangeLocationActivity extends BaseFragment {
    private EditTextBoldCursor cityTextField;
    private TextView checkTextView;
    private TextView helpTextView;
    private View doneButton;
    private final static int done_button = 1;
    private UserDataObj fhUserData = null;

    public ChangeLocationActivity(UserDataObj fhUserData) {
        super();
        this.fhUserData = fhUserData;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("UserCityName", R.string.UserCityName));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    updateLocation();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);

        FrameLayout fieldContainer = new FrameLayout(context);
        linearLayout.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        cityTextField = new EditTextBoldCursor(context);
        cityTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        cityTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        cityTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        cityTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        cityTextField.setMaxLines(4);
        cityTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        cityTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        cityTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        cityTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        cityTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(120) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && TextUtils.indexOf(source, '\n') != -1) {
                    doneButton.performClick();
                    return "";
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                if (result != null && source != null && result.length() != source.length()) {
                    Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(checkTextView, 2, 0);
                }
                return result;
            }
        };
        cityTextField.setFilters(inputFilters);
        cityTextField.setMinHeight(AndroidUtilities.dp(36));
        cityTextField.setHint(LocaleController.getString("UserCityHint", R.string.UserCityHint));
        cityTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        cityTextField.setCursorSize(AndroidUtilities.dp(20));
        cityTextField.setCursorWidth(1.5f);
        cityTextField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                doneButton.performClick();
                return true;
            }
            return false;
        });
        cityTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkTextView.setText(String.format("%d", (120 - cityTextField.length())));
            }
        });

        fieldContainer.addView(cityTextField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

        checkTextView = new TextView(context);
        checkTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        checkTextView.setText(String.format("%d", 120));
        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        fieldContainer.addView(checkTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT, 0, 4, 4, 0));

        helpTextView = new TextView(context);
        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserCityInfo", R.string.UserCityInfo)));
        linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));

        if (fhUserData != null) {
            String cityStr = fhUserData.getLocationStr();
            cityTextField.setText(cityStr);
            cityTextField.setSelection(cityTextField.length());
        }

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        boolean animations = preferences.getBoolean("view_animations", true);
        if (!animations) {
            cityTextField.requestFocus();
            AndroidUtilities.showKeyboard(cityTextField);
        }
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            cityTextField.requestFocus();
            AndroidUtilities.showKeyboard(cityTextField);
        }
    }

    private void updateLocation() {
        if (getParentActivity() == null || fhUserData == null) {
            return;
        }

        String locationStr = cityTextField.getText().toString();

        boolean isChanged = !(locationStr.equals(fhUserData.getLocationStr()));
        if (!isChanged) {
            finishFragment();
            return;
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("locationStr", locationStr);
        } catch (JSONException ex) {
            // TODO Handle JSON error
        }

        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setOnCancelListener(dialog -> {
            // TODO Try cancellation
        });

        (new Thread(() -> {
            UserDataObj result = UserData.updateProfileData(fhUserData.getUser_id(), postData.toString());

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

                    finishFragment();
                });
            } else {
                final String errorMessage = result.getErrorMessage();

                getParentActivity().runOnUiThread(() -> {
                    String errorHeading = "Could not update City";

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

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(cityTextField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(cityTextField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(cityTextField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField),
                new ThemeDescription(cityTextField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated),

                new ThemeDescription(helpTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText8),

                new ThemeDescription(checkTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),
        };
    }
}
