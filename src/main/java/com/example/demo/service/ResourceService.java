package com.example.demo.service;

import com.example.demo.dto.PLabelsDto;
import com.example.demo.dto.SubNodeDto;
import com.example.demo.dto.VLabelsDto;
import com.example.demo.entity.PointP;
import com.example.demo.entity.PointV;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;

import java.io.IOException;
import java.util.List;

public interface ResourceService {


    //返回节点V的所有标签
    String listAllLabelsV(PointV v);

    //返回p类型节点的数量
    int getNumP();

    //分页获取p类型节点
    List<PointP> getPointP(int pageIndex, int pageSize);

    //返回v类型节点的数量
    int getNumV();

    //返回节点P的关键字列表
    String listKeyWordsListP(PLabelsDto p) throws IOException;

    //返回节点V的关键字列表
    String listKeyWordsListV(VLabelsDto v) throws IOException;

    //生成关键字文档
    void setKeyWordsList() throws IOException;

    //生成关键字文档
    void setKeyWordsListP() throws IOException;

    //生成倒排索引
    void setInvertedIndex() throws Exception;

    //生成空间索引
    void setSpatialIndex();

    //读取空间索引
    RTree<String, Point> readSpatialIndex();

    //根据关键字查询倒排索引
    String searchKeyword( String keyword ) throws Exception;

    //根据请求位置获取p节点
    List<Entry<String, Point>> getPointPByDis(RTree<String, Point> rtree, Point point, double maxdistance, int maxcount);

    //获得uri为uri的根节点的所有子结点，返回对应深度
    List<SubNodeDto> getSubtree(String uri);

    //根据名称查找节点，例如：p1 --> 标签为p ，id为1
    PointP getPointPByName(String s);


}
