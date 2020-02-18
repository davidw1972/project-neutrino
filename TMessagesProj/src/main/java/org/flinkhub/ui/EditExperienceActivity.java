package org.flinkhub.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flinkhub.ui.Components.AlertsCreator;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.UserData;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.Experience;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.FileLog;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.NotificationCenter;
import org.flinkhub.messenger2.R;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.ActionBarMenu;
import org.flinkhub.ui.ActionBar.AlertDialog;
import org.flinkhub.ui.ActionBar.BaseFragment;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Components.EditTextBoldCursor;
import org.flinkhub.ui.Components.LayoutHelper;

import java.util.Calendar;

public class EditExperienceActivity extends BaseFragment {
    private View doneButton;
    private EditTextBoldCursor degreeField, descriptionField, roleField;
    private EditTextBoldCursor companyDomainNameField, startDateField;
    private EditTextBoldCursor endDateField;
    private CheckBox currentlyHere;
    private TextView helpTextView;
    private final static int done_button = 1;
    private Experience experience = null;
    private UserDataObj fhUserData = null;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();

    public EditExperienceActivity(UserDataObj fhUserData, Experience experience, int experienceType) {
        super();
        this.fhUserData = fhUserData;
        if (experience == null) {
            experience = new Experience();
            experience.setExperienceType(experienceType);
        }

        this.experience = experience;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        if (experience.getExperienceType() == Experience.EDUCATION) {
            actionBar.setTitle(LocaleController.getString("UserEducationHeading", R.string.UserEducationHeading));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            actionBar.setTitle(LocaleController.getString("UserExperienceHeading", R.string.UserExperienceHeading));
        }

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    saveExperience();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);

