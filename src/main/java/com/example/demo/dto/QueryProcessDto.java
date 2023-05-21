package com.example.demo.dto;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

//算法的输入
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryProcessDto {

    public int k; // top k
    public ArrayList<String> keyList; //关键字列表字符串
    public ArrayList<String> keyList_similar; //关键字列表字符串（近似词替换）
    public double longitude; //用户所在经度
    public double latitude; //用户所在纬度
    public RTree<String, Point> rTree; //空间索引
    public int alph; //邻域半径
    public int num; //算法选择


}
