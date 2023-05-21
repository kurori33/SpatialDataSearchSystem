package com.example.demo.service.Impl;

import com.example.demo.dto.QueryProcessDto;
import com.example.demo.dto.QueryRequestDto;
import com.example.demo.dto.TQSPDto;
import com.example.demo.semanticsquery.KSPQuery;
import com.example.demo.service.QueryService;
import com.example.demo.spatialIndex.IndexManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

@Service
public class QueryServiceImpI implements QueryService {

    IndexManager spatialindex = new IndexManager();
    KSPQuery kspQuery = new KSPQuery();

    //初始化用户请求
    @Override
    public QueryProcessDto initUserRequest(QueryRequestDto queryRequestDto){

        QueryProcessDto queryProcessDto = new QueryProcessDto();
        queryProcessDto.rTree = spatialindex.readIndex();
        queryProcessDto.alph = 2;
        queryProcessDto.k = queryRequestDto.k;
        String[] strArray = queryRequestDto.keyString.split(",");
        queryProcessDto.keyList = new ArrayList<String>(Arrays.asList(strArray));
        queryProcessDto.longitude = queryRequestDto.longitude;
        queryProcessDto.latitude = queryRequestDto.latitude;

        return queryProcessDto;

    }


    //执行查询
    @Override
    public Queue<TQSPDto> runQuery(QueryProcessDto queryProcessDto) throws Exception {

        Queue<TQSPDto> resultQueue = new PriorityQueue<>();

        //如果是ksp算法
        if(queryProcessDto.num <= 2){
            kspQuery.initData(queryProcessDto);
            kspQuery.getKeywordsNodes();
            if(queryProcessDto.num == 0){
                resultQueue = kspQuery.BspQuery(queryProcessDto.rTree).getHk();
            }
            else if(queryProcessDto.num == 1){
                resultQueue = kspQuery.SppQuery(queryProcessDto.rTree).getHk();
            }
            else if(queryProcessDto.num == 2){
                resultQueue = kspQuery.SpQuery(queryProcessDto.rTree,queryProcessDto.alph).getHk();
            }
        }
        else if (queryProcessDto.num == 3){

        }

        return resultQueue;
    }

}
