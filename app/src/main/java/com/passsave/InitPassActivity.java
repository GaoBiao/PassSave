package com.passsave;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.passsave.comment.Md5Utils;


public class InitPassActivity extends BaseActivity {
    private EditText passTxt1;
    private EditText passTxt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_pass);
        passTxt1 = (EditText) findViewById(R.id.pass_txt1);
        passTxt2 = (EditText) findViewById(R.id.pass_txt2);
        Button okBtn = (Button) findViewById(R.id.btn_ok);
        String localPassMd5 = sp.getString("passMD5","");
        if(!localPassMd5.isEmpty()){
            Intent intent = new Intent(InitPassActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass1 = passTxt1.getText().toString();
                String pass2 = passTxt2.getText().toString();
                if (pass1.isEmpty()) {
                    Toast.makeText(InitPassActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass2.isEmpty()) {
                    Toast.makeText(InitPassActivity.this, "请输入确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass2.equals(pass1)) {
                    Toast.makeText(InitPassActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                pass = pass1;
                String passMD5 = Md5Utils.getMd5(pass);
                sp.edit().putString("passMD5", passMD5).commit();
                Intent intent = new Intent(InitPassActivity.this, MainActivity.class);
                startActivity(intent);
                InitPassActivity.this.finish();
            }
        });
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
