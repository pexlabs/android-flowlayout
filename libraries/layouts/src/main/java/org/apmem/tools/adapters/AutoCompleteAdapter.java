package org.apmem.tools.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.apmem.tools.layouts.R;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.LetterTileProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by kaustubh on 27/10/17.
 */

public final class AutoCompleteAdapter<T extends ChipInterface> extends BaseAdapter
        implements Filterable {

    private List<T> mListItems;
    private AstroFilter mAstroFilter;
    private List<T> mSuggestions;
    private LetterTileProvider mLetterTileProvider;

    public AutoCompleteAdapter(Context context) {
        mListItems = new ArrayList<>();
        mSuggestions = new ArrayList<>();
        mAstroFilter = new AstroFilter();
        mLetterTileProvider = new LetterTileProvider(context);
    }

    @Override
    public int getCount() {
        return mListItems != null ? mListItems.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_autocomplete, null);
            holder = new ViewHolder();
            holder.mAvatar = (CircleImageView) convertView.findViewById(R.id.image);
            holder.mLabel = (TextView) convertView.findViewById(R.id.email);
            holder.mInfo = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mListItems.get(position).getAvatarDrawable() != null) {
            holder.mAvatar.setImageDrawable(mListItems.get(position).getAvatarDrawable());
        } else {
            holder.mAvatar.setImageDrawable(new BitmapDrawable(parent.getContext().getResources(),
                    mLetterTileProvider.getLetterTile(mListItems.get(position).getLabel())));
        }
        holder.mLabel.setText(mListItems.get(position).getLabel());
        holder.mInfo.setText(mListItems.get(position).getInfo());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mAstroFilter;
    }

    public void setData(List<T> data) {
        mListItems = data;
    }

    static class ViewHolder {
        TextView mLabel;
        TextView mInfo;
        CircleImageView mAvatar;
    }

    private class AstroFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mSuggestions.clear();

            if (!TextUtils.isEmpty(constraint)) {
                for (int i=0 ; i<mListItems.size(); i++) {
                    ChipInterface chipInterface = mListItems.get(i);
                    if (chipInterface.getLabel().toLowerCase(Locale.ENGLISH).contains(constraint)
                            || chipInterface.getInfo().toLowerCase(Locale.ENGLISH).contains(constraint)) {
                        mSuggestions.add(mListItems.get(i));
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = mSuggestions;
            results.count = mSuggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
