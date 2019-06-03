package com.example.crawl_practice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ScrapChartFrag extends Fragment {

    TextView tv_trend;
    BarChart barChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scrap_chart, container, false);

        barChart = view.findViewById(R.id.chart1);
        tv_trend = view.findViewById(R.id.tv_scrap_trend);

        //Scrap에서 ScrapPagerAdapter통해 스크랩 데이터 모두 받기
        ArrayList<String> scraps = getArguments().getStringArrayList("scraps");
        ArrayList<Scrapped> articles = new ArrayList<>();

        for(String scrap:scraps) {
            //[분야, 저장 시각, 제목, 링크] 순으로 파싱해 Scrapped객체로 하나씩 저장
            String[] parsed = scrap.split("&&&");
            articles.add(new Scrapped(parsed[0],parsed[1],parsed[2],parsed[3]));
        }

        //분야별로 나온 갯수 세기
        int[] count = {0,0,0,0,0,0};
        for(Scrapped article:articles) {
            count[Util.getPageFromPart(article.part)]++;
        }

        //최대 갯수 분야로 스크랩 트렌드 표시
        int max_part = 0;
        int max_cnt = 0;
        for(int i=0;i<count.length;i++) {
            Log.d("갯수",count[i]+"");
            if(count[i] > max_cnt) {
                max_part = i;
                max_cnt = count[i];
            }
        }
        String trend_text = Util.getPartFromPage(max_part) + " 기사를 많이 스크랩했어요!";
        tv_trend.setText(trend_text);

        //횟수
        List<BarEntry> entries = new ArrayList<>();
        for(int i=0;i<6;i++)
            entries.add(new BarEntry(i,count[i]));
        BarDataSet set = new BarDataSet(entries, "분야별 스크랩 수");

        //x축 설정
        final String[] values = { "정치", "경제", "사회", "문화", "세계", "IT"};

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(6);
        xAxis.setTextSize(13f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(values) {
            String[] mvalues = new String[6];
            @Override
            public String getFormattedValue(float value) {
                for(int i=0;i<6;i++)
                    mvalues[i] = values[i];
                return mvalues[(int)value];
            }
        });

        //y축 설정
        YAxis yAxis_left = barChart.getAxisLeft();
        yAxis_left.setAxisMinimum(0);
        yAxis_left.setGranularity(1f);

        YAxis yAxis_right = barChart.getAxisRight();
        yAxis_right.setEnabled(false);

        //기타 차트 설정
        BarData data = new BarData(set);
        set.setColors(Color.BLACK);
        data.setValueTextSize(13f);
        barChart.setData(data);

        Description description = new Description();
        description.setText("");

        barChart.setScaleEnabled(false);
        barChart.setDescription(description);
        barChart.invalidate(); // refresh
        return view;
    }

}
