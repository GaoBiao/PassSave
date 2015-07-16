package com.passsave;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.passsave.comment.AesUtils;
import com.passsave.comment.Md5Utils;
import com.passsave.database.DataBaseHelper;


public class SetPassActivity extends BaseActivity {

    private EditText oldPasswordTxt;
    private EditText newPasswordTxt;
    private EditText confirmPasswordTxt;
    private Button saveBtn;
    private Button cancelBtn;
    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pass);
        dataBaseHelper = new DataBaseHelper(this);
        oldPasswordTxt = (EditText) findViewById(R.id.txt_oldpassword);
        newPasswordTxt = (EditText) findViewById(R.id.txt_newpassword);
        confirmPasswordTxt = (EditText) findViewById(R.id.txt_confirmpassword);
        saveBtn = (Button) findViewById(R.id.btn_savepass);
        cancelBtn = (Button) findViewById(R.id.btn_cancelpass);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetPassActivity.this.finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordTxt.getText().toString();
                String newPassword = newPasswordTxt.getText().toString();
                String confirmPassword = confirmPasswordTxt.getText().toString();
                if (oldPassword.isEmpty()) {
                    Toast.makeText(SetPassActivity.this, "请输入当前密码", Toast.LENGTH_SHORT).show();
                    oldPasswordTxt.requestFocus();
                    return;
                }
                if (newPassword.isEmpty()) {
                    Toast.makeText(SetPassActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();
                    newPasswordTxt.requestFocus();
                    return;
                }
                if (confirmPassword.isEmpty()) {
                    Toast.makeText(SetPassActivity.this, "请输入确认密码", Toast.LENGTH_SHORT).show();
                    confirmPasswordTxt.requestFocus();
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(SetPassActivity.this, "确认密码与新密码不一致", Toast.LENGTH_SHORT).show();
                    confirmPasswordTxt.requestFocus();
                    return;
                }
                if (!pass.equals(oldPassword)) {
                    Toast.makeText(SetPassActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    oldPasswordTxt.requestFocus();
                    return;
                }
                updatePassword(oldPassword, newPassword);
                Toast.makeText(SetPassActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                SetPassActivity.this.finish();
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.query("t_userpass", null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            AesUtils aesUtils = new AesUtils();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String usernameEncrypted = cursor.getString(cursor.getColumnIndex("username"));
                String passwordEncrypted = cursor.getString(cursor.getColumnIndex("password"));
                String usernameDecrypted = aesUtils.decrypttoStr(usernameEncrypted, oldPassword);
                String passwordDecrypted = aesUtils.decrypttoStr(passwordEncrypted, oldPassword);
                db.execSQL("update t_userpass set username=?,password=? where _id=?", new Object[]{aesUtils.encrypttoStr(usernameDecrypted, newPassword), aesUtils.encrypttoStr(passwordDecrypted, newPassword), id});
            }
        }
        pass = newPassword;
        String passMD5 = Md5Utils.getMd5(newPassword);
        sp.edit().putString("passMD5", passMD5).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_pass, menu);
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
}
