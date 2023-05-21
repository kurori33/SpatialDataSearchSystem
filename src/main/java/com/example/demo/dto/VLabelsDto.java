package com.example.demo.dto;

import com.example.demo.entity.PointP;
import com.example.demo.entity.PointV;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

@Data
@QueryResult
public class VLabelsDto {

    public PointV pointV;

    public List<String> labels;
}
