package com.example.demo;

import com.example.demo.dataGenerator.RandVector;
import com.example.demo.dto.TestDataDto;
import com.example.demo.dto.TestResultDto;
import com.example.demo.entity.PointP;
import com.example.demo.repository.PointPRepository;
import com.example.demo.repository.PointVRepository;
import com.example.demo.semanticsquery.KSPQuery;
import com.example.demo.semanticsquery.QueryTest;
import com.example.demo.semanticsquery.SKGSPQuery;
import com.example.demo.service.ResourceService;
import com.example.demo.spatialIndex.IndexManager;
import com.example.demo.testdataset.TestDataSet;
import com.example.demo.topkskyline.EBNLTopK;
import com.example.demo.wordneighborhoods.AlphaRadiusWordN;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Point;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ResourceService resourceService;

    private KSPQuery semanticsQuery = new KSPQuery();

    @Autowired
    private PointVRepository pointVRepository;

    @Autowired
    private PointPRepository pointPRepository;


    private IndexManager indexManager = new IndexManager();

    private com.example.demo.invertedindex.IndexManager i = new com.example.demo.invertedindex.IndexManager();

    private AlphaRadiusWordN alphaRadiusWordN = new AlphaRadiusWordN();

    private TestDataSet testDataSet = new TestDataSet();
    private IndexManager spatialindex = new IndexManager();
    private SKGSPQuery skgspQuery = new SKGSPQuery();

    QueryTest queryTest = new QueryTest();

    @Test
    public void searchTest() throws Exception {
        resourceService.setKeyWordsListP();
//        queryTest.K_Test1();
////        queryTest.K_Test2();
//        queryTest.K_Test();

    }

    @Test
    public void GetDataSetTest() throws Exception {

        int num = 100;
        int keywordsnum = 1;
        int factor = 2;
        int maxSimilarNum = 1; //随机替换num以内个关键字为近似词
        double maxradius = 1; //生成的随机点到根节点的最大距离,千米
      testDataSet.createTestDataSet(num,keywordsnum,factor,maxSimilarNum,maxradius);

    }

    @Test
    public void ReadDataSetTest() throws Exception {


        Integer[] K = {1,3,5,8,10,15,20};
        //读取空间索引
        RTree<String, Point> rTree = spatialindex.readIndex();
        System.out.println("空间索引已读取!");

        double max=0.6;
        int index = 17;
        int k=1;
        String keys = "islamic,landlocked,developing,developed,cooperation";
        String keys_similar = "islamic,landlocked,develop,developed,cooperation";
        double longitude = 33.753464348594385;
        double latitude =  9.973213271702791;

        skgspQuery.initTestData(keys,(float) longitude,(float) latitude,k);

        //skg-sp算法
        TestResultDto testResultDto_SKGSP = skgspQuery.SkgspQuery(rTree,max);
        testResultDto_SKGSP.setIndex(index);
        testResultDto_SKGSP.setRuntime_prepare(0);
        String resultName ="SKG-SP-k.xls";
        String resultRoot = System.getProperty("user.dir");
        String resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
        queryTest.writeExcel(resultPath,testResultDto_SKGSP,index,k);


    }


    @Test
    public void
    indexTest() throws Exception {

//        long a = 2595303;
////        PointP p = pointPRepository.findById(a).orElse(null);
////        System.out.println( p == null);

//        resourceService.setKeyWordsListP();

//          semanticsQuery.initIndex();

        String w1 = "germanic";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        System.out.println(skgspQuery.getWordEmbedding(w1,client));


    }

    @Test
    public void setNeighborhoods() throws Exception {

//        RTree<String, Point> rTree = resourceService.readSpatialIndex();
//        int alph = 2;
//        alphaRadiusWordN.writeRtreeIndex(rTree.root().get());
//        alphaRadiusWordN.readRtreeIndex();
//        alphaRadiusWordN.initNeighborhoodsP(alph);
//        alphaRadiusWordN.initNeighborhoodsN(rTree);
//        alphaRadiusWordN.initRoot(rTree);


    }



    @Test
    public void setSpatialIndex() throws Exception {

//        RTree<String, Point> rtree = indexManager.readIndex();
//
//        float requestLongitude = 38;
//        float requestLatitude = 35;
//        double distance = 1; //空间查询最大半径
//        int maxcount = 1000; //空间查询最大返回数量
//
//        //查询位置
//        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);
//
//        //返回结果
//        List<Entry<String, Point>> plist = rtree.nearest(point,distance,maxcount).toList().toBlocking().single();
//
//        System.out.println(plist);
//       System.out.println(rtree.size());

//        rtree.visualize(6000,6000).save("D:/学习/毕业设计/笔记/mytree.png");
//        System.out.println(rtree.asString());


    }

    @Test
    public void runPY() throws Exception {

        RTree<String, Point> rTree = resourceService.readSpatialIndex();

        //初始化用户请求
        skgspQuery.testData();
        double max = 0.6;
        skgspQuery.SkgspQuery(rTree,max);

    }



    @Test
    public void topk() throws Exception {


        ArrayList<Object> input = new ArrayList<>();

        RandVector r1 = new RandVector(new double[] { 0.3, 0.3,0.5 });
        r1.setName("r1");
        input.add(r1);
        r1 = new RandVector(new double[] { 0.2, 0.2,0.3 });
        r1.setName("r2");
        input.add(r1);
        r1 = new RandVector(new double[] { 0.3, 0.3,0.4 });
        r1.setName("r3");
        input.add(r1);
        ArrayList<Object> i = new ArrayList<>();
        i.addAll(input);

        int topK = 2;
        EBNLTopK ebnl = new EBNLTopK(input, topK);
        ArrayList<Object> result = ebnl.getResult();
        for(Object o:result){
            System.out.println(o);
        }

    }


}
