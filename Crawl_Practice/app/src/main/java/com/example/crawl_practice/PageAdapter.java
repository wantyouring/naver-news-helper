package com.example.crawl_practice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.AsyncTask;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class PageAdapter extends PagerAdapter {

    private LayoutInflater inflater;
    final Context context;
    String items[] = new String[60];
    String links[] = new String[60];
    String imageLinks[] = new String[60];
    int characters[] = new int[5];
    TextView forTest;
    TextView comments;
    ImageView emoticon;
    ViewPager viewPager;

    String link_uri;
    String articleId;
    String original_link;
    String title;

    //생성자
    public PageAdapter(Context context, ArrayList<String> items, ArrayList<String> links,
                       String imageLinks[],TextView forTest, TextView comments, ImageView emoticon, ViewPager viewPager) {
        this.context = context;
        for(int i=0;i<60;i++) {
            this.items[i] = items.get(i);
            this.links[i] = links.get(i);
            this.imageLinks[i] = imageLinks[i];
        }
        this.forTest = forTest;
        this.comments = comments;
        this.emoticon = emoticon;
        this.viewPager = viewPager;
    }
    //getter

    public String getOriginal_link() {
        return original_link;
    }
    public String getTitle() {
        return title;
    }

    //PageAdapter implements
    @Override
    public int getCount() {
        return 6;   //정치, 경제, 사회, 생활/문화, 세계, IT/과학
    }

    //PageAdapter implements
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (LinearLayout) o;
    }

    //page생성
    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.slider, container, false);
        ListView listViewOfArticle = (ListView)v.findViewById(R.id.listViewOfArticle);

        String picked_item[] = new String[10];//한 분야의 10개 title 추출 + 미리보기 image uri
        final String picked_link[] = new String[10];

        for(int i=0;i<10;i++) {
            picked_item[i] = items[10*position + i] + "wantyouring" + imageLinks[10*position + i];
            picked_link[i] = links[10*position + i];
        }

        ListAdapter listAdapter = new listAdapter(context,picked_item);
        listViewOfArticle.setAdapter(listAdapter);
        //롱클릭 시 물어보고 기사로 이동
        listViewOfArticle.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                link_uri = picked_link[position]; //링크uri 저장.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
                builder.setMessage("해당 기사로 이동하시겠습니까?")
                        .setTitle("News")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //기사로 이동
                                //링크 열기
                                Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(link_uri));
                                MainActivity.context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); //adapter에서 activity실행하려면 addFlags해줘야함.
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
        //그냥 클릭 시 기사 요약
        listViewOfArticle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                link_uri = picked_link[position]; //링크uri 저장.
                MainActivity.last_selected_part = Util.getPartFromPage(viewPager.getCurrentItem());
                //  내용(or 댓글) 가져오기.
                JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask(link_uri,articleId,forTest,comments,emoticon);
                jsoupAsyncTask.execute();
            }
        });
        container.addView(v);

        return v;
    }

    //page삭제
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    //article short click시 기사 성향, 기사 원본링크 가져오기.
    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        String link_uri;
        String articleId;
        String commentsTextString = "";
        TextView forTest;
        TextView commentsText;
        ImageView emoticon;

        //constructor
        public JsoupAsyncTask(String link_uri, String articleId,
                              TextView forTest, TextView commentsText,ImageView emoticon) {
            super();
            this.link_uri = link_uri;
            this.articleId = articleId;
            this.forTest = forTest;
            this.commentsText = commentsText;
            this.emoticon = emoticon;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //기사 부분
                Connection.Response response = Jsoup.connect(link_uri).ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true).execute();
                Document doc = response.parse();
                //System.out.println(doc.toString());

                //기사 고유번호 파싱. articleId = ne_xxx_xxxxxxxxxx
                articleId = doc.select("div._reactionModule.u_likeit").first().attr("data-cid");
                //기사 원본링크 파싱
                Element parsed = doc.select("div.article_info div.sponsor a").first();
                if(parsed.text().equals("기사원문")) {
                    original_link = parsed.attr("href");
                } else {
                    original_link = null;
                }
                //기사 제목 파싱
                title = doc.select("head title").first().text();
                System.out.println("테스트: "+original_link);

                //기사 고유번호로 쿼리보내 기사성향파악.
                Document doc2 = Jsoup.connect("https://news.like.naver.com/v1/search/contents?q=NEWS["+articleId+"]|NEWS_SUMMARY["+articleId+"]")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .get();
                String doc_str = doc2.toString();
                //System.out.println(doc2.toString());
                //성향count부분 파싱
                int angry_cnt = 0;
                int sad_cnt = 0;
                int want_cnt = 0;
                int warm_cnt = 0;
                int like_cnt = 0;
                if(doc_str.contains("\"angry\",\"count\":"))
                    angry_cnt = Integer.parseInt(doc_str.split("\"angry\",\"count\":",2)[1].split(",",2)[0]);
                if(doc_str.contains("\"sad\",\"count\":"))
                    sad_cnt = Integer.parseInt(doc_str.split("\"sad\",\"count\":",2)[1].split(",",2)[0]);
                if(doc_str.contains("\"want\",\"count\":"))
                    want_cnt = Integer.parseInt(doc_str.split("\"want\",\"count\":",2)[1].split(",",2)[0]);
                if(doc_str.contains("\"warm\",\"count\":"))
                    warm_cnt = Integer.parseInt(doc_str.split("\"warm\",\"count\":",2)[1].split(",",2)[0]);
                if(doc_str.contains("\"like\",\"count\":"))
                    like_cnt = Integer.parseInt(doc_str.split("\"like\",\"count\":",2)[1].split(",",2)[0]);
                characters[0] = angry_cnt;
                characters[1] = sad_cnt;
                characters[2] = want_cnt;
                characters[3] = warm_cnt;
                characters[4] = like_cnt;

                //해당 기사 댓글 파싱. => referrer naver로 줘야 요청성공함.
                String articleId1 = articleId.split("_")[1];
                String articleId2 = articleId.split("_")[2];
                //Document doc3 = Jsoup.connect("https://apis.naver.com/commentBox/cbox/web_neo_list_jsonp.json?ticket=news&templateId=view_it&pool=cbox5&_callback=jQuery11240772130748100907_1558414665290&lang=ko&country=KR&objectId=news016%2C0001536858&categoryId=&pageSize=20&indexSize=10&groupId=&listType=OBJECT&pageType=more&parentCommentNo=1713860462&page=1&userType=&includeAllStatus=true")
                        Document doc3 = Jsoup.connect("https://apis.naver.com/commentBox/cbox/web_neo_list_jsonp.json?ticket=news&templateId=default_society&pool=cbox5&_callback=jQuery1707138182064460843_1523512042464&lang=ko&country=&objectId=news"+articleId1+","+articleId2+"&categoryId=&pageSize=20&indexSize=10&groupId=&listType=OBJECT&pageType=more&page=1&refresh=false&sort=FAVORITE")
                        //Document doc3 = Jsoup.connect("https://apis.naver.com/commentBox/cbox/web_neo_list_jsonp.json?ticket=news&templateId=default_it&pool=cbox5&_callback=jQuery1124028231177890026493_1558420491431&lang=ko&country=KR&objectId=news"+articleId+"&categoryId=&pageSize=20&indexSize=10&groupId=&listType=OBJECT&pageType=more&page=1&initialize=true&userType=&useAltSort=true&replyPageSize=20&moveTo=&sort=favorite&includeAllStatus=true&_=1558420491432")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")
                        .referrer("https://news.like.naver.com/")
                        .ignoreContentType(true)
                        .get();
                //System.out.println(articleId+"\n"+doc3.toString());
                String split[] = doc3.toString().split("\"contents\":\""); //댓글 내용 파싱
                String sympathy[] = doc3.toString().split("\"sympathyCount\":"); //공감 갯수 파악
                String antipathy[] = doc3.toString().split("\"antipathyCount\":"); //비공감 갯수 파악

                //댓글 내용
                ArrayList<String> comments = new ArrayList<String>();
                for(int i=1;i<split.length;i++) {
                    //댓글 부분 가져오고 \n문자 공백으로 바꿔주기, \문자 없애주기
                    comments.add(split[i].split("\",\"userIdNo\"")[0]
                            .replace("\\n"," ").replace("\\",""));
                }
                //공감 갯수
                ArrayList<Integer> sympathyCount = new ArrayList<>();
                for(int i=1;i<split.length;i++) {
                    String str = sympathy[i].split(",\"antipathyCount\"")[0];
                    if(str.equals(""))
                        sympathyCount.add(0);
                    else
                        sympathyCount.add(Integer.parseInt(str));
                }
                //비공감 갯수
                ArrayList<Integer> antipathyCount = new ArrayList<>();
                for(int i=1;i<split.length;i++) {
                    String str = antipathy[i].split(",\"userBlind\"")[0];
                    if(str.equals(""))
                        antipathyCount.add(0);
                    else
                        antipathyCount.add(Integer.parseInt(str));
                }

                int i=0;
                for(String e:comments) {
                    commentsTextString +=(i+1)+" : "+ e + "\n"+"   공감 : "+sympathyCount.get(i)+" 비공감 : "+antipathyCount.get(i)+"\n\n";
                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String message = "";

            //최댓값 찾아 기사 성향 파악.
            int max = 0;
            for(int i=0;i<5;i++){
                if(max < characters[i])
                    max = characters[i];
            }

            if (max == characters[0]) {
                message = "많은 사람이 화낸 기사에요!";
                emoticon.setImageResource(R.drawable.angry);
            } else if(max == characters[1]) {
                message = "많은 사람이 슬퍼한 기사에요ㅠㅠ";
                emoticon.setImageResource(R.drawable.sad);
            } else if(max == characters[2]) {
                message = "많은 사람이 후속기사를 원해요!";
                emoticon.setImageResource(R.drawable.want);
            } else if(max == characters[3]) {
                message = "훈훈한 기사에요~";
                emoticon.setImageResource(R.drawable.warm);
            } else if(max == characters[4]) {
                message = "많은 사람이 좋아한 기사에요!";
                emoticon.setImageResource(R.drawable.like);
            }

            forTest.setText(message);
            commentsText.scrollTo(0,0); //scroll맨 위로 올리기
            commentsText.setText(commentsTextString);

        }
    }
}