        if (experience.getExperienceType() == Experience.EDUCATION) {
            FrameLayout fieldContainer = new FrameLayout(context);
            linearLayout.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

            degreeField = new EditTextBoldCursor(context);
            degreeField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            degreeField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            degreeField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            degreeField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
            degreeField.setSingleLine(true);
            degreeField.setMaxLines(1);
            degreeField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
            degreeField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            degreeField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            degreeField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            degreeField.setImeOptions(EditorInfo.IME_ACTION_DONE);
            degreeField.setMinHeight(AndroidUtilities.dp(36));
            degreeField.setHint(LocaleController.getString("UserEducationDegreeHeading", R.string.UserEducationDegreeHeading));
            degreeField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            degreeField.setCursorSize(AndroidUtilities.dp(20));
            degreeField.setCursorWidth(1.5f);
            degreeField.setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                    companyDomainNameField.requestFocus();
                    return true;
                }
                return false;
            });

            degreeField.setText(experience.getDegreeStr());
            fieldContainer.addView(degreeField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

            helpTextView = new TextView(context);
            helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
            helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserEducationDegreePrompt", R.string.UserEducationDegreePrompt)));
            linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            FrameLayout fieldContainer = new FrameLayout(context);
            linearLayout.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

            roleField = new EditTextBoldCursor(context);
            roleField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            roleField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            roleField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            roleField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
            roleField.setSingleLine(true);
            roleField.setMaxLines(1);
            roleField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
            roleField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            roleField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            roleField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            roleField.setImeOptions(EditorInfo.IME_ACTION_DONE);
            roleField.setMinHeight(AndroidUtilities.dp(36));
            roleField.setHint(LocaleController.getString("UserExperienceRoleHeading", R.string.UserExperienceRoleHeading));
            roleField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            roleField.setCursorSize(AndroidUtilities.dp(20));
            roleField.setCursorWidth(1.5f);
            roleField.setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                    companyDomainNameField.requestFocus();
                    return true;
                }
                return false;
            });

            roleField.setText(experience.getRoleStr());
            fieldContainer.addView(roleField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

            helpTextView = new TextView(context);
            helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
            helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserExperienceRolePrompt", R.string.UserExperienceRolePrompt)));
            linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
        }

        FrameLayout fieldContainer2 = new FrameLayout(context);
        linearLayout.addView(fieldContainer2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        companyDomainNameField = new EditTextBoldCursor(context);
        companyDomainNameField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        companyDomainNameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        companyDomainNameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        companyDomainNameField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        companyDomainNameField.setSingleLine(true);
        companyDomainNameField.setMaxLines(1);
        companyDomainNameField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        companyDomainNameField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        companyDomainNameField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        companyDomainNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        companyDomainNameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        companyDomainNameField.setMinHeight(AndroidUtilities.dp(36));
        companyDomainNameField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        companyDomainNameField.setCursorSize(AndroidUtilities.dp(20));
        companyDomainNameField.setCursorWidth(1.5f);
        companyDomainNameField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                startDateField.performClick();
                return true;
            }
            return false;
        });

        if (experience.getExperienceType() == Experience.EDUCATION) {
            companyDomainNameField.setHint(LocaleController.getString("UserEducationCompanyName", R.string.UserEducationCompanyName));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            companyDomainNameField.setHint(LocaleController.getString("UserExperienceCompanyName", R.string.UserExperienceCompanyName));
        }

        if (experience != null) {
            Company c = experience.getCompany();
            if (c != null) {
                companyDomainNameField.setText(c.getDomainName());
            }
        }

        fieldContainer2.addView(companyDomainNameField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

        helpTextView = new TextView(context);
        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        if (experience.getExperienceType() == Experience.EDUCATION) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserEducationCompanyNamePrompt", R.string.UserEducationCompanyNamePrompt)));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserExperienceCompanyNamePrompt", R.string.UserExperienceCompanyNamePrompt)));
        }

        linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));

        FrameLayout fieldContainer3 = new FrameLayout(context);
        linearLayout.addView(fieldContainer3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        DatePickerDialog.OnDateSetListener startDateListener = (view, year, monthOfYear, dayOfMonth) -> {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, monthOfYear);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            startDateField.setText(new DateTime(startDateCalendar.getTime()).toString(Constants.DT_FORMAT_MONTH_YEAR));
        };

        startDateField = new EditTextBoldCursor(context);
        startDateField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        startDateField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        startDateField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        startDateField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        startDateField.setSingleLine(true);
        startDateField.setMaxLines(1);
        startDateField.setFocusable(false);
        startDateField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        startDateField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        startDateField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        startDateField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        startDateField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        startDateField.setMinHeight(AndroidUtilities.dp(36));
        startDateField.setHint(LocaleController.getString("UserEducationStartDate", R.string.UserEducationStartDate));
        startDateField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        startDateField.setCursorSize(AndroidUtilities.dp(20));
        startDateField.setCursorWidth(1.5f);
        startDateField.setOnClickListener((View v) -> {
            new DatePickerDialog(context, startDateListener, startDateCalendar
                    .get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (experience.getStartDate() != null) {
            DateTime startDate = experience.getStartDate();
            startDateCalendar.set(Calendar.YEAR, startDate.getYear());
            startDateCalendar.set(Calendar.MONTH, startDate.getMonthOfYear());
            startDateCalendar.set(Calendar.DAY_OF_MONTH, startDate.getDayOfMonth());
            startDateField.setText(startDate.toString(Constants.DT_FORMAT_MONTH_YEAR));
        }

        fieldContainer3.addView(startDateField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

        helpTextView = new TextView(context);
        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        if (experience.getExperienceType() == Experience.EDUCATION) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserEducationStartDatePrompt", R.string.UserEducationStartDatePrompt)));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserExperienceStartDatePrompt", R.string.UserExperienceStartDatePrompt)));
        }

        linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));

        FrameLayout fieldContainer6 = new FrameLayout(context);
        linearLayout.addView(fieldContainer6, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

//        currentlyHere = new CheckBox(context);
//        if (experience.getExperienceType() == Experience.EDUCATION) {
//            currentlyHere.setText("I currently study here");
//        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
//            currentlyHere.setText("I currently work here");
//        }
//        currentlyHere.setChecked(false);
//        currentlyHere.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
//            if (buttonView.isChecked()) {
//                endDateField.setVisibility(View.GONE);
//            } else {
//                endDateField.setVisibility(View.VISIBLE);
//            }
//        });

        FrameLayout fieldContainer4 = new FrameLayout(context);
        linearLayout.addView(fieldContainer4, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, monthOfYear, dayOfMonth) -> {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, monthOfYear);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endDateField.setText(new DateTime(endDateCalendar.getTime()).toString(Constants.DT_FORMAT_MONTH_YEAR));
            descriptionField.requestFocus();
        };

        endDateField = new EditTextBoldCursor(context);
        endDateField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        endDateField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        endDateField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        endDateField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        endDateField.setSingleLine(true);
        endDateField.setMaxLines(1);
        endDateField.setFocusable(false);
        endDateField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        endDateField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        endDateField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        endDateField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        endDateField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        endDateField.setMinHeight(AndroidUtilities.dp(36));
        endDateField.setHint(LocaleController.getString("UserEducationEndDate", R.string.UserEducationEndDate));
        endDateField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        endDateField.setCursorSize(AndroidUtilities.dp(20));
        endDateField.setCursorWidth(1.5f);
        endDateField.setOnClickListener((View v) -> {
            new DatePickerDialog(context, endDateListener, endDateCalendar
                    .get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH),
                    endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (experience.getEndDate() != null) {
            DateTime endDate = experience.getEndDate();
            endDateCalendar.set(Calendar.YEAR, endDate.getYear());
            endDateCalendar.set(Calendar.MONTH, endDate.getMonthOfYear());
            endDateCalendar.set(Calendar.DAY_OF_MONTH, endDate.getDayOfMonth());
            endDateField.setText(endDate.toString(Constants.DT_FORMAT_MONTH_YEAR));
        }

        fieldContainer4.addView(endDateField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

        helpTextView = new TextView(context);
        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        if (experience.getExperienceType() == Experience.EDUCATION) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserEducationEndDatePrompt", R.string.UserEducationEndDatePrompt)));
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UserExperienceEndDatePrompt", R.string.UserExperienceEndDatePrompt)));
        }
        linearLayout.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));

        FrameLayout fieldContainer5 = new FrameLayout(context);
        linearLayout.addView(fieldContainer5, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        descriptionField = new EditTextBoldCursor(context);
        descriptionField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        descriptionField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descriptionField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        descriptionField.setMinLines(1);
        descriptionField.setMaxLines(10);
        descriptionField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        descriptionField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        descriptionField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        descriptionField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        descriptionField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        descriptionField.setMinHeight(AndroidUtilities.dp(36));
        descriptionField.setHint(LocaleController.getString("UserEducationDescription", R.string.UserEducationDescription));
        descriptionField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionField.setCursorSize(AndroidUtilities.dp(20));
        descriptionField.setCursorWidth(1.5f);
        descriptionField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                doneButton.performClick();
                return true;
            }
            return false;
        });

        descriptionField.setText(experience.getDescription());
        fieldContainer5.addView(descriptionField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

//        ScrollView c1 = (ScrollView) View.inflate(context, R.layout.edit_experience, null);
//        fieldContainer.addView(c1);

        return fragmentView;
    }

    private void isValid() {
    }

    private void saveExperience() {
        if (getParentActivity() == null) {
            return;
        }

        String degreeStr = "";
        String roleStr = "";

        if (experience.getExperienceType() == Experience.EDUCATION) {
            degreeStr = degreeField.getText().toString();
        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
            roleStr = roleField.getText().toString();
        }

        String companyDomainName = companyDomainNameField.getText().toString();
        String startDate = new DateTime(startDateCalendar.getTime()).toString(Constants.DT_FORMAT_TO_SERVER);
        String endDate = new DateTime(endDateCalendar.getTime()).toString(Constants.DT_FORMAT_TO_SERVER);
        String description = descriptionField.getText().toString();

        // TODO Validation

        // No need to post to server if data is not changed
        boolean isChanged = true;
        if (!isChanged) {
            finishFragment();
            return;
        }

        JSONObject postData = new JSONObject();

        try {
            if (experience.getExperienceType() == Experience.EDUCATION) {
                postData.put("degree", degreeStr);
            } else if (experience.getExperienceType() == Experience.WORK_EXP) {
                postData.put("jobTitle", roleStr);
            }

            postData.put("companyDomainName", companyDomainName);
            postData.put("startDate", startDate);
            postData.put("endDate", endDate);
            postData.put("description", description);
        } catch (JSONException ex) {
            // TODO Handle json error
        }

        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setOnCancelListener(dialog -> {
            // TODO Try cancellation
        });

        (new Thread(() -> {
            UserDataObj result = null;

            if (experience.getId().length() > 0) {
                if (experience.getExperienceType() == Experience.EDUCATION) {
                    result = UserData.updateEducation(fhUserData.getUser_id(), experience.getId(), postData.toString());
                } else if (experience.getExperienceType() == Experience.WORK_EXP) {
                    result = UserData.updateExperience(fhUserData.getUser_id(), experience.getId(), postData.toString());
                }
            } else {
                if (experience.getExperienceType() == Experience.EDUCATION) {
                    result = UserData.addEducation(fhUserData.getUser_id(), postData.toString());
                } else if (experience.getExperienceType() == Experience.WORK_EXP) {
                    result = UserData.addExperience(fhUserData.getUser_id(), postData.toString());
                }
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

                    Log.e(Constants.TAG, "Save/update done");
                    finishFragment();
                });
            } else {
                final String errorMessage = result.getErrorMessage();

                getParentActivity().runOnUiThread(() -> {
                    String errorHeading = "Could not update Education";

                    if (experience.getId().length() > 0) {
                        if (experience.getExperienceType() == Experience.EDUCATION) {
                            errorHeading = "Could not update Education";
                        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
                            errorHeading = "Could not update Experience";
                        }
                    } else {
                        if (experience.getExperienceType() == Experience.EDUCATION) {
                            errorHeading = "Could not add Education";
                        } else if (experience.getExperienceType() == Experience.WORK_EXP) {
                            errorHeading = "Could not add Experience";
                        }
                    }

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

