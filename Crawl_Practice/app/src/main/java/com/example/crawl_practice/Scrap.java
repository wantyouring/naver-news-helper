package com.example.crawl_practice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class Scrap extends AppCompatActivity {

    public static int fragId;
    static boolean toggle = false;

    Toolbar myToolbar;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrap);

        tabLayout = findViewById(R.id.tab_layout);
        myToolbar = findViewById(R.id.toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("경향분석"));
        tabLayout.addTab(tabLayout.newTab().setText("기사확인"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //action bar setting
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_check_circle_outline);

        //scrap데이터 가져오기
        Intent getIntent = getIntent();
        ArrayList<String> scraps = getIntent.getStringArrayListExtra("scraps");

        //viewpager, adapter setting
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final ScrapPagerAdapter adapter = new ScrapPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), scraps);
        viewPager.setAdapter(adapter);
        //tab과 viewpager page 연동
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // viewpager page에 따라서 toolbar 버튼 변경(삭제 버튼은 아직 구현x)
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == 0) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    //myToolbar.getMenu().findItem(R.id.deleteButton).setVisible(false);
                    invalidateOptionsMenu();
                } else if(i == 1) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    //myToolbar.getMenu().findItem(R.id.deleteButton).setVisible(true);
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
    //action bar 설정
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_scrap, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //action bar menu 버튼 클릭 시
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final ScrapListFrag scrapListFrag = (ScrapListFrag) getSupportFragmentManager().findFragmentById(fragId);
        if (id == R.id.deleteButton) { //삭제 버튼
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(scrapListFrag.returnCheckedItemsCount()+"개 기사를 삭제하시겠습니까?")
                    .setTitle("스크랩 삭제")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //fragent에서 데이터 삭제
                            scrapListFrag.deleteCheckedItems();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if(id == android.R.id.home) { //선택 버튼
            toggle = !toggle;
            scrapListFrag.selectAllItems(toggle);
        }
        return true;
    }

    //뒤로가기 버튼
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);
        finish();
    }
}
