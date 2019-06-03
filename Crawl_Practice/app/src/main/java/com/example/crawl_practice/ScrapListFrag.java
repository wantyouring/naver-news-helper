package com.example.crawl_practice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ScrapListFrag extends Fragment {
    View view;
    ListView listViewOfArticle;
    ArrayList<String> scraps;
    ScrapListAdapter listAdapter;
    ArrayList<Scrapped> articles;
    FirebaseUser user;
    String user_email;
    DatabaseReference databaseReference;
    
    //Scrap액티비티에서 fragment 접근하기 위해
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Scrap.fragId = getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.slider, container, false);
        listViewOfArticle = (ListView)view.findViewById(R.id.listViewOfArticle);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        //로그인된 사용자 email 가져오기
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user_email = user.getEmail();
        } else {
            Toast.makeText(getActivity(), "오류 발생", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        //Scrap에서 ScrapPagerAdapter통해 스크랩 데이터 모두 받기
        scraps = getArguments().getStringArrayList("scraps");
        articles = new ArrayList<>();

        for(String scrap:scraps) {
            //[분야, 날짜, 제목, 링크] 순으로 파싱해 Scrapped객체로 하나씩 저장
            String[] parsed = scrap.split("&&&");
            articles.add(new Scrapped(parsed[0],parsed[1],parsed[2],parsed[3]));
        }
        //String[] picked_item = scraps.toArray(new String[0]);
        listAdapter = new ScrapListAdapter(getActivity(),scraps);
        //listAdapter = new ScrapListAdapter(getActivity(),new ArrayList<String>(scraps.keySet()));
        listViewOfArticle.setAdapter(listAdapter);
        //롱클릭 시 원본 기사로 이동
        listViewOfArticle.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String link_uri = articles.get(position).link; //링크uri 저장.
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("해당 기사로 이동하시겠습니까?")
                        .setTitle("News")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //기사로 이동
                                //링크 열기
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link_uri));
                                getActivity().startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //기사로 이동x
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true; // 롱클릭 이후 숏클릭 인식 안되게
            }
        });
        //그냥 클릭 시 선택상태(배경 바꾸기)
        listViewOfArticle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "기사 선택", Toast.LENGTH_SHORT).show();
            }
        });
        container.addView(view);

        return view;
    }

    //check된 item 수
    public int returnCheckedItemsCount() {
        return listViewOfArticle.getCheckedItemCount();
    }

    //모든 item check 또는 check해제
    public void selectAllItems(boolean toggle) {
        for(int i=0;i<listViewOfArticle.getAdapter().getCount();i++) {
            listViewOfArticle.setItemChecked(i,toggle);
        }
    }

    //checked items 삭제
    public void deleteCheckedItems() {
        SparseBooleanArray checkedItems = listViewOfArticle.getCheckedItemPositions();
        int count = listAdapter.getCount();

        for(int i=count-1; i>=0; i--) {
            Log.d("인덱스",i+"");
            if(checkedItems.get(i)) {
                //서버에서 삭제
                final String removeItem = scraps.get(i);
                databaseReference.child("user_id").child(Util.EncodeString(user_email)).child("scrap").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> scraps = new ArrayList<>();//scrap데이터들 모두 저장
                        String removeKey;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //remove할 값과 똑같으면 key값 찾아 delete
                            if(snapshot.getValue().toString().equals(removeItem)){
                                removeKey = snapshot.getKey();
                                databaseReference.child("user_id").child(Util.EncodeString(user_email)).child("scrap")
                                        .child(removeKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            //삭제완료시 완료 메시지
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //listview에서 삭제
                scraps.remove(i);
            }
        }
        listViewOfArticle.clearChoices();
        listAdapter.notifyDataSetChanged();
    }
}