package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity("v")
public class PointV {

    @Id @GeneratedValue
    private Long id;

    @Property("uri")
    private final String uri;


    public PointV(String uri) {
        this.uri = uri;
    }


    public String getUri() {
        return uri;
    }


    public Long getId() {
        return id;
    }



    }

