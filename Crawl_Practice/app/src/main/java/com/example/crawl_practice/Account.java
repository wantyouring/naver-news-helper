package com.example.crawl_practice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Account extends AppCompatActivity {

    TextView tv_email;
    TextView tv_user_email;
    TextView tv_name;
    EditText et_name;

    String user_name;
    String user_email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        tv_email = findViewById(R.id.tv_email);
        tv_name = findViewById(R.id.tv_name);
        tv_user_email = findViewById(R.id.tv_user_email);
        et_name = findViewById(R.id.et_name);

        Intent getIntent = getIntent();
        user_email = getIntent.getExtras().getString("user_email");
        user_name = getIntent.getExtras().getString("user_name");

        tv_user_email.setText(user_email);
        et_name.setText(user_name);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);
        finish();
    }

    //변경 버튼
    public void change(View view) {
        user_name = et_name.getText().toString();
        //이름 변경(server)
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference();
        databaseReference.child("user_id").child(Util.EncodeString(user_email)).child("name").setValue(user_name);
        //변경 이름 main activity로 전송
        Intent intent = new Intent();
        intent.putExtra("user_name",user_name);
        setResult(RESULT_OK,intent);
        finish();
    }
}
