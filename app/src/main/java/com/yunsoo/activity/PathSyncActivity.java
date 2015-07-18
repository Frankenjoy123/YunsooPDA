package com.yunsoo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.yunsoo.adapter.FileSyncAdapter;
import com.yunsoo.exception.BaseException;
import com.yunsoo.exception.ServerAuthException;
import com.yunsoo.manager.DeviceManager;
import com.yunsoo.manager.FileManager;
import com.yunsoo.manager.SQLiteManager;
import com.yunsoo.manager.SessionManager;
import com.yunsoo.service.DataServiceImpl;
import com.yunsoo.service.FileUpLoadService;
import com.yunsoo.service.PermanentTokenLoginService;
import com.yunsoo.sqlite.MyDataBaseHelper;
import com.yunsoo.util.Constants;
import com.yunsoo.view.TitleBar;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class PathSyncActivity extends BaseActivity implements DataServiceImpl.DataServiceDelegate {
    private MyDataBaseHelper dataBaseHelper;
    private ListView lv_path_sync;
    private TitleBar titleBar;
    private TextView tv_empty_file_tip;
    private FileSyncAdapter adapter;

    //    private int minIndex;
    private int maxIndex;

    private List<String> fileNames;
    private List<Integer> status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_sync);

        getActionBar().hide();

        init();

        createFile();

        getPackFileNames();

    }


    private void init() {
        lv_path_sync = (ListView) findViewById(R.id.lv_path_sync);
        tv_empty_file_tip= (TextView) findViewById(R.id.tv_path_empty_tip);
        titleBar= (TitleBar) findViewById(R.id.path_sync_title_bar);
        titleBar.setTitle(getString(R.string.path_sync));
        titleBar.setMode(TitleBar.TitleBarMode.BOTH_BUTTONS);
        titleBar.setDisplayAsBack(true);
        titleBar.setRightButtonText(getString(R.string.sync));
        adapter=new FileSyncAdapter(this);
        fileNames=new ArrayList<>();
        status=new ArrayList<>();
        adapter.setFileNames(fileNames);
        adapter.setStatus(status);

        lv_path_sync.setAdapter(adapter);


        titleBar.setOnRightButtonClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileNames!=null&&fileNames.size()>0){
                    String[] titleArray = new String[]{getString(R.string.off_line_upload),
                            getString(R.string.wifi_upload)};
                    AlertDialog dialog = new AlertDialog.Builder(PathSyncActivity.this)
                            .setItems(titleArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i==0){
                                        Intent intent=new Intent(PathSyncActivity.this,OffLineUploadActivity.class);
                                        startActivity(intent);
                                    }
                                    else if(i==1){
                                        uploadFiles();
                                    }
                                }
                            })
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.cancel, null).create();
                    dialog.show();
                }
            }
        });
    }

    private void uploadFiles() {
        showLoading();
        String folderName = android.os.Environment.getExternalStorageDirectory() +
                Constants.YUNSOO_FOLDERNAME+Constants.PATH_SYNC_TASK_FOLDER;
        File pack_task_folder = new File(folderName);
        File[] files=pack_task_folder.listFiles();
        for(int i=0;i<files.length;i++){
            status.set(i,1);
            FileUpLoadService service=new FileUpLoadService(files[i].getAbsolutePath());
            service.setFileType(FileUpLoadService.PATH_FILE);
            service.setIndex(i);
            service.setDelegate(this);
            service.start();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestSucceeded(final DataServiceImpl service, JSONObject data, boolean isCached) {
        super.onRequestSucceeded(service, data, isCached);
        if (service instanceof FileUpLoadService){
            String folderName = android.os.Environment.getExternalStorageDirectory() +
                    Constants.YUNSOO_FOLDERNAME+Constants.PATH_SYNC_SUCCESS_FOLDER;
            File path_success_folder = new File(folderName);
            if (!path_success_folder.exists()){
                path_success_folder.mkdirs();
            }
            File oldFile=new File(((FileUpLoadService) service).getFilePath());
            File newFile=new File(path_success_folder,oldFile.getName());
            oldFile.renameTo(newFile);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int index=((FileUpLoadService) service).getIndex();
                    status.set(index,2);
                    adapter.notifyDataSetChanged();
                    if (index==status.size()-1){
                        hideLoading();
                    }
                }
            });
        }

        if (service instanceof PermanentTokenLoginService){
            String folderName = android.os.Environment.getExternalStorageDirectory() +
                    Constants.YUNSOO_FOLDERNAME+Constants.PATH_SYNC_TASK_FOLDER;
            File path_task_folder = new File(folderName);
            File[] files=path_task_folder.listFiles();
            for(int i=0;i<files.length;i++){
                status.set(i,1);
                FileUpLoadService fileUpLoadService=new FileUpLoadService(files[i].getAbsolutePath());
                fileUpLoadService.setIndex(i);
                fileUpLoadService.setDelegate(this);
                fileUpLoadService.start();
            }
        }
    }

    @Override
    public void onRequestFailed(DataServiceImpl service, BaseException exception) {
//        super.onRequestFailed(service, exception);
        if (exception instanceof ServerAuthException){
            PermanentTokenLoginService service1=new PermanentTokenLoginService(SessionManager.getInstance().
                    getAuthUser().getPermanent_token());
            service1.setDelegate(this);
            service1.start();
        }
    }

    private void getPackFileNames() {
        try {
            String folderName = android.os.Environment.getExternalStorageDirectory() +
                    Constants.YUNSOO_FOLDERNAME+Constants.PATH_SYNC_TASK_FOLDER;
            File pack_task_folder = new File(folderName);
            String[] packFiles= pack_task_folder.list();
            if (packFiles!=null&&packFiles.length>0){
                for (int i=0;i<packFiles.length;i++){
                    fileNames.add(packFiles[i]);
                    status.add(0);
                }
                lv_path_sync.setEmptyView(tv_empty_file_tip);
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        dataBaseHelper=new MyDataBaseHelper(this, Constants.SQ_DATABASE,null,1);
        Cursor cursor= null;
        try {
            cursor = dataBaseHelper.getReadableDatabase().rawQuery("select * from path where _id>?",
                    new String[]{String.valueOf(SQLiteManager.getInstance().getPathLastId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor!=null&&cursor.getCount()>0){
/**
 DeviceCode:ffffffff-bb69-7f45-bbb6-c7f60033c587
 2,6cekDjJ3TWKAzN-ZW7X9gx,2015-07-14T11:12:16
 2,6cekDjJ3TWKAzN-ZW7X9gx,2015-07-14T11:12:16
 2,6cekDjJ3TWKAzN-ZW7X9gx,2015-07-14T11:12:16
 2,6cekDjJ3TWKAzN-ZW7X9gx,2015-07-14T11:12:16
 */
            StringBuilder builder=new StringBuilder("DeviceCode:");
            builder.append(DeviceManager.getInstance().getDeviceId());
            builder.append("\r\n");
/**
 *     final String CREATE_PATH_TABLE_SQL =
 "create table path(_id integer primary key autoincrement , pack_key, action_id integer,last_save_time)";
 */
            while (cursor.moveToNext()){
                if (cursor.isLast()){
                    maxIndex=cursor.getInt(0);
                    SQLiteManager.getInstance().savePathLastId(maxIndex);
                }
                builder.append(cursor.getString(2)); 
                builder.append(",");
                builder.append(cursor.getString(1));
                builder.append(",");
                builder.append(cursor.getString(3));
                builder.append("\r\n");
            }
            
            dataBaseHelper.close();

            try {

                String folderName = android.os.Environment.getExternalStorageDirectory() +
                        Constants.YUNSOO_FOLDERNAME+Constants.PATH_SYNC_TASK_FOLDER;
                File path_task_folder = new File(folderName);
                if (!path_task_folder.exists())
                    path_task_folder.mkdirs();

                StringBuilder fileNameBuilder=new StringBuilder("Path_");
                fileNameBuilder.append(DeviceManager.getInstance().getDeviceId());
                fileNameBuilder.append("_");
                fileNameBuilder.append(FileManager.getInstance().getPathFileLastIndex() + 1);
                fileNameBuilder.append(".txt");

                File file=new File(path_task_folder,fileNameBuilder.toString());

                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

                bw.write(builder.toString());
                bw.flush();

                FileManager.getInstance().savePathFileIndex(FileManager.getInstance().getPathFileLastIndex() + 1);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}