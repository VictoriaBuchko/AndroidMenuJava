package com.example.myapplicationmenu;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    EditText editContent;
    private static final int MENU_EDIT        = 1;
    private static final int MENU_NOTIFY      = 2;
    private static final int REQUEST_CODE_NOTIFY = 100; // код запроса разрешения

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editContent = findViewById(R.id.editContent);
        MyNotificationManager.createNotificationChannel(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, "Редагувати текст")
                .setIcon(android.R.drawable.ic_menu_edit)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, MENU_NOTIFY, Menu.NONE, "Виклик сповіщення")
                .setIcon(android.R.drawable.ic_menu_send)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == MENU_EDIT) {
            showEditDialog();
            return true;
        }

        if (id == MENU_NOTIFY) {
            String text = editContent.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this,
                        "Спочатку введіть текст через меню «Редагувати текст»",
                        Toast.LENGTH_SHORT).show();
            } else {
                checkNotificationPermissionAndSend();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkNotificationPermissionAndSend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                sendNotification();
            } else {
                // Запрашиваем разрешение старым способом
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFY
                );
            }
        } else {
            sendNotification();
        }
    }

    // Обработка результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendNotification();
            } else {
                Toast.makeText(this,
                        "Дозвіл на сповіщення не надано",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotification() {
        String text = editContent.getText().toString().trim();
        MyNotificationManager.showNotification(this, "From my app", text);
    }

    private void showEditDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit, null);

        final EditText dialogEditText = dialogView.findViewById(R.id.dialogEditText);

        String current = editContent.getText().toString();
        if (!current.isEmpty()) {
            dialogEditText.setText(current);
            dialogEditText.setSelection(current.length());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редагування тексту");
        builder.setView(dialogView);

        builder.setPositiveButton("ОК", (dialog, which) -> {
            String entered = dialogEditText.getText().toString();
            editContent.setText(entered);
        });

        builder.setNegativeButton("СКАСУВАТИ", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }
}