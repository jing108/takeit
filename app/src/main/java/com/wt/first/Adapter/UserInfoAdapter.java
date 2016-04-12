package com.wt.first.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wt.first.CustomView.CircleHead;
import com.wt.first.R;

import java.util.List;

/**
 * Created by jing107 on 2016/4/2 0002.
 */
public class UserInfoAdapter extends ArrayAdapter<UserInfo> {

    private int resourceId;

    public UserInfoAdapter(Context context, int resource, List<UserInfo> objects) {
        super(context, resource, objects);

        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserInfo info = getItem(position);

        ViewHolder holder;
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder = new ViewHolder();
            holder.image = (ImageView) view.findViewById(R.id.iv_isSelected);
            holder.head = (CircleHead) view.findViewById(R.id.ch_tx);
            holder.tvUserName = (TextView) view.findViewById(R.id.tv_username);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.head.setResource(info.getBitmap());
        holder.tvUserName.setText(info.getUserName());
        if (info.isBeSelected()) {
            holder.image.setBackgroundResource(R.drawable.icon_choosed);
        } else {
            holder.image.setBackgroundResource(R.drawable.icon_unchecked);
        }

        return view;
    }

    class ViewHolder {
        ImageView image;
        CircleHead head;
        TextView tvUserName;
    }
}
