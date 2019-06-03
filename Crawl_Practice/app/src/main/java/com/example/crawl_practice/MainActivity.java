package com.example.crawl_practice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static Context context;  //PageAdapter dialog에서 context 참조하기 위해서.
    static final int REQUEST_LOG_IN = 1;
    static final int REQUEST_ACCOUNT = 2;
    static final int REQUEST_SCRAP = 3;

    //객체 생성

    PageAdapter adapter;
    ViewPager viewPager;
    TextView forTest;
    TextView comments;
    ImageView emoticon;
    Toolbar myToolbar;
    TextView part;
    SpringDotsIndicator springDotsIndicator;
    //navigation drawer
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    TextView nv_name;
    TextView nv_email;


    //firebase
    private DatabaseReference databaseReference =
            FirebaseDatabase.getInstance().getReference();


    Document doc;

    ArrayList<String> items = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    String imagelinks[] = new String[60];

    String now_part = "정치";
    static String last_selected_part;
    String user_email;
    String user_name;
    String user_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //id저장
        forTest = (TextView)findViewById(R.id.forTest);
        comments = (TextView)findViewById(R.id.comments);
        comments.setMovementMethod(new ScrollingMovementMethod());
        emoticon = (ImageView)findViewById(R.id.emoticon);
        myToolbar = (Toolbar)findViewById(R.id.toolbar);
        part = (TextView)findViewById(R.id.part);
        springDotsIndicator = (SpringDotsIndicator)findViewById(R.id.spring_dots_indicator);
        navigationView = (NavigationView)findViewById(R.id.navigationView) ;
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);

        //분야
        part.setText(now_part);
        //기사성향 textview
        forTest.setTypeface(forTest.getTypeface(), Typeface.BOLD);
        //actionbar(toolbar)
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_view_headline);
        //navigation bar
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);
        nv_name = (TextView)nav_header_view.findViewById(R.id.name);
        nv_email = (TextView)nav_header_view.findViewById(R.id.email);
        //생성 동시에 파싱해 뉴스 데이터 가져오기.
        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();

        Intent intent = new Intent(getApplicationContext(),LogIn.class);
        startActivityForResult(intent,REQUEST_LOG_IN);
    }

    //다른 Activity에서 넘어와 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_LOG_IN && resultCode != RESULT_OK) {
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //Login activity에서 넘어옴
        if(requestCode == REQUEST_LOG_IN && resultCode == RESULT_OK) {
            //로그인 성공
            user_email = data.getStringExtra("email");
            databaseReference.child("user_id").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d("파싱test",Util.DecodeString(snapshot.getKey()));
                        if(user_email.equals(Util.DecodeString(snapshot.getKey()))) { //사용자 이메일 데이터 일치하면
                            user_data = snapshot.getValue().toString(); //사용자 기타 데이터 저장
                            user_name = user_data.split("name=")[1].split(",")[0];
                        }
                    }
                    nv_email.setText(user_email);
                    nv_name.setText(user_name);
                    Toast.makeText(context, user_name + "님 로그인 성공", Toast.LENGTH_SHORT).show();
                    Log.d("파싱", user_data);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //Account activity에서 넘어옴
        if(requestCode == REQUEST_ACCOUNT && resultCode == RESULT_OK) {
            user_name = data.getStringExtra("user_name");
            nv_name.setText(user_name);
            Toast.makeText(this, user_name + "님 환영합니다~", Toast.LENGTH_SHORT).show();
//            dialog로 재로그인. 아직 이름만 바꿔 재로그인 필요없음.
//            Intent intent = new Intent(getApplicationContext(),LogIn.class);
//            startActivityForResult(intent,REQUEST_LOG_IN);
        }
        if(requestCode == REQUEST_ACCOUNT && resultCode != RESULT_OK) {
            return;
        }
    }

    // create action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            drawerLayout.openDrawer(Gravity.LEFT);
        } else if (id == R.id.clipButton) {
            // 기사 원문 링크, 제목 파싱해 가져오기. adapter.getTitle()
            if(adapter.getOriginal_link() == null) {
                Toast.makeText(this, "기사원문이 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                //분야 : last_selected_part
                //저장 날짜
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String getTime = sdf.format(date);

                //[분야, 저장 날짜, 제목, 링크] 형태로 저장
                databaseReference.child("user_id").child(Util.EncodeString(user_email)).child("scrap")
                        .push()
                        .setValue(last_selected_part + "&&&" + getTime + "&&&" + adapter.getTitle() +
                                "&&&" + adapter.getOriginal_link());

                Toast.makeText(this, "스크랩 완료\n"+adapter.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    // navigation drawer items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.account:
                Intent intent = new Intent(getApplicationContext(),Account.class);
                intent.putExtra("user_email",user_email);
                intent.putExtra("user_name",user_name);
                startActivityForResult(intent,REQUEST_ACCOUNT);
                break;
            case R.id.scrap:
                databaseReference.child("user_id").child(Util.EncodeString(user_email)).child("scrap").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> scraps = new ArrayList<>();//scrap데이터들 모두 저장
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            scraps.add(snapshot.getValue().toString());
                            //[분야, 저장 시각, 제목, 링크] 순으로 파싱.
                            //파싱 => Scrap.java에서 파싱
                        }
                        Collections.reverse(scraps); //최근 날짜부터 표시

                        Intent intent = new Intent(getApplicationContext(),Scrap.class);
                        intent.putStringArrayListExtra("scraps",scraps);
                        startActivityForResult(intent,REQUEST_SCRAP);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.setting:
                Toast.makeText(this, "구현중입니다", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
                builder.setMessage("로그아웃 하시겠습니까?")
                        .setTitle("로그아웃")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //로그아웃
                                FirebaseAuth.getInstance().signOut(); //firebase 로그아웃
                                //로그인 intent 생성해 액티비티 변환
                                Intent intent = new Intent(getApplicationContext(),LogIn.class);
                                startActivityForResult(intent,REQUEST_LOG_IN);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //로그아웃 취소
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
        drawerLayout.closeDrawer(Gravity.LEFT);
        return true;
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //주소, 링크 파싱
                Connection.Response response = Jsoup.connect("https://news.naver.com/main/ranking/popularDay.nhn").ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true).execute();
                doc = response.parse();

                Elements titles = doc.select("table.container tbody tr td.aside div.aside div.section.section_wide ul.section_list_ranking a[title]"); //(사이드부분)가장 많이 본 뉴스 부분 파싱
                Elements links_ele = doc.select("table.container tbody tr td.aside div.aside div.section.section_wide ul.section_list_ranking a[href]"); //링크부분 파싱

                for (Element e:titles) {
                    items.add(e.text());
                }
                for (Element e:links_ele) {
                    links.add("https://news.naver.com" + e.attr("href"));
                }

                //미리보기 이미지 주소 파싱
                //100:정치, 101:경제, 102:사회, 103:생활/문화, 104: 세계, 105:IT/과학
                String part_string;
                for(int i=0;i<6;i++) {
                    part_string = "https://news.naver.com/main/ranking/popularDay.nhn?rankingType=popular_day&sectionId=" + (100 + i);
                    response = Jsoup.connect(part_string).ignoreContentType(true)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")
                            .referrer("http://www.google.com")
                            .timeout(5000)
                            .followRedirects(true).execute();
                    doc = response.parse();

                    //썸네일 이미지가 없는 경우도 있어 썸네일 유무도 체크
                    links_ele = doc.select("table.container tbody tr td.content ol.ranking_list img");
                    Elements thumb_check = doc.select("table.container tbody tr td.content ol.ranking_list div.ranking_thumb a");
                    ArrayList<Integer> thumbIndex = new ArrayList<Integer>();
                    for(int j=0;;j++) {
                        int tmp;
                        tmp = Integer.parseInt(thumb_check.get(j).attr("href").split("rankingSeq=")[1].split("\"")[0]);
                        thumbIndex.add(tmp-1); //thumb counting은 1부터 시작함
                        if (tmp >= 10)
                            break;
                    }
                    int j=0;
                    for(Integer index: thumbIndex)   //이미지링크 rank10까지만 추출
                        imagelinks[i*10+index] = links_ele.get(j++).attr("src");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            viewPager = (ViewPager)findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(6); //처음에 6페이지 모두 로드
            /* //viewpager 좌우 살짝 보이게 하는 옵션
            viewPager.setClipToPadding(false);
            viewPager.setPadding(60,0,60,0);
            viewPager.setPageMargin(30);
            */

            adapter = new PageAdapter(getApplicationContext(),items,links,imagelinks,forTest,comments,emoticon,viewPager);
            viewPager.setAdapter(adapter);
            springDotsIndicator.setViewPager(viewPager);

            //viewPager 바꼈을때
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    //toolbar 메뉴 setting
                    now_part = Util.getPartFromPage(i);
                    part.setText(now_part);
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
        }
    }
}
