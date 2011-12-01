package com.allrightsms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allrightsms.shared.SmsProxy;

public class SmsAdapter extends BaseAdapter {

    private final static class ViewHolder {
        TextView title;
        TextView date;
    }

    private Comparator<SmsProxy> smsComparator = new Comparator<SmsProxy>() {
        public int compare(SmsProxy object1, SmsProxy object2) {
            Date date1 = object1.getDueDate();
            Date date2 = object2.getDueDate();
            if (date1 != null) {
                if (date2 != null) {
                    return date1.compareTo(date2);
                } else {
                    return -1;
                }
            } else {
                if (date2 != null) {
                    return 1;
                }
            }
            return 0;
        }
    };

    private final List<SmsProxy> items = new ArrayList<SmsProxy>();
    private final LayoutInflater inflater;

    private java.text.DateFormat dateFormat;

    public SmsAdapter(Context context) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        dateFormat = DateFormat.getDateFormat(context);
    }

    public void setTasks(List<SmsProxy> items) {
        this.items.clear();
        this.items.addAll(items);
        Collections.sort(this.items, smsComparator);
    }

    public void addTasks(List<SmsProxy> items) {
        this.items.addAll(items);
        Collections.sort(this.items, smsComparator);
    }

    public SmsProxy get(int position) {
        return items.get(position);
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup view) {
  /*      ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.taskTitle);
            holder.date = (TextView) convertView.findViewById(R.id.taskDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TaskProxy task = items.get(position);

        holder.title.setText(task.getName());
        Date dueDate = task.getDueDate();
        if (dueDate != null) {
            holder.date.setText(dateFormat.format(dueDate));
        }

        return convertView;
       */
    	return null;
    }
}
