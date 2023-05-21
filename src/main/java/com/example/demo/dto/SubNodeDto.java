package com.example.demo.dto;

import com.example.demo.entity.PointP;
import com.example.demo.repository.PointPRepository;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Optional;

@Data
@QueryResult
public class SubNodeDto {

    private int path;

    private long subNodeID;

    public int getPath(){
        return path;
    }

    public long getSubNodeID() {

        return subNodeID;
    }

}
