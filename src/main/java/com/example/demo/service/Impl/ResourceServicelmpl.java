package com.example.demo.service.Impl;

import com.example.demo.dto.PLabelsDto;
import com.example.demo.dto.SubNodeDto;
import com.example.demo.dto.VLabelsDto;
import com.example.demo.entity.PointP;
import com.example.demo.entity.PointV;
import com.example.demo.invertedindex.IndexManager;
import com.example.demo.keywords.KeyWordsList;
import com.example.demo.service.ResourceService;
import com.example.demo.stopwords.StopWordsList;
import com.example.demo.repository.PointPRepository;
import com.example.demo.repository.PointVRepository;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;


@Service
//@Transactional(value="neo4jTransactionManager")
public class ResourceServicelmpl implements ResourceService {

//    private PointVRepository pointVRepository;
//
//    private PointPRepository pointPRepository;

//    @Autowired
//    public ResourceServicelmpl(PointVRepository pointVRepository, PointPRepository pointPRepository) {
//        this.pointVRepository = pointVRepository;
//        this.pointPRepository = pointPRepository;
//    }
//

    @Autowired
    @Lazy
    private PointVRepository pointVRepository;

    @Autowired
    @Lazy
    private PointPRepository pointPRepository;


    KeyWordsList keyWords = new KeyWordsList();

    StopWordsList stopWords = new StopWordsList();

    IndexManager indexManager = new IndexManager();

    com.example.demo.spatialIndex.IndexManager spatialindex = new com.example.demo.spatialIndex.IndexManager();



    //返回节点V的所有标签
    @Override
    public String listAllLabelsV(PointV v){

        //获取节点v的uri
        String vURI= v.getUri();

        //根据uri得到该节点的所有标签
        String vLabels = pointVRepository.getAllLabels(vURI);

        return vLabels;

    }

    //返回p类型节点的数量
    @Override
    public int getNumP(){

        int num = pointPRepository.getCountP();
        return num;

    }

    //分页获取p类型节点
    @Override
    public List<PointP> getPointP(int pageIndex, int pageSize){

        return pointPRepository.getPointP(pageIndex,pageSize);

    }

    //返回v类型节点的数量
    @Override
    public int getNumV(){

        return pointVRepository.getCountV();

    }

    //返回节点P的关键字列表
    @Override
    public String listKeyWordsListP(PLabelsDto p) throws IOException {

        StringBuilder sb = new StringBuilder();

        keyWords.keyWordsList = new HashSet<>();

        long id = p.pointP.getId();

        sb.append("p"+ id +":{");


        //节点p的uri值
        String uri = p.pointP.getUri();


        keyWords.labelToKeyWordsList(p.labels);


        keyWords.uriToKeyWordsList(uri);

        //去除停用词
        stopWords.setStopWordsList();
        keyWords.keyWordsList = stopWords.stopWordsRemove(keyWords.keyWordsList);

        //输出关键字字典
        for(Iterator<String> it = keyWords.keyWordsList.iterator(); it.hasNext() ; ){
            String str = it.next();
            if(it.hasNext()){
                sb.append(str + ",");
            }
            else{
                sb.append(str + "}");
            }
        }

        return sb.toString();
    }

    //返回节点v的关键字列表
    @Override
    public String listKeyWordsListV(VLabelsDto v) throws IOException {

        StringBuilder sb = new StringBuilder();

        keyWords.keyWordsList = new HashSet<>();

        long id = v.pointV.getId();

        sb.append("v"+ id +":{");


        //节点v的uri值
        String uri = v.pointV.getUri();

        keyWords.labelToKeyWordsList(v.labels);

        keyWords.uriToKeyWordsList(uri);

        //去除停用词
        stopWords.setStopWordsList();
        keyWords.keyWordsList = stopWords.stopWordsRemove(keyWords.keyWordsList);

        //输出关键字字典
        for(Iterator<String> it = keyWords.keyWordsList.iterator(); it.hasNext() ; ){
            String str = it.next();
            if(it.hasNext()){
                sb.append(str + ",");
            }
            else{
                sb.append(str);
            }
        }
        sb.append("}");

        return sb.toString();
    }

