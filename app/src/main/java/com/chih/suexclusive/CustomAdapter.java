package com.chih.suexclusive;

import android.view.*;
import android.widget.*;
import android.widget.BaseAdapter;
import android.content.Context;

import java.util.ArrayList;
public class CustomAdapter extends BaseAdapter{
    ArrayList<PackagePermission> listItem;
    Context ctx;

    public CustomAdapter(Context ctx, ArrayList<PackagePermission> listItem) {
        this.ctx = ctx;
        this.listItem = listItem;
    }
    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.listrow, parent, false);

        TextView pkgName = (TextView) row.findViewById(R.id.pkgName);
        TextView permission = (TextView) row.findViewById(R.id.permission);
        PackagePermission currItem = listItem.get(position);
        pkgName.setText(currItem.name);
        String perm = "";
        if (currItem.permission.contains("0")) {
            perm = "Permission: Denied";
        }
        else if (currItem.permission.contains("1")) {
            perm = "Permission: Granted";
        }
        else if (currItem.permission.contains("2")) {
            perm = "Permission: Exclusive";
        }
        permission.setText(perm);


        return row;
    }
}
