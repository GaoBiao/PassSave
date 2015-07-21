package com.passsave;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.google.zxing.client.android.CaptureActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.Where;
import com.passsave.comment.AesUtils;
import com.passsave.comment.HttpClientManager;
import com.passsave.database.DataBaseHelper;
import com.passsave.model.UserPass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity {

    //private static final String SERVER_URL = "http://192.168.15.22:8080/PassTrans/";
    private static final String SERVER_URL = "http://passtrans.aliapp.com/";
    private static final String FILE_NAME = "passsave.csv";
    public static final int SCAN_CODE = 1;
    public static final int EDIT_CODE = 2;
    private Dao userPassDao;
    private ListView listView;
    private long mExitTime;
    private AlertDialog selectDialog;
    private ProgressDialog progressDialog;
    private DrawerLayout drawerLayout;
    private ListView rightDrawer;
    private List<UserPass> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (sp.getString("passMD5", "").isEmpty()) {
            Intent intent = new Intent(MainActivity.this, InitPassActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }
        if (pass == null || pass.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }
        try {
            userPassDao = (new DataBaseHelper(this)).getDao(UserPass.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Button scanBtn = (Button) findViewById(R.id.btn_scan);
        scanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "删除");
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, UserPassActivity.class);
                UserPass userPass = list.get(position);
                intent.putExtra("id", userPass.getId());
                startActivityForResult(intent, EDIT_CODE);
            }
        });

        Button addBtn = (Button) findViewById(R.id.btn_add_pass);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserPassActivity.class);
                startActivityForResult(intent, EDIT_CODE);
            }
        });
        showList();
        Button setBtn = (Button) findViewById(R.id.btn_set);
        setBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        rightDrawer = (ListView) findViewById(R.id.right_drawer);
        String[] planetTitles = getResources().getStringArray(R.array.planets_array);
        rightDrawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, planetTitles));
        rightDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, SetPassActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        exportData();
                        break;
                    case 2:
                        importData();
                        break;
                    case 3:
                        upgrade();
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });
    }

    private void importData() {
        File sdCardDir = Environment.getExternalStorageDirectory();
        File file = new File(sdCardDir, FILE_NAME);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "SD卡" + FILE_NAME + "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        FileReader fr;
        BufferedReader bfr;
        try {
            fr = new FileReader(file);
            bfr = new BufferedReader(fr);
            String line = bfr.readLine();
            if (line != null && !line.isEmpty()) {
                String[] columns = line.split(",");
                int usernameIndex = -1;
                int passwordIndex = -1;
                int domainIndex = -1;
                int nameIndex = -1;
                int userIdIndex = -1;
                for (int i = 0; i < columns.length; i++) {
                    switch (columns[i]) {
                        case "username":
                            usernameIndex = i;
                            break;
                        case "password":
                            passwordIndex = i;
                            break;
                        case "domain":
                            domainIndex = i;
                            break;
                        case "name":
                            nameIndex = i;
                            break;
                        case "user_id":
                            userIdIndex = i;
                            break;
                        default:
                            break;
                    }
                }
                while ((line = bfr.readLine()) != null) {
                    String[] values = line.split(",");
                    String username;
                    if (usernameIndex != -1) {
                        username = values[usernameIndex];
                        if (username != null && !username.isEmpty()) {
                            UserPass userPass = new UserPass();
                            userPass.setUsername(username);
                            String domain = null;
                            if (passwordIndex != -1) {
                                userPass.setPassword(values[passwordIndex]);
                            }
                            if (domainIndex != -1) {
                                domain = values[domainIndex];
                                userPass.setDomain(domain);
                            }
                            if (nameIndex != -1) {
                                userPass.setName(values[nameIndex]);
                            }
                            if (userIdIndex != -1 && values[userIdIndex] != null) {
                                userPass.setUserId(Integer.parseInt(values[userIdIndex]));
                            }
                            Map<String, Object> map = new HashMap<>();
                            map.put("domain", domain);
                            map.put("username", username);
                            List list = userPassDao.queryForFieldValues(map);
                            if (list == null || list.isEmpty()) {
                                userPassDao.create(userPass);
                            }
                        }
                    }
                }
                showList();
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private void exportData() {
        File sdCardDir = Environment.getExternalStorageDirectory();
        final File file = new File(sdCardDir, FILE_NAME);
        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("SD卡中已存在文件" + FILE_NAME + ",确定要覆盖吗？");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    file.delete();
                    exportToFile(file);
                }
            });
            builder.create().show();
        } else {
            exportToFile(file);
        }
    }

    private void exportToFile(File file) {
        try {
            List<UserPass> list = userPassDao.queryForAll();
            if (list != null && !list.isEmpty()) {
                FileWriter fw;
                BufferedWriter bfw;
                fw = new FileWriter(file);
                bfw = new BufferedWriter(fw);
                bfw.write("username,password,domain,name,user_id");
                bfw.newLine();
                // 写入数据
                for (UserPass userPass : list) {
                    bfw.write(userPass.getUsername() + "," + userPass.getPassword() + "," + userPass.getDomain() + "," + userPass.getName() + "," + userPass.getUserId());
                    bfw.newLine();
                }
                bfw.flush();
                bfw.close();
                Toast.makeText(MainActivity.this, "数据成功导出到SD卡" + FILE_NAME + "文件,请妥善保存", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
    }

    private void showList() {
        try {
            String[] from = {"name", "domain"};
            list = userPassDao.queryForAll();
            List<Map<String, Object>> data = new ArrayList<>();
            for (UserPass userPass : list) {
                Map<String, Object> map = new HashMap();
                map.put("name", userPass.getName());
                map.put("domain", userPass.getDomain());
                data.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.list_item, from, new int[]{R.id.itemTextView, R.id.itemTextView2});
            listView.setAdapter(adapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    showList();
                }
                break;
            case SCAN_CODE:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("scan_result");
                    String[] resultArr = result.split(",");
                    if (resultArr.length != 4) {
                        Toast.makeText(MainActivity.this, "扫描结果格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String key = resultArr[0];
                    final String iv = resultArr[1];
                    final String domain = resultArr[2];
                    final String token = resultArr[3];
                    if (!key.isEmpty() && !iv.isEmpty() && !domain.isEmpty() && !token.isEmpty()) {
                        List<UserPass> list = null;
                        try {
                            list = userPassDao.queryForAll();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        final List<Map<String, String>> matches = new ArrayList();
                        if (list != null && !list.isEmpty()) {
                            for (UserPass userPass : list) {
                                String _domain = userPass.getDomain();
                                if (domain.indexOf(_domain) != -1) {
                                    Map<String, String> map = new HashMap<>();
                                    map.put("domain", _domain);
                                    map.put("name", userPass.getName());
                                    map.put("username", userPass.getUsername());
                                    map.put("password", userPass.getPassword());
                                    matches.add(map);
                                }
                            }
                            final AesUtils aesUtils = new AesUtils();
                            if (matches.size() == 1) {
                                Map<String, String> map = matches.get(0);
                                String usernameEncryped = map.get("username");
                                String passwordEncryped = map.get("password");

                                String username = aesUtils.decrypttoStr(usernameEncryped, pass);
                                String password = aesUtils.decrypttoStr(passwordEncryped, pass);
                                authorization(domain, username, password, key, iv, token);
                            } else if (matches.size() > 1) {
                                List<String> listData = new ArrayList<>();
                                for (Map<String, String> map : matches) {
                                    String usernameEncryped = map.get("username");
                                    String username = aesUtils.decrypttoStr(usernameEncryped, pass);
                                    listData.add(username);
                                }
                                selectDialog = showSelectDialog(listData, new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Map<String, String> map = matches.get(position);
                                        String usernameEncryped = map.get("username");
                                        String passwordEncryped = map.get("password");

                                        String username = aesUtils.decrypttoStr(usernameEncryped, pass);
                                        String password = aesUtils.decrypttoStr(passwordEncryped, pass);
                                        authorization(domain, username, password, key, iv, token);
                                        selectDialog.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "未找到匹配数据", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "未找到匹配数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (resultCode == RESULT_CANCELED) {

                }
                break;
            default:
                break;
        }
    }

    private void authorization(String domain, String username, String password, String key, String iv, String token) {
        progressDialog = ProgressDialog.show(MainActivity.this, "扫描完成", "加密并提交数据...", true, true);
        AesUtils aesUtils = new AesUtils();
        aesUtils.setIv(iv);
        String encrypted = Base64.encodeToString(aesUtils.encrypt((username + "," + password).getBytes(), key), Base64.NO_WRAP);
        HttpClientManager manager = new HttpClientManager(SERVER_URL + "passtrans", "pass=" + encrypted + "&token=" + token, "POST");
        manager.setHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HttpClientManager.COMPLETE) {
                    Bundle bundle = msg.getData();
                    if (bundle.getInt("status") == 200) {
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                    }
                }
                if (msg.what == HttpClientManager.ERROR) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
        manager.start();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            long l = System.currentTimeMillis();
            if ((l - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = l;
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        UserPass userPass = list.get(info.position);
        switch (item.getItemId()) {
            case 0:
                try {
                    userPassDao.deleteById(userPass.getId());
                    showList();
                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void upgrade() {
        HttpClientManager manager = new HttpClientManager(SERVER_URL + "version", null, "GET");
        manager.setHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HttpClientManager.COMPLETE) {
                    Bundle bundle = msg.getData();
                    if (bundle.getInt("status") == 200) {
                        try {
                            JSONObject json = new JSONObject(bundle.getString("result"));
                            PackageManager packageManager = getPackageManager();
                            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                            int localVersion = packageInfo.versionCode;
                            int serverVersion = json.getInt("versionCode");
                            if (localVersion < serverVersion) {
                                sendNotification(json.getString("upgradeUrl"));
                            } else {
                                Toast.makeText(MainActivity.this, "已经是最新版，无需更新", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                    }
                }
                if (msg.what == HttpClientManager.ERROR) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
            }
        });
        manager.start();
    }

    private void sendNotification(final String upgradeUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage("发现新版本，是否更新？");
        builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(upgradeUrl));
                startActivity(intent);
            }
        });
        builder.create().show();
    }
}
