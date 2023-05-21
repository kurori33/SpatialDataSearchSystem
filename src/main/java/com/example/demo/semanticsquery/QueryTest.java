package com.example.demo.semanticsquery;

import com.example.demo.dto.SubTreeDto;
import com.example.demo.dto.TestDataDto;
import com.example.demo.dto.TestResultDto;
import com.example.demo.spatialIndex.IndexManager;
import com.example.demo.testdataset.TestDataSet;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class QueryTest {

    //测试数据存储位置
    String resultRoot = System.getProperty("user.dir");
    //测试数据表头
//    String[] headName = {"序号","运行时间","构建关键字集子集时间","构建语义位置时间","其他时间","返回结果数","返回结果根节点","平均松散度","平均空间距离","访问空间索引节点数","构建语义位置数"};
    String[] headName = {"序号","运行时间","构建关键字集子集时间","构建语义位置时间","其他时间","返回结果数","返回结果根节点","平均相似度/松散度","平均相似度","平均松散度","平均空间距离","访问空间索引节点数","构建语义位置数"};

    private TestDataSet testDataSet = new TestDataSet();
    private IndexManager spatialindex = new IndexManager();
    private KSPQuery kspQuery = new KSPQuery();
    private SKGSPQuery skgspQuery = new SKGSPQuery();

    //改变k值的测试
    public void K_Test() throws Exception {

        Integer[] K = {1,3,5,8,10,15,20};

        String resultName ="BSP-k.xls";
        String resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//        initExcel(resultPath,headName,K);
        resultName ="SPP-k.xls";
        resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//        initExcel(resultPath,headName,K);
        resultName ="SP-k.xls";
        resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//        initExcel(resultPath,headName,K);
        resultName ="SKG-SP-k.xls";
        resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
        initExcel(resultPath,headName,K);

        List<TestDataDto> testDataDtos = testDataSet.readExcel();

        //读取空间索引
        RTree<String, Point> rTree = spatialindex.readIndex();
        System.out.println("空间索引已读取!");

        double max=0.8;
        int alph = 2;
        int index = 1;


//        //遍历每一条测试数据
//        for(TestDataDto testDataDto:testDataDtos){
//
//            System.out.println(index);
//
//            String keys = testDataDto.getKeys();
//            String keys_similar = testDataDto.getKeys_similar();
//            String longlat = testDataDto.getLonglat_similar();
//            String[] str = longlat.split(",");
//            double longitude = Double.parseDouble(str[0]);
//            double latitude =  Double.parseDouble(str[1]);

//            kspQuery.initTestData2(keys_similar,longitude,latitude);
//            long prepare_startTime = System.currentTimeMillis();
//            kspQuery.getKeywordsNodes();
//            long prepare_endTime = System.currentTimeMillis();
//            long prepare_time = prepare_endTime-prepare_startTime;
//
//
//            for(Integer k:K){
//                //初始化查询信息
//                kspQuery.initTestData(keys_similar,longitude,latitude,k);
//
//                //bsp算法
//                TestResultDto testResultDto_BSP = kspQuery.BspQuery(rTree);
//                testResultDto_BSP.setIndex(index);
//                testResultDto_BSP.setRuntime_prepare(prepare_time);
//
//                resultName ="BSP-k.xls";
//                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//                writeExcel(resultPath,testResultDto_BSP,index,k);
//
//
//                //spp算法
//                TestResultDto testResultDto_SPP = kspQuery.SppQuery(rTree);
//                testResultDto_SPP.setIndex(index);
//                testResultDto_SPP.setRuntime_prepare(prepare_time);
//
//                resultName ="SPP-k.xls";
//                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//                writeExcel(resultPath,testResultDto_SPP,index,k);
//
//
//                //sp算法
//                TestResultDto testResultDto_SP = kspQuery.SpQuery(rTree,alph);
//                testResultDto_SP.setIndex(index);
//                testResultDto_SP.setRuntime_prepare(prepare_time);
//
//                resultName ="SP-k.xls";
//                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//                writeExcel(resultPath,testResultDto_SP,index,k);
//
//            }
//
//            index++;
//
//        }

        index = 1;

        //遍历每一条测试数据
        for(TestDataDto testDataDto:testDataDtos){

            System.out.println(index);

            if(index>50){
                break;
            }

            String keys = testDataDto.getKeys();
            String keys_similar = testDataDto.getKeys_similar();
            String longlat = testDataDto.getLonglat_similar();
            String[] str = longlat.split(",");
            double longitude = Double.parseDouble(str[0]);
            double latitude =  Double.parseDouble(str[1]);
//
//            for(Integer k:K){
//
//                skgspQuery.initTestData(keys,(float) longitude,(float) latitude,k);
//
//                //skg-sp算法
//                TestResultDto testResultDto_SKGSP = skgspQuery.SkgspQuery(rTree,max);
//                testResultDto_SKGSP.setIndex(index);
//                testResultDto_SKGSP.setRuntime_prepare(0);
//                resultName ="SKG-SP-k.xls";
//                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//                writeExcel(resultPath,testResultDto_SKGSP,index,k);
//
//            }

            skgspQuery.initTestData2(keys_similar,(float) longitude,(float) latitude,K);
            int i=0;
            List<TestResultDto> testResultDtos_SKGSP = skgspQuery.SkgspQuery2(rTree,max);
            for(TestResultDto testResultDto_SKGSP:testResultDtos_SKGSP){
                testResultDto_SKGSP.setIndex(index);
                resultName ="SKG-SP-k.xls";
                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
                writeExcel(resultPath,testResultDto_SKGSP,index,K[i]);
                i++;
            }

            index++;

        }

    }

    //改变k值的测试
    public void K_Test1() throws Exception {

        Integer[] K = {1,3,5,8,10,15,20};

        String resultName ="SKG-SP-k-1.xls";
        String resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
//        initExcel(resultPath,headName,K);

        List<TestDataDto> testDataDtos = testDataSet.readExcel2();

        //读取空间索引
        RTree<String, Point> rTree = spatialindex.readIndex();
        System.out.println("空间索引已读取!");

        double max=0.5;
        int index = 1;
        //遍历每一条测试数据
        for(TestDataDto testDataDto:testDataDtos){

            System.out.println(index);

            if(index>51){
                break;
            }
            if(index==50 || index<18){
                index++;
                continue;
            }

            String keys = testDataDto.getKeys();
            String keys_similar = testDataDto.getKeys_similar();
            String longlat = testDataDto.getLonglat_similar();
            String[] str = longlat.split(",");
            double longitude = Double.parseDouble(str[0]);
            double latitude =  Double.parseDouble(str[1]);


            skgspQuery.initTestData2(keys_similar,(float) longitude,(float) latitude,K);
            int i=0;
            List<TestResultDto> testResultDtos_SKGSP = skgspQuery.SkgspQuery2(rTree,max);
            for(TestResultDto testResultDto_SKGSP:testResultDtos_SKGSP){
                testResultDto_SKGSP.setIndex(index);
                resultName ="SKG-SP-k-1.xls";
                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
                writeExcel(resultPath,testResultDto_SKGSP,index,K[i]);
                i++;
            }

            index++;

        }

    }


    //改变k值的测试
    public void K_Test2() throws Exception {

        Integer[] K = {1,3,5,8,10,15,20};

        String resultName ="SKG-SP-k-2.xls";
        String resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
        initExcel(resultPath,headName,K);

        List<TestDataDto> testDataDtos = testDataSet.readExcel2();

        //读取空间索引
        RTree<String, Point> rTree = spatialindex.readIndex();
        System.out.println("空间索引已读取!");

        double max=0.7;
        int index = 1;
        //遍历每一条测试数据
        for(TestDataDto testDataDto:testDataDtos){

            System.out.println(index);

            if(index>52){
                break;
            }
            if(index==6 || index==17){
                index++;
                continue;
            }

            String keys = testDataDto.getKeys();
            String keys_similar = testDataDto.getKeys_similar();
            String longlat = testDataDto.getLonglat_similar();
            String[] str = longlat.split(",");
            double longitude = Double.parseDouble(str[0]);
            double latitude =  Double.parseDouble(str[1]);


            skgspQuery.initTestData2(keys_similar,(float) longitude,(float) latitude,K);
            int i=0;
            List<TestResultDto> testResultDtos_SKGSP = skgspQuery.SkgspQuery2(rTree,max);
            for(TestResultDto testResultDto_SKGSP:testResultDtos_SKGSP){
                testResultDto_SKGSP.setIndex(index);
                resultName ="SKG-SP-k-2.xls";
                resultPath =resultRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + resultName;
                writeExcel(resultPath,testResultDto_SKGSP,index,K[i]);
                i++;
            }

            index++;

        }

    }

    //初始表格
    public void initExcel(String filePath, String[] headName,Integer[] num){

        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            for(int i=0;i<num.length;i++) {
                //创建表单
                HSSFSheet sheet = workbook.createSheet(num[i]+"");
                //初始化行列索引
                int rowIndex = 0, columnIndex = 0;
                Row row = sheet.createRow(rowIndex);
                for (String head : headName) {
                    Cell cell = row.createCell(columnIndex++);
                    cell.setCellValue(head);
                }
            }
            FileOutputStream outputStream=null;
            //写入表格
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //写入表格
    public void writeExcel(String filePath, TestResultDto testResultDto, int index,int k){

        try {
            //读取文档
            FileInputStream fileInputStream = new FileInputStream(filePath);
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            //读取表单
            HSSFSheet sheet = workbook.getSheet(k+"");

            //创建表格
            writeXls(testResultDto,index,workbook,sheet);

            FileOutputStream outputStream=null;
            //写入表格
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            if (outputStream != null) {
                outputStream.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //创建表格
    public Workbook writeXls(TestResultDto testResultDto, int index,HSSFWorkbook workbook,HSSFSheet sheet){

        try{

            //初始化列索引
            int columnIndex = 0;
//            //创建表头
//            Row row = sheet.createRow(rowIndex++);
//            for (String head : headName) {
//                Cell cell = row.createCell(columnIndex++);
//                cell.setCellValue(head);
//            }
            //创建内容
            Row row = sheet.createRow(index);
            columnIndex = 0;
            //加入序号
            Cell cell = row.createCell(columnIndex++);
            String key = testResultDto.getIndex()+"";
            cell.setCellValue(key);
            //加入运行时间
            cell = row.createCell(columnIndex++);
            key = testResultDto.getRuntime()+"";
            cell.setCellValue(key);
            //构建关键字集子集时间
            cell = row.createCell(columnIndex++);
            key = testResultDto.getRuntime_prepare()+"";
            cell.setCellValue(key);
            //构建语义位置时间
            cell = row.createCell(columnIndex++);
            key = testResultDto.getRuntime_sem()+"";
            cell.setCellValue(key);
            //加入其他时间
            cell = row.createCell(columnIndex++);
            key = testResultDto.getRuntime_other()+"";
            cell.setCellValue(key);
            //加入返回结果数
            cell = row.createCell(columnIndex++);
            key = testResultDto.getResult_num()+"";
            cell.setCellValue(key);
            //返回结果根节点
            cell = row.createCell(columnIndex++);
            key = testResultDto.getResult_root();
            cell.setCellValue(key);
            //平均相似度/松散度
            cell = row.createCell(columnIndex++);
            key = testResultDto.getF_score_ave()+"";
            cell.setCellValue(key);
            //平均相似度
            cell = row.createCell(columnIndex++);
            key = testResultDto.getSem_score_ave()+"";
            cell.setCellValue(key);
            //平均松散度
            cell = row.createCell(columnIndex++);
            key = testResultDto.getLtp_score_ave()+"";
            cell.setCellValue(key);
            //平均空间距离
            cell = row.createCell(columnIndex++);
            key = testResultDto.getDis_score_ave()+"";
            cell.setCellValue(key);
            //访问空间索引节点数
            cell = row.createCell(columnIndex++);
            key = testResultDto.getDis_index_num()+"";
            cell.setCellValue(key);
            //构建语义位置数
            cell = row.createCell(columnIndex);
            key = testResultDto.getSem_num()+"";
            cell.setCellValue(key);

            return workbook;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

}