    //生成关键字文档
    @Override
    public void setKeyWordsList() throws IOException {

        //清空旧的关键字文档
        keyWords.deleteKeyWordsListAll();


        int pageSize = 1000; //每一页大小
        int pageIndex = 0; //当前页号
        int pageCount = pointVRepository.getCountV(); //v总记录数

        while(pageIndex < pageCount){

            //分页获取pagesize个节点
            List<VLabelsDto> VLabelsList = pointVRepository.getLabelsV(pageIndex,pageSize);

            long startTime = System.currentTimeMillis();    //获取开始时间

            //遍历每一个节点生成关键字文档
            for(VLabelsDto vLabelsDto : VLabelsList){

                String kv = listKeyWordsListV(vLabelsDto);
                long idv = vLabelsDto.pointV.getId();
                String name = "v"+idv;
                keyWords.setKeyWordsList(kv,name);
                keyWords.setKeyWordsListAll(kv);

            }

            long endTime = System.currentTimeMillis();    //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
            System.out.println(pageIndex);    //输出程序运行时间

            //当前页号改变
            pageIndex = pageIndex + pageSize;

        }

        pageIndex = 0;
        pageCount = pointPRepository.getCountP();; //p总记录数

        while(pageIndex < pageCount){

            //分页获取pagesize个节点
            List<PLabelsDto> PLabelsList = pointPRepository.getLabelsP(pageIndex,pageSize);

            long startTime = System.currentTimeMillis();    //获取开始时间

            //遍历每一个节点生成关键字文档
            for(PLabelsDto pLabelsDto : PLabelsList){

                String kp = listKeyWordsListP(pLabelsDto);
                long idp = pLabelsDto.pointP.getId();
                String name = "p"+idp;
                keyWords.setKeyWordsList(kp,name);
                keyWords.setKeyWordsListAll(kp);

            }

            long endTime = System.currentTimeMillis();    //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
            System.out.println(pageIndex);

            //当前页号改变
            pageIndex = pageIndex + pageSize;

        }


        System.out.println("关键字文档已生成!");
    }

    //生成关键字文档
    @Override
    public void setKeyWordsListP() throws IOException {

        int pageSize = 1000; //每一页大小
        int pageIndex = 0; //当前页号
        int pageCount = pointPRepository.getCountP();; //p总记录数

        while(pageIndex < pageCount){

            //分页获取pagesize个节点
            List<PLabelsDto> PLabelsList = pointPRepository.getLabelsP(pageIndex,pageSize);

            long startTime = System.currentTimeMillis();    //获取开始时间

            //遍历每一个节点生成关键字文档
            for(PLabelsDto pLabelsDto : PLabelsList){

                String kp = listKeyWordsListP(pLabelsDto);
                long idp = pLabelsDto.pointP.getId();
                String name = "p"+idp;
                keyWords.setKeyWordsList(kp,name);

            }

            long endTime = System.currentTimeMillis();    //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
            System.out.println(pageIndex);

            //当前页号改变
            pageIndex = pageIndex + pageSize;

        }


        System.out.println("关键字文档p已生成!");
    }

    //生成倒排索引
    @Override
    public void setInvertedIndex() throws Exception {

        indexManager.deleteAllDocument();
        indexManager.createIndex();

        System.out.println("倒排索引已生成!");
    }


    //生成空间索引
    @Override
    public void setSpatialIndex() {

        spatialindex.createIndex();
        System.out.println("空间索引已生成!");

    }

    //读取空间索引
    @Override
    public RTree<String, Point> readSpatialIndex(){

        RTree<String, Point> rTree = spatialindex.readIndex();
        System.out.println("空间索引已读取!");
        return rTree;

    }


    //根据关键字查询倒排索引
    @Override
    public String searchKeyword( String keyword ) throws Exception{

       return (indexManager.searchIndex(keyword));

    }


    //根据请求位置获取p节点
    @Override
    public List<Entry<String, Point>> getPointPByDis(RTree<String, Point> rtree, Point point, double maxdistance, int maxcount){

        List<Entry<String, Point>> plist = spatialindex.getPointPByDis(rtree,point,maxdistance,maxcount);


        return plist;

    }

    //根据uri获得深度为3的所有子结点，返回对应深度
    @Override
    public List<SubNodeDto> getSubtree(String uri){

        List<SubNodeDto> subtree = pointPRepository.getSubtree(uri);
        return subtree;

    }

    //根据名称查找节点，例如：p1 --> 标签为p ，id为1
    @Override
    public PointP getPointPByName(String s){
        s = s.substring(1);
        Optional<PointP> o = pointPRepository.findById(Long.parseLong(s));
        PointP p = o.get();
        return p;
    }



}
