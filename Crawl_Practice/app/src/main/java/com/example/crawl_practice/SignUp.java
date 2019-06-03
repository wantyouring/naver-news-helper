package com.example.crawl_practice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private EditText editTextAge;
    private RadioGroup radioGroupGender;

    private String email = "";
    private String password = "";
    private String name = "";
    private String age;
    private String gender = "";

    private DatabaseReference databaseReference =
            FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        editTextEmail = (EditText)findViewById(R.id.et_email);
        editTextPassword = (EditText)findViewById(R.id.et_password);
        editTextName = (EditText)findViewById(R.id.et_name);
        editTextAge = (EditText)findViewById(R.id.et_age);
        radioGroupGender = (RadioGroup) findViewById(R.id.radioGroup);

        //기존에 사용자가 입력하던 데이터 가져오기
        Intent getintent = getIntent();
        email = getintent.getExtras().getString("email");
        password = getintent.getExtras().getString("password");
        editTextEmail.setText(email);
        editTextPassword.setText(password);

        firebaseAuth = FirebaseAuth.getInstance();

    }

    //회원가입
    public void singUp(View view) {
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        name = editTextName.getText().toString();
        age = editTextAge.getText().toString();
        int id = radioGroupGender.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton)findViewById(id);
        gender = radioButton.getText().toString();

        //계정 생성, user database에 정보 저장
        createUser(email.replaceAll(" ",""),password,
                Integer.parseInt(age),gender,name);

    }

    // 계정 생성
    private void createUser(final String email, final String password, final int age, final String gender, final String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공. database에 새 회원정보 추가하기
                            User user = new User(age, gender, name);
                            databaseReference.child("user_id").child(Util.EncodeString(email)).setValue(user);
                            Toast.makeText(SignUp.this, R.string.success_signup, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra("email",email);
                            intent.putExtra("password",password);

                            setResult(RESULT_OK,intent);
                            finish();
                        } else {
                            // 회원가입 실패
                            Toast.makeText(SignUp.this, R.string.failed_signup, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
