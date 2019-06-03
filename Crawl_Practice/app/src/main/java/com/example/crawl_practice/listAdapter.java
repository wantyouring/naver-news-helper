package com.example.crawl_practice;

import android.support.annotation.NonNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class listAdapter extends ArrayAdapter<String> {
    public listAdapter(Context context, String[] items) {
        super(context, R.layout.list_layout_scrap,items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater imageInflater = LayoutInflater.from(getContext()); // layoutinflater : XML에 정의된 resource들을 view형태로 반환.
        View view = imageInflater.inflate(R.layout.list_layout, parent, false);
        String item = getItem(position); //해당 position의 item string객체 생성.
        TextView textView = (TextView)view.findViewById(R.id.textView); //textView객체 생성
        ImageView imageView = (ImageView)view.findViewById(R.id.imageView); //ImageView객체 생성

        //items에서 title, imageslink 구분하기
        String imageLink = item.split("wantyouring")[1];
        item = item.split("wantyouring")[0];

        textView.setText(item); //item의 string으로 textView에 쓰기
        // 기사 미리보기 이미지 가져오기.
        if(!imageLink.contains("https"))
            imageView.setImageResource(R.drawable.cat);
        else {
            Picasso.with(MainActivity.context)
                    .load(imageLink)
                    .into(imageView);
        }
        return view;
    }
}
