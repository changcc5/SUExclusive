//package com.chih.suexclusive;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.*;
//import android.widget.*;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//
//class RowAdapter extends ArrayAdapter<String> {
//
//    private ArrayList<PackagePermission> pkgList;
//
//    private class ConfigReader {
//        private Context context;
//        public ConfigReader(Context context) {
//            this.context = context;
//        }
//
//        public void readConfig() {
//            try {
//                FileInputStream is = new FileInputStream(new File(context.getFilesDir(), "su.cfg"));
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                PackagePermission pkg = new PackagePermission();
//
//                while(reader.ready()) {
//                    String line = reader.readLine();
//                    Log.d("SUEX", "InitUI: " + line);
//                    if (line.startsWith("[")) {
//                        pkg.name = line;
//                    }
//                    else if (line.startsWith("uid")) {
//                        pkg.uid = line;
//                    }
//                    else if (line.startsWith("access")) {
//                        pkg.permission = line;
//                    }
//                    else if (line.isEmpty()) {
//                        pkgList.add(pkg);
//                        pkg = new PackagePermission();
//                    }
//                }
//                reader.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//    public RowAdapter(Context context, String[] list) {
//        super(context,R.layout.listrow, list);
//        pkgList = new ArrayList<PackagePermission>();
//        ConfigReader reader = new ConfigReader(context);
//        reader.readConfig();
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        View customView = inflater.inflate(R.layout.listrow, parent, false);
//
//        String item = getItem(position);
//        final TextView pkgText = (TextView) customView.findViewById(R.id.pkgName);
//        pkgText.setText(item);
//
//        final Spinner spinner = (Spinner) customView.findViewById(R.id.permissionSelector);
//        final String[] selections = {"Deny", "Grant", "Exclusive"};
//        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,selections);
//
//        spinner.setAdapter(spinAdapter);
//        spinner.setOnItemSelectedListener(
//                new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        int selected = spinner.getSelectedItemPosition();
//                        Log.d("SUEX", "selections: " + pkgText.getText() + " " + selections[selected]);
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//
//                    }
//            }
//        );
//
//        return customView;
//    }
//}
