package com.passsave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by gaobiao on 2015/6/25.
 */
public class BaseActivity extends Activity {
    protected static String pass;
    protected SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("data", 0);
    }

    public AlertDialog showInputPassDialog(View.OnClickListener onOkClick, View.OnClickListener onCancelClick) {
        final AlertDialog inputPassDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.input_password_dialog, null);
        builder.setTitle("输入密码");
        builder.setView(view);
        inputPassDialog = builder.create();
        Button btnOk = (Button) view.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnOk.setOnClickListener(onOkClick);
        if (onCancelClick != null) {
            btnCancel.setOnClickListener(onCancelClick);
        } else {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputPassDialog.dismiss();
                }
            });
        }
        inputPassDialog.show();
        return inputPassDialog;
    }

    protected AlertDialog showSelectDialog(List<String> data, AdapterView.OnItemClickListener onItemClickListener) {
        AlertDialog selectDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.select_dialog, null);
        ListView listView = (ListView) view.findViewById(R.id.listView_select);

        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
        if (onItemClickListener != null) {
            listView.setOnItemClickListener(onItemClickListener);
        }
        builder.setTitle("选择账号");
        builder.setView(view);
        selectDialog = builder.create();
        selectDialog.show();
        return selectDialog;
    }

}
