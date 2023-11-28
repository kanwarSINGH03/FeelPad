package com.example.journalapp.ui.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewAllFoldersActivity extends AppCompatActivity {

    private FolderViewModel folderViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton addFolderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_list); // 使用包含RecyclerView的布局文件
        recyclerView = findViewById(R.id.folderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton addFolderButton = findViewById(R.id.add_folder_button);
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddFolderDialog();
            }
        });

        // 初始化 Adapter
        FolderListAdapter adapter = new FolderListAdapter(new FolderListAdapter.FolderDiff());
        recyclerView.setAdapter(adapter);
        // 初始化 ViewModel
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllFolders().observe(this, folders -> {
            // 更新 Adapter 的数据
            adapter.submitList(folders);
        });
    }

    private void openAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Folder");

        // 设置一个输入框
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // 设置对话框的按钮
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString();
                createNewFolder(folderName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewFolder(String folderName) {
        // 获取当前日期，格式化为字符串
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 创建新的 Folder 对象
        Folder newFolder = new Folder(folderName, currentDate, R.drawable.ic_folder_flag, R.color.colorAccentGrey);
        // ...
        // 将新文件夹保存到数据库
        folderViewModel.CreateFolder(newFolder);
    }
}

