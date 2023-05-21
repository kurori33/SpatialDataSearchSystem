package com.example.demo.keywords;

import com.example.demo.dto.VLabelsDto;
import com.example.demo.entity.PointV;
import org.apache.commons.lang3.StringUtils;


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class KeyWordsList {


    public HashSet<String> keyWordsList; //关键字列表


    //将uri值转化为关键字，例如：http://yago-knowledge.org/resource/Bethsaida ——> Bethsaida
    public void uriToKeyWordsList(String uri){

        String[] keywords = null;
        String pattern1 = ".*http://.*";
       // String pattern2 = ".*http:_.*";
        boolean matches1 = Pattern.matches(pattern1,uri);

        //http://yago-knowledge.org/resource/Bethsaida ——> Bethsaida
        if(matches1){

            //截取uri最后部分
            String index = StringUtils.substringBeforeLast(uri, "/");

            uri = uri.substring(index.length()+1);

            keywords = uri.split("_");

        }

        // http:_www.ecuafutbol.org/web/noticias_seccion.php
        // es/Anexo:Obispos_de_Santiago_de_Chile

        if(keywords!=null){

            //将关键字添加入字典
            for(int j = 0 ; j < keywords.length ; j++ ){
                if(keywords[j] != ""){
                    keyWordsList.add(keywords[j]);
                }
            }

        }


    }

    //将标签值转化为关键字，例如：ns0__wikicat_Christian_mystics ——> Christian, mystics
    public void labelToKeyWordsList(List<String> labels) {

        //创建关键字字典
        keyWordsList = new HashSet<>();

        for(String label:labels){

            String[] keywords = label.split("_");

            for(int i = 0 ; i < keywords.length ; i++ ){

                if(keywords[i] != ""){
                    keyWordsList.add(keywords[i]);
                }

            }
        }
    }

    //生成关键字文档
    public void setKeyWordsList(String keywords,String id) throws IOException {

        String root = System.getProperty("user.dir");
        root= root.substring(0,root.lastIndexOf("\\"));
        String fileName = id + ".txt";

        String filePath = root + File.separator + "document" +File.separator  + fileName;
//        String filePath = root + File.separator + "documentp" +File.separator  + fileName;

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
        bw.write(keywords);
        bw.close();
    }

    //生成关键字文档
    public void setKeyWordsListAll(String keywords) throws IOException {

        String root = System.getProperty("user.dir");
        String fileName = "keywordslist.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\keywords" +File.separator  + fileName;

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath,true));
        bw.write(keywords);
        bw.newLine();
        bw.close();
    }

    //清空关键字文档
    public void deleteKeyWordsListAll() throws IOException {
        String root = System.getProperty("user.dir");
        String fileName = "keywordslist.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\keywords" +File.separator  + fileName;
        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
        bw.write("");
        bw.close();

    }



}
