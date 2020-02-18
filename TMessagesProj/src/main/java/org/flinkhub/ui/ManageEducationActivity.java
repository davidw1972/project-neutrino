package org.flinkhub.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.UserData;
import org.flinkhub.fhnet.models.Experience;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.FileLog;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.NotificationCenter;
import org.flinkhub.messenger2.R;
import org.flinkhub.messenger2.UserConfig;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.ActionBarMenu;
import org.flinkhub.ui.ActionBar.AlertDialog;
import org.flinkhub.ui.ActionBar.BaseFragment;
import org.flinkhub.ui.Cells.ExperienceCell;
import org.flinkhub.ui.Components.AlertsCreator;
import org.flinkhub.ui.Components.LayoutHelper;

public class ManageEducationActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ExperienceCell.EditExperienceHandler {
    private View addButton;
    private final static int add_button = 1;
    private UserDataObj fhUserData = null;
    private ExperienceCell expListView;

    public ManageEducationActivity(UserDataObj fhUserData) {
        super();
        this.fhUserData = fhUserData;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fhUserInfoDidLoad);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fhUserInfoDidLoad);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("UserEducationHeading", R.string.UserEducationHeading));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == add_button) {
                    presentFragment(new EditExperienceActivity(fhUserData, null, Experience.EDUCATION));
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        addButton = menu.addItemWithWidth(add_button, R.drawable.plus, AndroidUtilities.dp(56));

        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        fragmentView.setOnTouchListener((v, event) -> true);

//        RelativeLayout item = (RelativeLayout) findViewById(R.id.item);
//        View child = View.inflate(context, R.layout.child);
//        item.addView(child);
        expListView = new ExperienceCell(context);
        linearLayout.addView(expListView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0));
        expListView.setData(fhUserData.getEducation());
        expListView.setEditable(true);
        expListView.setClickHandler(this);

        return fragmentView;
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
            UserDataObj result = UserData.deleteEducation(fhUserData.getUser_id(), experience.getId());

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

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fhUserInfoDidLoad) {
            Integer uid = (Integer) args[0];
            if (uid == UserConfig.getInstance(currentAccount).getClientUserId()) {
                fhUserData = (UserDataObj) args[1];
                expListView.setData(fhUserData.getEducation());
            }
        }
    }
}
