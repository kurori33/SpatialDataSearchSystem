package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

@NodeEntity("p")
public class PointP {

    @Id
    @GeneratedValue
    private Long id;

    @Property("uri")
    private final String uri;

    @Property("latitude")
    private final float latitude;

    @Property("longitude")
    private final float longitude;


    public PointP(Long id, String uri, float latitude, float longitude) {
        this.id = id;
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getUri() {
        return uri;
    }

    public Long getId() {
        return id;
    }
}
