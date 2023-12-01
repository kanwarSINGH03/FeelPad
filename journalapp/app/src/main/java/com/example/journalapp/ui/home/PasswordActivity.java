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
        NoteDatabase db = NoteDatabase.getNoteDatabase(getApplicationContext());
        folderDao = db.noteDao();
    }

    public void showPasswordDialog(Folder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        if (folder.getEncrypted()) {
            builder.setTitle("Decrypt Folder");
            builder.setPositiveButton("Decrypt", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                if (enteredPassword.equals(folder.getPassword())) {
                    folder.setIsEncrypted(false);
                    folder.setPassword(null);
                    updateFolderInDatabase(folder);
                } else {

                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setTitle("Encrypt Folder");
            builder.setPositiveButton("Encrypt", (dialog, which) -> {
                String Password = input.getText().toString();
                folder.setIsEncrypted(true);
                folder.setPassword(Password);
                updateFolderInDatabase(folder);
            });
        }
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void updateFolderPassword(Folder folder, String newPassword) {
        String Password = newPassword;
        folder.setPassword(Password);
        updateFolderInDatabase(folder);
    }

    private void updateFolderInDatabase(Folder folder) {
        new Thread(()-> {
            folderDao.updateFolder(folder);
        }).start();
    }
}

