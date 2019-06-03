package com.example.crawl_practice;

public class Util {
    // utility method들 모음
    //분야 string 입력받아 해당 분야 번호 return
    public static int getPageFromPart(String part) {
        int page = 0;

        if(part.equals("정치")){
            page = 0;
        } else if(part.equals("경제")) {
            page = 1;
        } else if(part.equals("사회")){
            page = 2;
        } else if(part.equals("생활/문화")){
            page = 3;
        } else if(part.equals("세계")){
            page = 4;
        } else if(part.equals("IT/과학")){
            page = 5;
        }

        return page;
    }

    //분야 번호 입력받아 분야 string return
    public static String getPartFromPage(int position) {
        String now_part = null;

        switch (position) {
            case 0:
                now_part = "정치";
                break;
            case 1:
                now_part = "경제";
                break;
            case 2:
                now_part = "사회";
                break;
            case 3:
                now_part ="생활/문화";
                break;
            case 4:
                now_part = "세계";
                break;
            case 5:
                now_part = "IT/과학";
                break;
        }
        return now_part;
    }

    //firebase에 .포함된 문자열 저장 불가능하므로 ,로 encoding
    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }

    //decoding
    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }
}
