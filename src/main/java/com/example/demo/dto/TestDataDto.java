package com.example.demo.dto;

import lombok.Data;

//测试数据传输对象
@Data
public class TestDataDto {

    private int num; //序号
    private String uri; //根节点uri
    private long id; //根节点id
    private float longitude; //根节点经度
    private float latitude; //根节点纬度
    private String longlat; //根节点经纬度: "12, 13"
    private String keys; //查询关键字: "hello, world"
    private String keys_similar; //使用近似词替换的查询关键字:"hi, world"
    private int keys_similar_num; //近似词替换的个数
    private float longitude_similar; //查询位置经度
    private float latitude_similar; //查询位置纬度
    private String longlat_similar; //查询位置经纬度: "12.1, 13.3"
    private double distance; //查询位置和根节点的距离 km
    

}
