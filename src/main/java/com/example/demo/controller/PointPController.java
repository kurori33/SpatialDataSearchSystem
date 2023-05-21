package com.example.demo.controller;

import com.example.demo.entity.PointP;
import com.example.demo.service.PointPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;



@RestController
@RequestMapping("/entity/pointp")
public class PointPController {

    @Autowired
    PointPService pointPService;

    @GetMapping("/getById")
    public PointP getById(@RequestParam(value = "id") long id) {

        PointP p = pointPService.getPointPbyId(id);

        return p;
    }


}
