package com.passsave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.j256.ormlite.dao.Dao;
import com.passsave.comment.AesUtils;
import com.passsave.database.DataBaseHelper;
import com.passsave.model.UserPass;

import java.sql.SQLException;


public class UserPassActivity extends BaseActivity {
    private Dao userPassDao;
    private EditText domainTxt;
    private EditText usernameTxt;
    private EditText passwordTxt;
    private EditText nameTxt;
    private CheckBox checkBox;
    private int id;
    private AlertDialog inputPassDialog;
    public static final int SCAN_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pass);
        try {
            userPassDao = (new DataBaseHelper(this)).getDao(UserPass.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        domainTxt = (EditText) findViewById(R.id.domainTxt);
        usernameTxt = (EditText) findViewById(R.id.usernameTxt);
        passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        nameTxt = (EditText) findViewById(R.id.nameTxt);
        fillEditText();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String domain = domainTxt.getText().toString();
                String username = usernameTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                String name = nameTxt.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(UserPassActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (domain.isEmpty()) {
                    Toast.makeText(UserPassActivity.this, "请输入域名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (username.isEmpty()) {
                    Toast.makeText(UserPassActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(UserPassActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                AesUtils aesUtils = new AesUtils();
                UserPass userPass = new UserPass();
                userPass.setName(name);
                userPass.setDomain(domain);
                userPass.setUsername(aesUtils.encrypttoStr(username, pass));
                userPass.setPassword(aesUtils.encrypttoStr(password, pass));
                userPass.setId(id);
                try {
                    if (id > 0) {
                        userPassDao.update(userPass);
                    } else {
                        userPassDao.create(userPass);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkBox = (CheckBox) findViewById(R.id.checkBox_showpass);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    passwordTxt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        ((Button) (findViewById(R.id.scanBtn))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserPassActivity.this, CaptureActivity.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });
    }

    private void fillEditText() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        if (id > 0) {
            if (pass == null || pass.isEmpty()) {
                inputPassDialog = showInputPassDialog(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText passTxt = (EditText) v.getRootView().findViewById(R.id.txt_password);
                        pass = passTxt.getText().toString();
                        if (pass.isEmpty()) {
                            Toast.makeText(UserPassActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        inputPassDialog.dismiss();
                        fillEditText();
                    }
                }, null);
                return;
            }
            try {
                AesUtils aesUtils = new AesUtils();
                UserPass userPass = (UserPass) userPassDao.queryForId(id);
                nameTxt.setText(userPass.getName());
                domainTxt.setText(userPass.getDomain());
                usernameTxt.setText(aesUtils.decrypttoStr(userPass.getUsername(), pass));
                passwordTxt.setText(aesUtils.decrypttoStr(userPass.getPassword(), pass));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCAN_CODE:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("scan_result");
                    String[] resultArr = result.split(",");
                    if (resultArr.length > 0 && !resultArr[0].isEmpty()) {
                        nameTxt.setText(resultArr[0]);
                    }
                    if (resultArr.length > 1 && !resultArr[1].isEmpty()) {
                        domainTxt.setText(resultArr[1]);
                    }
                    if (resultArr.length > 2 && !resultArr[2].isEmpty()) {
                        usernameTxt.setText(resultArr[2]);
                    }
                    if (resultArr.length > 3 && !resultArr[3].isEmpty()) {
                        passwordTxt.setText(resultArr[3]);
                    }
                }
                break;
            default:
                break;
        }
    }
}
