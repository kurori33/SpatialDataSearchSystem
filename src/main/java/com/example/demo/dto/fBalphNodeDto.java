package com.example.demo.dto;

import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.geometry.Point;
import lombok.Data;

@Data
public class fBalphNodeDto implements Comparable<fBalphNodeDto>{

    private Node<String, Point> node;

    private double fBalph;

    public fBalphNodeDto(Node<String, Point> node,double fBalph){
        this.node = node;
        this.fBalph = fBalph;
    }

    public Node<String, Point> getNode(){
        return node;
    }

    public double getFBalph(){
        return fBalph;
    }

    //最小优先
    @Override
    public int compareTo(fBalphNodeDto o) {
        if(this.getFBalph() < o.getFBalph()) {
            return -1;
        } else if (this.getFBalph() > o.getFBalph()) {
            return 1;
        } else {
            return 0;
        }
    }
}
