package com.example.demo.controller;

import com.example.demo.dto.QueryProcessDto;
import com.example.demo.dto.QueryRequestDto;
import com.example.demo.dto.TQSPDto;
import com.example.demo.repository.PointPRepository;
import com.example.demo.repository.PointVRepository;
import com.example.demo.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Queue;

@Controller
public class UserRequestController {



    @RequestMapping("/receive")
    public String receiveRequest(QueryRequestDto queryRequestDto, Model model) throws Exception {


//        //接收用户输入
//        QueryProcessDto queryProcessDto = queryService.initUserRequest(queryRequestDto);
//
//        //执行算法
//        Queue<TQSPDto> resultQueue = queryService.runQuery(queryProcessDto);

        model.addAttribute("k",queryRequestDto.k);
        model.addAttribute("longitude",queryRequestDto.longitude);
        model.addAttribute("keyString",queryRequestDto.keyString);
        model.addAttribute("latitude",queryRequestDto.latitude);
        model.addAttribute("num",queryRequestDto.num);

        return "receive";
    }

}
