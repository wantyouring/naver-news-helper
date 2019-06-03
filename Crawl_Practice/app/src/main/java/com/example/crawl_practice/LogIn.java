package com.example.crawl_practice;

import android.Manifest;
import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LogIn extends AppCompatActivity {

    static final int REQUEST_SIGN_UP = 1;

    // 비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{6,16}$");

    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox autoLoginCheckBox;

    private String email = "";
    private String password = "";

    SharedPreferences auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);
        requirePermission();//시작할 때 필요한 권한 요청

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        autoLoginCheckBox = findViewById(R.id.checkBox);

        // 자동 로그인
        auto = getSharedPreferences("auto", MODE_PRIVATE);
        Boolean autoCheck = auto.getBoolean("autoCheck",false);
        email = auto.getString("email",null);
        password = auto.getString("password",null);

        if(autoCheck) {
            autoLoginCheckBox.setChecked(true);
            editTextEmail.setText(email);
            editTextPassword.setText(password);
            //@@@@@@@@@@@@자동 로그인 후 액티비티로 이동 추가@@@@@@@
        } else {
            if (ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "번호 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            editTextEmail.setText(getPhoneNumber(LogIn.this)+"@naver.com");
        }
    }

    //핸드폰 번호 가져오기
    @RequiresPermission(permission.READ_PHONE_STATE)
    public static String getPhoneNumber(Context context){

        String phoneNumber = "";

        TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            String tmpPhoneNumber = mgr.getLine1Number();
            phoneNumber = tmpPhoneNumber.replace("+82", "0");
        } catch (Exception e) {
            phoneNumber = "";
        }
        return phoneNumber;
    }

    //필요한 permission 허가
    void requirePermission() {
        String[] permissions = new String[] {permission.READ_PHONE_STATE, permission.INTERNET};//필요한 권한 여기에 추가
        ArrayList<String> listPermissionNeeded = new ArrayList<>();
        for(String permission : permissions) {
            // 권한이 허가 안됐을 경우 요청할 권한 모집.
            if(ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_DENIED) {
                listPermissionNeeded.add(permission);
            }
        }
        //Toast.makeText(this,listPermissionNeeded.size()+"",Toast.LENGTH_SHORT).show();
        if(!listPermissionNeeded.isEmpty()){
            //권한 요청하는 부분
            ActivityCompat.requestPermissions(this,listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),1);
        }
    }

    //회원가입 액티비티로 이동
    public void singUp(View view) {
        Intent intent = new Intent(getApplicationContext(),SignUp.class);
        intent.putExtra("email",editTextEmail.getText().toString());
        intent.putExtra("password",editTextPassword.getText().toString());
        startActivityForResult(intent,REQUEST_SIGN_UP);
    }

    //회원가입 완료 이후
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show();
            return;
        }
        if(requestCode == REQUEST_SIGN_UP) {
            String rec_email = data.getStringExtra("email");
            String rec_password = data.getStringExtra("password");
            editTextEmail.setText(rec_email);
            editTextPassword.setText(rec_password);
            //Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show();
        }
    }

    //로그인 양식 확인, 로그인 하기
    public void signIn(View view) {
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();

        if(isValidEmail() && isValidPasswd()) {
            loginUser(email, password);
        } else {
            Toast.makeText(this, "이메일,패스워드 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 이메일 유효성 검사
    private boolean isValidEmail() {
        if (email.isEmpty()) {
            // 이메일 공백
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 유효성 검사
    private boolean isValidPasswd() {
        if (password.isEmpty()) {
            // 비밀번호 공백
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 로그인
    private void loginUser(final String email, final String password)
    {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            // 자동로그인 체크면 해당 데이터들 저장.
                            SharedPreferences.Editor autoLogin = auto.edit();
                            if(autoLoginCheckBox.isChecked()) {
                                autoLogin.putBoolean("autoCheck",true);
                                autoLogin.putString("email",email);
                                autoLogin.putString("password",password);
                                //Toast.makeText(LogIn.this, "자동로그인 저장", Toast.LENGTH_SHORT).show();
                            } else {
                                autoLogin.putBoolean("autoCheck",false);
                                //Toast.makeText(LogIn.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                            }
                            autoLogin.apply();
                            Intent intent = new Intent();
                            intent.putExtra("email",email);
                            setResult(RESULT_OK,intent);
                            finish();
                        } else {
                            // 로그인 실패
                            Toast.makeText(LogIn.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}