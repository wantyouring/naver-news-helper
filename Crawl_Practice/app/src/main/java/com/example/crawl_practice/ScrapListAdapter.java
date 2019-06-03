package com.example.crawl_practice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ScrapListAdapter extends ArrayAdapter<String> {
    public ScrapListAdapter(Context context, ArrayList<String> items) {
        super(context, R.layout.list_layout,items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater imageInflater = LayoutInflater.from(getContext()); // layoutinflater : XML에 정의된 resource들을 view형태로 반환.
        View view = imageInflater.inflate(R.layout.list_layout_scrap, parent, false);
        String item = getItem(position); //해당 position의 item string객체 생성.

        TextView tv_title = view.findViewById(R.id.title);
        TextView tv_date = view.findViewById(R.id.date);
        TextView tv_part = view.findViewById(R.id.part);
        ImageView imageView = view.findViewById(R.id.imageView);

        //items에서 (분야 0,날짜 1,제목 2,링크 3)구분해 처리
        String[] parsed = item.split("&&&");

        //분야 : 해당 분야 이미지 가져오기
        int part_num = Util.getPageFromPart(parsed[0]);
        switch(part_num) {//정치,경제,사회,문화,세계,IT
            case 0:
                imageView.setImageResource(R.drawable.politics);
                tv_part.setText("정치");
                break;
            case 1:
                imageView.setImageResource(R.drawable.economy);
                tv_part.setText("경제");
                break;
            case 2:
                imageView.setImageResource(R.drawable.society);
                tv_part.setText("사회");
                break;
            case 3:
                imageView.setImageResource(R.drawable.culture);
                tv_part.setText("문화");
                break;
            case 4:
                imageView.setImageResource(R.drawable.global);
                tv_part.setText("세계");
                break;
            case 5:
                imageView.setImageResource(R.drawable.it);
                tv_part.setText("IT");
                break;
        }

        //날짜
        tv_date.setText(parsed[1]);
        //제목
        tv_title.setText(parsed[2]);

        return view;
    }
}
