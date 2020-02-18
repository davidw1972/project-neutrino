package org.flinkhub.ui.Adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.flinkhub.fhnet.Constants;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.messenger2.LocaleController;
import org.flinkhub.messenger2.R;
import org.flinkhub.ui.ActionBar.Theme;
import org.flinkhub.ui.Components.RecyclerListView;

import java.util.List;

public class UniversitiesAdapter extends RecyclerListView.Adapter<UniversitiesAdapter.ViewHolder> {
    public interface ClickHandler {
        void onClick(Company c);
    }

    private List<Company> data;
    private Context ctx;
    private ClickHandler clickHandler = null;

    public UniversitiesAdapter(Context context) {
        ctx = context;
    }

    public void setClickHandler(ClickHandler handler) {
        this.clickHandler = handler;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView universityName;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            universityName = v.findViewById(R.id.universityName);
        }
    }

    public void add(int position, Company company) {
        data.add(position, company);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void set(List<Company> company) {
        data = company;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UniversitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.university_item, parent, false);
        UniversitiesAdapter.ViewHolder vh = new UniversitiesAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(UniversitiesAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Company company = data.get(position);

        String companyName = company.getName();
        if (companyName.length() == 0) {
            companyName = company.getDomainName();
        }

        holder.universityName.setOnClickListener((View view) -> {
            if (clickHandler != null) {
                clickHandler.onClick(company);
            }
        });

        holder.universityName.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        holder.universityName.setSingleLine(false);
        holder.universityName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        holder.universityName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        holder.universityName.setText(companyName);
        holder.universityName.setVisibility(View.VISIBLE);
        holder.universityName.setFocusable(true);
        holder.universityName.setClickable(true);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}
