package com.example.journalapp.ui.home;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.database.NoteDao;
import com.example.journalapp.database.NoteDatabase;
import com.example.journalapp.database.entity.Folder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordActivity extends AppCompatActivity {
    private NoteDao folderDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化数据库和 DAO
        NoteDatabase db = NoteDatabase.getNoteDatabase(getApplicationContext());
        folderDao = db.noteDao();
    }

    public void showEncryptionDialog(Folder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        if (folder.getEncrypted()) {
            builder.setTitle("Decrypt Folder");
            builder.setPositiveButton("Decrypt", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                if (hashPassword(enteredPassword).equals(folder.getEncryptionPasswordHash())) {
                    // 密码匹配，解除加密
                    folder.setIsEncrypted(false);
                    folder.setEncryptionPasswordHash(null);
                    // 更新文件夹到数据库
                    updateFolderInDatabase(folder);
                } else {
                    // 密码不匹配
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setTitle("Encrypt Folder");
            builder.setPositiveButton("Encrypt", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                String hashedPassword = hashPassword(enteredPassword);
                folder.setIsEncrypted(true);
                folder.setEncryptionPasswordHash(hashedPassword);
                // 更新文件夹到数据库
                updateFolderInDatabase(folder);
            });
        }
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public void updateFolderPassword(Folder folder, String newPassword) {
        String hashedPassword = hashPassword(newPassword); // 假设您有一个方法来哈希密码
        folder.setEncryptionPasswordHash(hashedPassword);
        updateFolderInDatabase(folder); // 调用更新方法
    }

    private void updateFolderInDatabase(Folder folder) {
        new Thread(()-> {
            folderDao.updateFolder(folder);
        }).start();
    }
}

