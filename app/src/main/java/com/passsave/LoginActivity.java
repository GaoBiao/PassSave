package com.passsave;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.passsave.comment.Md5Utils;


public class LoginActivity extends BaseActivity {

    private EditText passTxt;
    private long prevClickTime;
    private long lockTime;
    private int errorTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        passTxt = (EditText) findViewById(R.id.pass_txt);
        Button loginBtn = (Button) findViewById(R.id.btn_login);
        errorTimes = 3;
        lockTime = sp.getLong("lockTime", 0L);
        prevClickTime = 0L;
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                //防止点击过于频繁
                if ((currentTime - prevClickTime) < 3000) {
                    return;
                }
                //锁定时间内不能登录
                if (currentTime < lockTime) {
                    Toast.makeText(LoginActivity.this, "请于" + ((lockTime - currentTime) / 1000) + "秒后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = passTxt.getText().toString();
                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                String passMD5 = Md5Utils.getMd5(password);
                if (passMD5.equals(sp.getString("passMD5", ""))) {
                    pass = password;
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    errorTimes--;
                    if (errorTimes <= 0) {
                        lockTime = currentTime + 100000;
                        sp.edit().putLong("lockTime", lockTime).commit();
                    }
                }
                prevClickTime = currentTime;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
