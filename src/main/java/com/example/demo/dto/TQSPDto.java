package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class TQSPDto implements Comparable<TQSPDto> {

    public double score;

    public String root;

    public ArrayList<String> subTree = new ArrayList<>();

    public double sem_score;

    public double dis_score;

    public double longitude; //用户所在经度

    public double latitude; //用户所在纬度


    public double getScore(){
        return score;
    }
    public String getRoot(){
        return root;
    }


    @Override
    public int compareTo(TQSPDto o) {
        if(this.getScore() < o.getScore()) {
            return 1;
        } else if (this.getScore() > o.getScore()) {
            return -1;
        } else {
            return 0;
        }
    }

}
