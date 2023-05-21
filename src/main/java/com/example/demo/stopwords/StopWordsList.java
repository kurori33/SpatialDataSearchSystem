package com.example.demo.stopwords;

import java.io.*;
import java.util.HashSet;
import java.util.regex.Pattern;

public class StopWordsList {

    private HashSet<String> stopWordsList; //停用词表

    public void setStopWordsList() throws IOException {
        //创建停用词表
        stopWordsList = new HashSet<>();

        String root = System.getProperty("user.dir");
        String fileName = "stopwordslist.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\stopwords" +File.separator  + fileName;

        //读取停用词文档
        BufferedReader br = new BufferedReader( new FileReader(filePath));

        //暂存停用词
        String str = null;

        //将停用词添加入词表
        while((str = br.readLine()) != null ){

            stopWordsList.add(str);

        }
    }

    //去除关键字表中的停用词，如：the owl ns0 数字等
    public HashSet<String> stopWordsRemove(HashSet<String> keywordslist){

        //去除停用词后关键字表
        HashSet<String> newkeywordslist = new HashSet<>();

        //为0保留，为1去除
        int remove = 0;

        //去除关键字表中的数字
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");

        //输出停用词表
        for( String keyword : keywordslist ){
            for(String stopword : stopWordsList){
                if( (keyword.toLowerCase()).equals(stopword) || keyword.equals(" ") || pattern.matcher(keyword).matches()){
                    remove = 1;
                    break;
                }
            }
            if(remove == 0){
                newkeywordslist.add(keyword.toLowerCase());
            }
            remove = 0;
        }
        return(newkeywordslist);
    }
}
