package org.flinkhub.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.flinkhub.fhnet.CompanyData;
import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.UserData;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.CompanySearchResult;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.BuildVars;
import org.flinkhub.messenger2.FileLog;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.NotificationCenter;
import org.flinkhub.messenger2.R;
import org.flinkhub.ui.ActionBar.ActionBar;
import org.flinkhub.ui.ActionBar.AlertDialog;
import org.flinkhub.ui.ActionBar.BaseFragment;
import org.flinkhub.ui.ActionBar.MenuDrawable;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Adapters.UniversitiesAdapter;
import org.flinkhub.ui.Components.AlertsCreator;
import org.flinkhub.ui.Components.EmptyTextProgressView;
import org.flinkhub.ui.Components.LayoutHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class SelectUniversityActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private Context ctx;
    private boolean showBackButton;
    private UserDataObj fhUserData;
    private MenuDrawable menuDrawable;

    private RecyclerView listView;
    private TextView txtResult;
    private UniversitiesAdapter universitiesAdapter;
    private EditText txtUniversitySearch;

    private Timer searchCompaniesTimer;
    private boolean requestInProgress = false;
    private boolean requestPending = false;
    private AlertDialog progressDialog = null;

    public SelectUniversityActivity(Bundle bundle) {
        showBackButton = bundle.getBoolean("showBackButton", true);
        fhUserData = (UserDataObj) bundle.getSerializable("fhUserData");
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }

    @Override
    public View createView(Context context) {
        ctx = context;
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);

        if (showBackButton) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setBackButtonContentDescription(LocaleController.getString("Back", R.string.Back));
        } else {
            actionBar.setBackButtonDrawable(menuDrawable = new MenuDrawable());
            actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", R.string.AccDescrOpenMenu));
        }

        actionBar.setAddToContainer(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setTitle("University");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (showBackButton) {
                        finishFragment();
                    } else {
                        if (parentLayout != null) {
                            parentLayout.getDrawerLayoutContainer().openDrawer(false);
                        }
                    }
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView.setTag(Theme.key_windowBackgroundGray);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        frameLayout.addView(actionBar);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.select_university, frameLayout, false);
        frameLayout.addView(layout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 10, 95, 10, 0));

        listView = layout.findViewById(R.id.universityList);
        txtUniversitySearch = layout.findViewById(R.id.txtUniversitySearch);
        txtResult = layout.findViewById(R.id.txtResult);

        prepareListView();
        loadUniversitySuggestions("");
        return fragmentView;
    }

    private void openBotChat() {
        try {
            Intent openChat = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BOT_LINK));
            openChat.setPackage(BuildVars.APP_PACKAGE_NAME);
            getParentActivity().startActivity(openChat);
            finishFragment();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Intent failed");
            Log.e(Constants.TAG, e.getMessage());
        }
    }

    private void prepareListView() {
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));

        universitiesAdapter = new UniversitiesAdapter(ctx);
        listView.setAdapter(universitiesAdapter);
        universitiesAdapter.setClickHandler(this::selectUniversity);
        universitiesAdapter.set(new Vector<>());

        listView.addItemDecoration(new DividerItemDecoration(listView.getContext(), DividerItemDecoration.VERTICAL));
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);

        txtUniversitySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchCompaniesTimer != null) {
                    searchCompaniesTimer.cancel();
                }

                if (requestInProgress) {
                    requestPending = true;
                } else {
                    searchCompaniesTimer = new Timer();
                    searchCompaniesTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String query = txtUniversitySearch.getText().toString();
                            loadUniversitySuggestions(query);
                        }
                    }, 500);
                }
            }
        });
    }

    private void loadUniversitySuggestions(String query) {
        if (requestInProgress) {
            return;
        }

        (new Thread(() -> {
            requestInProgress = true;

            getParentActivity().runOnUiThread(() -> {
                progressDialog = new AlertDialog(getParentActivity(), 5);
                progressDialog.setOnCancelListener(dialog -> {
                    // TODO Try cancellation
                });
                progressDialog.show();
            });

            CompanySearchResult result = CompanyData.getUniversities(query);

            requestInProgress = false;

            if (!result.getQueryStr().equals(query)) {
                getParentActivity().runOnUiThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                });
            } else if (result.isCorrect()) {
                final List<Company> companies = result.getCompanies();
                getParentActivity().runOnUiThread(() -> {
                    universitiesAdapter.set(companies);

                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }

                    if (companies.size() > 0) {
                        txtResult.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    } else {
                        if (query.length() > 0) {
                            txtResult.setText("No universities found matching search query.");
                        } else {
                            txtResult.setText("Oops.. no universities found!");
                        }

                        txtResult.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                });
            } else {
                // Ignoring error silently
                getParentActivity().runOnUiThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }

//                    AlertsCreator.showSimpleAlert(this, errorHeading, errorMessage);
                });
            }

            if (requestPending) {
                getParentActivity().runOnUiThread(() -> {
                    String newQuery = txtUniversitySearch.getText().toString();
                    loadUniversitySuggestions(newQuery);
                    requestPending = false;
                });
            }
        })).start();
    }

    private void selectUniversity(Company company) {
        JSONObject postData = new JSONObject();

        try {
            postData.put("companyId", company.getId());
        } catch (JSONException ex) {
            // TODO Handle json error
        }

        progressDialog = new AlertDialog(getParentActivity(), 3);
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

                    Log.e(Constants.TAG, "Save/update done");

                    String botStage = fhUserData.getBotStage();
                    if (botStage.equals("")) {
                        String errorHeading = "Flinkhub Groups Bot";
                        String errorMessage = "Our bot @" + Constants.BOT_USERNAME + " will guide you through the process of joining Alumni Groups. Chat with it to get the invite links.";
                        AlertsCreator.showBotChatAlert(this, errorHeading, errorMessage, (dialogInterface, i) -> {
                            openBotChat();
                        });
                        return;
                    }

                    Bundle args = new Bundle();
                    presentFragment(new DialogsActivity(args));
                });
            } else {
                final String errorMessage = result.getErrorMessage();

                getParentActivity().runOnUiThread(() -> {
                    String errorHeading = "Request failed";

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
    public void onResume() {
        if (fhUserData != null && fhUserData.hasUniversity()) {
            Bundle args = new Bundle();
            presentFragment(new DialogsActivity(args));
            finishFragment();
        }
    }
}
