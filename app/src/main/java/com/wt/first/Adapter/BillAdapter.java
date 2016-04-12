package com.wt.first.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wt.first.R;

import java.util.List;

/**
 * Created by jing107 on 2016/3/31 0031.
 */
public class BillAdapter extends ArrayAdapter<Bill> {

    private int resourceId;

    public BillAdapter(Context context, int resource, List<Bill> objects) {
        super(context, resource, objects);

        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bill bill = getItem(position);

        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();

            viewHolder.date = (TextView) view.findViewById(R.id.tv_one_bill_date);
            viewHolder.time = (TextView) view.findViewById(R.id.tv_one_bill_time);
            viewHolder.money = (TextView) view.findViewById(R.id.tv_one_bill_money);
            viewHolder.content = (TextView) view.findViewById(R.id.tv_one_bill_content);
            viewHolder.type = (ImageView) view.findViewById(R.id.bill_type);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.date.setText(bill.getmDate());
        viewHolder.time.setText(bill.getmTime());
        viewHolder.money.setText(bill.getMoney());
        viewHolder.content.setText(bill.getContent());
        viewHolder.type.setBackgroundResource(bill.getId());

        return view;
    }

    class ViewHolder {
        TextView date;
        TextView time;
        TextView money;
        TextView content;
        ImageView type;
    }
}
