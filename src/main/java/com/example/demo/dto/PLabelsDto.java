package com.example.demo.dto;

import com.example.demo.entity.PointP;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

@Data
@QueryResult
public class PLabelsDto {

    public PointP pointP;

    public List<String> labels;


}
