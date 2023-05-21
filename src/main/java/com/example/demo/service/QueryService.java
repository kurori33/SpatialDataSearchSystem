package com.example.demo.service;

import com.example.demo.dto.QueryProcessDto;
import com.example.demo.dto.QueryRequestDto;
import com.example.demo.dto.TQSPDto;
import com.example.demo.entity.PointV;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import org.springframework.stereotype.Service;

import java.util.Queue;


public interface QueryService {

    //初始化用户请求
    QueryProcessDto initUserRequest(QueryRequestDto queryRequestDto);

//    //执行查询
    Queue<TQSPDto> runQuery(QueryProcessDto queryProcessDto) throws Exception;
}
