package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequestDto {

    public int k; // top k
    public double longitude; //用户所在经度
    public String keyString; //关键字列表字符串
    public double latitude; //用户所在纬度
    public int num; //用户选择的算法

}
