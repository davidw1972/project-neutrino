package org.flinkhub.ui.Cells;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.joda.time.DateTime;
import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.Experience;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.R;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Components.LayoutHelper;

import java.util.List;
import java.util.Vector;

public class ExperienceCell extends RecyclerView {
    public interface EditExperienceHandler {
        void editClicked(Experience e);
        void deleteClicked(Experience e);
    }

    private ExperienceAdapter listAdapter;
    private boolean isEditable = false;
    private EditExperienceHandler clickHandler = null;

    public ExperienceCell(Context context) {
        super(context);
        this.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 0, 0, 0));
        this.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL);
        this.addItemDecoration(dividerItemDecoration);

        listAdapter = new ExperienceAdapter(context);
        this.setAdapter(listAdapter);
    }

    public void setData(Vector<Experience> experienceData) {
        if (listAdapter != null && experienceData != null) {
            listAdapter.set(experienceData);
        }
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public void setClickHandler(EditExperienceHandler handler) {
        this.clickHandler = handler;
    }

    public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ViewHolder> {
        private List<Experience> values;
        private Context ctx;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView companyName, expDescription;
            public TextView roleStr, timePeriodStr;
            public TextView btnEditLink, btnDeleteLink;
            public ImageView imgCompanyLogo;
            public View layout;

            public ViewHolder(View v) {
                super(v);
                layout = v;
                roleStr = v.findViewById(R.id.roleStr);
                companyName = v.findViewById(R.id.companyName);
                expDescription = v.findViewById(R.id.expDescription);
                timePeriodStr = v.findViewById(R.id.timePeriodStr);
                imgCompanyLogo = v.findViewById(R.id.companyLogo);
                btnEditLink = v.findViewById(R.id.btnEditLink);
                btnDeleteLink = v.findViewById(R.id.btnDeleteLink);
            }
        }

        public void add(int position, Experience ex) {
            values.add(position, ex);
            notifyItemInserted(position);
        }

        public void remove(int position) {
            values.remove(position);
            notifyItemRemoved(position);
        }

        public void set(List<Experience> ex) {
            values = ex;
            notifyDataSetChanged();
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ExperienceAdapter(Context context) {
            ctx = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ExperienceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.experience_row, parent, false);
            // set the view's size, margins, paddings and layout parameters
            v.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            v.setFocusable(true);
            v.setClickable(true);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ExperienceAdapter.ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Experience ex = values.get(position);

            String roleStr = "";
            if (ex.getExperienceType() == Experience.EDUCATION) {
                roleStr = ex.getDegreeStr();
            } else if (ex.getExperienceType() == Experience.WORK_EXP) {
                roleStr = ex.getRoleStr();
            }

            if (roleStr.length() > 0) {
                holder.roleStr.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                holder.roleStr.setSingleLine(false);
                holder.roleStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                holder.roleStr.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                holder.roleStr.setText(ex.getRoleStr());
                holder.roleStr.setVisibility(VISIBLE);
            } else {
                holder.roleStr.setText("");
                holder.roleStr.setVisibility(GONE);
            }

            if (ex.getExperienceType() == Experience.EDUCATION) {
                holder.roleStr.setText(ex.getDegreeStr());
            } else if (ex.getExperienceType() == Experience.WORK_EXP) {
                holder.roleStr.setText(ex.getRoleStr());
            }

            holder.companyName.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            holder.companyName.setSingleLine(false);
            holder.companyName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            holder.companyName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

            Company company = ex.getCompany();
            if (company != null) {
                String companyName = company.getName();
                if (companyName.length() == 0) {
                    companyName = company.getDomainName();
                }

                if (companyName.length() == 0) {
                    companyName = "Unknown";
                }

                holder.companyName.setText(companyName);
                holder.companyName.setVisibility(VISIBLE);
            } else {
                holder.companyName.setVisibility(GONE);
            }

            holder.timePeriodStr.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            holder.timePeriodStr.setSingleLine(false);
            holder.timePeriodStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            holder.timePeriodStr.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

            String timePeriodStr = "";
            DateTime startDate = ex.getStartDate();
            DateTime endDate = ex.getEndDate();

            if (ex.isCurrent()) {
                if (startDate != null) {
                    timePeriodStr = ex.getStartDate().toString("MMM yyyy") + " - Current";
                }
            } else {
                if (startDate != null && endDate != null) {
                    timePeriodStr = ex.getStartDate().toString("MMM yyyy") + " - " + ex.getEndDate().toString("MMM yyyy");
                } else if (startDate != null) {
                    timePeriodStr = ex.getStartDate().toString("MMM yyyy") + " - Unknown";
                }
            }

            if (timePeriodStr.length() > 0) {
                holder.timePeriodStr.setText(timePeriodStr);
                holder.timePeriodStr.setVisibility(VISIBLE);
            } else {
                holder.timePeriodStr.setVisibility(GONE);
            }

            String desc = ex.getDescription();
            if (desc != null && desc.length() > 0) {
                holder.expDescription.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                holder.expDescription.setSingleLine(false);
                holder.expDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                holder.expDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
                holder.expDescription.setText(desc);
                holder.expDescription.setVisibility(VISIBLE);
            } else {
                holder.expDescription.setText("");
                holder.expDescription.setVisibility(GONE);
            }

            String imgUrl = Constants.LOGO_BASE_URL + ex.getCompany().getDomainName();

            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.fitCenter();
            options.circleCrop();

            Glide.with(ctx)
                    .load(imgUrl)
                    .placeholder(R.drawable.loading_animation)
                    .apply(options)
                    .into(holder.imgCompanyLogo);

            if (isEditable) {
                holder.btnEditLink.setVisibility(VISIBLE);
                holder.btnDeleteLink.setVisibility(VISIBLE);

                holder.btnEditLink.setFocusable(true);
                holder.btnEditLink.setClickable(true);
                holder.btnEditLink.setOnClickListener((View view) -> {
                    clickHandler.editClicked(ex);
                });

                holder.btnDeleteLink.setFocusable(true);
                holder.btnDeleteLink.setClickable(true);
                holder.btnDeleteLink.setOnClickListener((View view) -> {
                    clickHandler.deleteClicked(ex);
                });
            } else {
                holder.btnEditLink.setVisibility(GONE);
                holder.btnDeleteLink.setVisibility(GONE);
            }
        }


        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return values.size();
        }

    }

    //    @Override
//    protected void onDraw(Canvas canvas) {
//
//    }
}
