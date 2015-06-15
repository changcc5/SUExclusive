package com.chih.suexclusive;

import android.view.*;
import android.widget.*;
import android.widget.BaseAdapter;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
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

        //TODO check if lock exist
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.listrow, parent, false);


        File inputFile = new File(ctx.getFilesDir(),"euFiles");
        ArrayList<String> euList = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                euList.add(trimmedLine);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView pkgName = (TextView) row.findViewById(R.id.pkgName);
        TextView permission = (TextView) row.findViewById(R.id.permission);
        PackagePermission currItem = listItem.get(position);
        if (!currItem.name.contains("default") && !currItem.name.contains("suexclusive")) {
            pkgName.setText(currItem.name);
            String perm = "";
            if (currItem.permission.contains("0")) {
                perm = "Permission: Denied";
            } else if (currItem.permission.contains("1")) {
                perm = "Permission: Granted";
                for (String p : euList) {
                    if (p.contains(currItem.name)) {
                        perm = "Permission: Exclusive";
                    }
                }
            }
            permission.setText(perm);

        } else {
            pkgName.setVisibility(View.GONE);
            permission.setVisibility(View.GONE);
        }
        return row;
    }
}
