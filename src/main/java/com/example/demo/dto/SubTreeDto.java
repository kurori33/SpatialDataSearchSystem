package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SubTreeDto {

    public int LTp;

    public ArrayList<String> subTree = new ArrayList<>();

    public SubTreeDto(int LTp, ArrayList<String> subTree) {

        this.LTp = LTp;
        this.subTree = subTree;
    }


    public int getLTp(){
        return LTp;
    }

    public ArrayList<String> getSubTree(){
        return subTree;
    }

}
