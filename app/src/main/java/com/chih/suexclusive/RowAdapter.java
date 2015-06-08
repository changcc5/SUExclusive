package com.chih.suexclusive;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;

class RowAdapter extends ArrayAdapter<String> {

    public RowAdapter(Context context, String[] list) {
        super(context,R.layout.listrow, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.listrow, parent, false);

        String item = getItem(position);
        final TextView pkgText = (TextView) customView.findViewById(R.id.pkgName);
        pkgText.setText(item);

        final Spinner spinner = (Spinner) customView.findViewById(R.id.permissionSelector);
        final String[] selections = {"Deny", "Grant", "Exclusive"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,selections);

        spinner.setAdapter(spinAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int selected = spinner.getSelectedItemPosition();
                        Log.d("SUEX", "selections: " + pkgText.getText() + " " + selections[selected]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
            }
        );

        return customView;
    }
}
