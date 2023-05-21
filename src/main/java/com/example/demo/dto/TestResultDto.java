package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

//测试结果传输对象
@Data
public class TestResultDto {

    private int index; //序号
    private long runtime; //运行时间
    private long runtime_sem; //构建语义位置时间
    private long runtime_other; //其他时间
    private long runtime_prepare; //预处理时间
    private int result_num; //返回结果数
    private String result_root; //返回结果根节点
    private double sem_score_ave; //平均相似度
    private double ltp_score_ave; //平均松散度
    private double f_score_ave; //平均相似度/松散度
    private double dis_score_ave; //平均空间距离
    private int dis_index_num; //访问空间索引节点数
    private int sem_num; //构建语义位置数
    private Queue<TQSPDto> Hk; //优先队列存放结果
    ArrayList<Object> result; //存放skg-sp结果

}
