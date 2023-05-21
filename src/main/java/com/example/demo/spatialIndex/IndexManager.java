package com.example.demo.spatialIndex;


import com.example.demo.entity.PointP;

import com.example.demo.service.ResourceService;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.*;
import java.util.List;

@Component
public class IndexManager {

    private static ResourceService resourceService;

    @Autowired
    public void setResourceService(ResourceService resourceService) {
        IndexManager.resourceService = resourceService;
    }


    //创建空间索引
    public void createIndex() {

        RTree<String, Point> rtree; //r树
        //创建r树
        rtree = RTree.star().create();

        int pageSize = 1000; //每一页大小
        int pageIndex = 0; //当前页号
        int pageCount = resourceService.getNumP(); //v总记录数

        while (pageIndex < pageCount) {

            //分页获取pagesize个节点
            List<PointP> pplist = resourceService.getPointP(pageIndex, pageSize);

            //遍历每一个节点加入空间索引
            for (PointP pp : pplist) {

                float lon = pp.getLongitude();
                float lat = pp.getLatitude();
                Point point = Geometries.pointGeographic(lon, lat);
                String name = "p" + String.valueOf(pp.getId());
                rtree = rtree.add(name, point);

            }

            //当前页号改变
            pageIndex = pageIndex + pageSize;

            System.out.println(pageIndex);

        }


        //空间索引序列化
        String root = System.getProperty("user.dir");
        String fileName = "spatialIndex.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\spatialIndex" +File.separator  + fileName;

        OutputStream outputStream = null;

        try{
            //写入文件
            outputStream = new FileOutputStream(filePath);
            Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
            serializer.write(rtree, outputStream);

        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                //关闭写入
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    //遍历空间索引，在neo4j中建立

    //读入空间索引
    public RTree<String, Point> readIndex(){

        RTree<String, Point> tree = null;

//        String root = System.getProperty("user.dir");
        String root = "D:\\学习\\毕业设计\\demo";
        String fileName = "spatialIndex.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\spatialIndex" +File.separator  + fileName;

        InputStream inputStream = null;

        File file = new File(filePath);
        long size = file.length();

        try{

            //读入文件
            inputStream = new FileInputStream(filePath);
            Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
            tree = serializer.read(inputStream ,size , InternalStructure.SINGLE_ARRAY);

            return tree;

        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {

                //关闭读入
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tree;

    }


    //获得距离查询位置distance范围内的所有p类型节点
    public List<Entry<String, Point>> getPointPByDis(RTree<String, Point> rtree, Point point, double distance, int maxcount) {

        //返回结果
        List<Entry<String, Point>> plist = rtree.nearest(point,distance,maxcount).toList().toBlocking().single();

        return plist;

    }



}
