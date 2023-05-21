package com.example.demo.service.Impl;

import com.example.demo.entity.PointP;
import com.example.demo.repository.PointPRepository;
import com.example.demo.service.PointPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PointPServiceImpl implements PointPService {


    @Autowired
    PointPRepository pointPRepository;

    //根据id返回节点p的坐标
    @Override
    public PointP getPointPbyId(long id){

        PointP p = pointPRepository.getPointPById(id);
        return p;

    }
}
