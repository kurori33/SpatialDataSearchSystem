package com.example.demo.testdataset;


import com.example.demo.dto.QueryProcessDto;
import com.example.demo.dto.SubNodeDto;
import com.example.demo.dto.TestDataDto;
import com.example.demo.entity.PointP;
import com.example.demo.repository.PointPRepository;
import com.example.demo.service.ResourceService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class TestDataSet {

    private static ResourceService resourceService;
    @Autowired
    public void setResourceService(ResourceService resourceService){
        TestDataSet.resourceService = resourceService;
    }

    private static PointPRepository pointPRepository;

    @Autowired
    public void setPointPRepository(PointPRepository pointPRepository){
        TestDataSet.pointPRepository = pointPRepository;
    }

    //测试数据存储位置
    String excelRoot = System.getProperty("user.dir");
    String excelName ="testdata.xls";
    String excelName2 ="testdata2.xls";
    String excelName1 ="testdata1.xls";
    String excelPath =excelRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + excelName;
    String excelPath2 =excelRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + excelName2;
    String excelPath1 =excelRoot + File.separator + "src\\main\\java\\com\\example\\demo\\testdataset" +File.separator  + excelName1;
    //测试数据表头
    String[] headName = {"序号","根节点URI","根节点ID","根节点经纬度","查询关键字","查询关键字(近似词替换)","近似词替换的个数","查询位置经纬度","查询位置和根节点的距离(km)"};

    //生成num条测试数据，例如：hello，world
    public void createTestDataSet(int num , int keywordsnum , int factor, int maxSimilarNum, double maxradius) throws IOException {

        //存放生成的测试数据
        List<TestDataDto> testDataList = new LinkedList<>();
        //判断关键字是否重复
        Set<String> keySet = new HashSet<>();

        //生成数据下限
        int min = keywordsnum/2;

        //生成数据上限
        int max = keywordsnum*factor;

        CloseableHttpClient client = HttpClientBuilder.create().build();

        //获取num条测试数据
        for(int i=num ; i>0 ; i--){


           int rand = (int)(1+Math.random()*(936495-1+1));

            //随机选择一个p节点
            PointP pointP = pointPRepository.getRandPointP(rand);

            if(pointP == null){
                i++;
                continue;
            }

            //获得该节点的uri
            String ppUri = pointP.getUri();

            //获得其所有子节点
            List<SubNodeDto> ppSubtree = resourceService.getSubtree(ppUri);

            //获得所有子节点的个数
            int ppSubtreeNum = ppSubtree.size();

            //如果该p点可达的节点小于下限，丢弃该点
            if(ppSubtree.size() < min){
                i++;
                continue;
            }

            //随机获得最多keywordsnum个节点
            List<Integer> listIndex = getRandNode(min,max,ppSubtreeNum);

            //存放遍历获得节点的关键字
            Set<String> keys = new HashSet<String>();

            //遍历获得的节点
            Iterator<Integer> iter = listIndex.iterator();
            while (iter.hasNext()) {
                int item = iter.next();
                SubNodeDto std = ppSubtree.get(item);

                //获得节点的关键字
                String root = System.getProperty("user.dir");
                root= root.substring(0,root.lastIndexOf("\\"));

                String fileName = "p"+std.getSubNodeID()+".txt";
                String filePath = root + File.separator + "document" +File.separator  + fileName;
                File file = new File(filePath);

                if (!file.exists()) {
                    fileName = "v"+std.getSubNodeID()+".txt";
                    filePath = root + File.separator + "document" +File.separator  + fileName;
                }

                BufferedReader br = new BufferedReader(new FileReader(filePath));

                //获得子节点的关键字列表
                String pvKeys = br.readLine();
                br.close();
                pvKeys = pvKeys.substring(0,pvKeys.indexOf("}"));
                String temp = pvKeys.substring(0, pvKeys.indexOf("{"));
                pvKeys = pvKeys.substring(temp.length()+1, pvKeys.length());
                String[] strArray = pvKeys.split(",");


                //加入keys
                for(int j=0; j<strArray.length; j++){

                    if(!strArray[j].equals("")){
                        keys.add(strArray[j]);
                    }

                }

            }

            String temp = getRandKeys(keywordsnum,keys,client);

            //如果本次生成的测试数据集为空或者重复
            if(temp==null || !keySet.add(temp)){
                i++;
                continue;
            }
            int similarNum = (int)(1+Math.random()*(maxSimilarNum-1+1));
            String temp_similar = getSimilarKeys(temp,similarNum);
            String longlat = pointP.getLongitude()+","+pointP.getLatitude();
            double distance = (Math.random()*maxradius);
            String longlat_similar = getSimilarLonglat(longlat,distance);
            String[] strings = longlat_similar.split(",");

            TestDataDto testData = new TestDataDto();
            testData.setNum(num-i+1);
            testData.setUri(ppUri);
            testData.setId(pointP.getId());
            testData.setKeys(temp);
            testData.setKeys_similar(temp_similar);
            testData.setKeys_similar_num(similarNum);
            testData.setLatitude(pointP.getLatitude());
            testData.setLongitude(pointP.getLongitude());
            testData.setLonglat(longlat);
            testData.setLatitude_similar(Float.parseFloat(strings[1]));
            testData.setLongitude_similar(Float.parseFloat(strings[0]));
            testData.setDistance(distance);
            testData.setLonglat_similar(longlat_similar);

            testDataList.add(testData);

            System.out.println("测试数据"+testData.getNum()+":{"+testData.getKeys()+"}" + testData.getUri());
        }

        //写入excel文件
        writeExcel(excelPath,headName,testDataList);

    }

    //获得给定范围内的随机点
    public String getSimilarLonglat(String Longlat, double distance){

        StringBuilder Longlat_s = new StringBuilder();

        String[] str = Longlat.split(",");

        double longitude = Double.parseDouble(str[0]);
        double latitude =  Double.parseDouble(str[1]);

        double angle = (1+Math.random()*359);

        double longitude_s = longitude + (distance * Math.sin(Math.toRadians(angle))) / (111 * Math.cos(Math.toRadians(latitude)));
        double latitude_s =  latitude + (distance * Math.cos(Math.toRadians(angle))) / 111;


        Longlat_s.append(longitude_s+","+latitude_s);

        return Longlat_s.toString();
    }

    //使用近似词代替查询关键字序列中的关键字
    public String getSimilarKeys(String keys,int num) throws IOException {

        //保存结果
        StringBuilder similarKeys = new StringBuilder();

        //获取关键字列表
        String[] keyArray = keys.split(",");

        //随机生成要替换的关键字的序号(不重复)
        Integer[] rands = new Integer[num];
        for(int i=0;i<num;i++){

            //随机生成一个数据
            int rand = (int)(Math.random()*keyArray.length);

            //判断是否重复
            boolean flag = true;
            for(int j=0;j<i;j++){
                if(rand == rands[j]){
                    flag = false;
                    break;
                }
            }

            //如果重复，再一次随机生成
            if(!flag){
                i--;
                continue;
            }

            //如果找不到近义词，再一次随机生成
            String SimilarKey = getSimilarKey(keyArray[rand]);
            if(SimilarKey.equals("")){
                i--;
                continue;
            }
            else{
                rands[i]=rand;
                keyArray[rand] = SimilarKey;
            }
        }

        for(String s:keyArray){
           similarKeys.append(s+",");
        }
        similarKeys.deleteCharAt(similarKeys.lastIndexOf(","));
        return  similarKeys.toString();

    }

    //获得近义词
    public String getSimilarKey(String key) throws IOException {

        String keyMostSimilar;

        //使用curl命令获取服务器端的词向量
        String address = "http://10.196.80.173:5000/word2vec/most_similar?positive="+key+"&topn=1";
        ProcessBuilder pb = new ProcessBuilder("curl",address);
        Process p = pb.start();
        InputStream is = p.getInputStream();

        StringBuilder result = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            try {
                is.close();
                bufferedReader.close();
            } catch (Exception e1) {
            }
        }

        keyMostSimilar = result.toString();

        if(keyMostSimilar.equals("{\"message\": \"Internal Server Error\"}")){

            keyMostSimilar="";

        }else{

            String temp = keyMostSimilar.substring(0,keyMostSimilar.indexOf("\""));
            keyMostSimilar = keyMostSimilar.substring(temp.length()+1);
            keyMostSimilar = keyMostSimilar.substring(0,keyMostSimilar.indexOf("\""));

        }

        return keyMostSimilar;

    }
    //读取表格
    public List<TestDataDto> readExcel(){

        List<TestDataDto> testDataDtos = new LinkedList<>();
        Workbook workbook = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(excelPath);
            workbook = new HSSFWorkbook(fileInputStream);
        } catch (IOException e) {
        e.printStackTrace();
        }

        assert workbook != null;
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {

            Sheet sheet = workbook.getSheetAt(sheetNum); // 获取表格

            //初始化行列索引
            int rowNum = sheet.getPhysicalNumberOfRows() - 1;//获取有记录的行数，即：最后有数据的行是第n行，前面有m行是空行没数据，则返回n-m；

            //遍历每一行测试数据
            for(int rowIndex=1;rowIndex<=rowNum;rowIndex++){

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                TestDataDto testDataDto = new TestDataDto();
                //处理Cell
                int columnIndex = 4; //查询关键字
                Cell cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys = cell.getStringCellValue();
                    testDataDto.setKeys(keys);
                }
                columnIndex = 5; //查询关键字(近似词替换)
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys_similar = cell.getStringCellValue();
                    testDataDto.setKeys_similar(keys_similar);
                }
                columnIndex = 7; //查询位置
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String longlat = cell.getStringCellValue();
                    testDataDto.setLonglat_similar(longlat);
                }
                testDataDtos.add(testDataDto);

            }

        }

        return testDataDtos;

    }

    //读取表格
    public List<TestDataDto> readExcel1(){

        List<TestDataDto> testDataDtos = new LinkedList<>();
        Workbook workbook = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(excelPath1);
            workbook = new HSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert workbook != null;
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {

            Sheet sheet = workbook.getSheetAt(sheetNum); // 获取表格

            //初始化行列索引
            int rowNum = sheet.getPhysicalNumberOfRows() - 1;//获取有记录的行数，即：最后有数据的行是第n行，前面有m行是空行没数据，则返回n-m；

            //遍历每一行测试数据
            for(int rowIndex=1;rowIndex<=rowNum;rowIndex++){

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                TestDataDto testDataDto = new TestDataDto();
                //处理Cell
                int columnIndex = 4; //查询关键字
                Cell cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys = cell.getStringCellValue();
                    testDataDto.setKeys(keys);
                }
                columnIndex = 5; //查询关键字(近似词替换)
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys_similar = cell.getStringCellValue();
                    testDataDto.setKeys_similar(keys_similar);
                }
                columnIndex = 7; //查询位置
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String longlat = cell.getStringCellValue();
                    testDataDto.setLonglat_similar(longlat);
                }
                testDataDtos.add(testDataDto);

            }

        }

        return testDataDtos;

    }

    //读取表格
    public List<TestDataDto> readExcel2(){

        List<TestDataDto> testDataDtos = new LinkedList<>();
        Workbook workbook = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(excelPath2);
            workbook = new HSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert workbook != null;
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {

            Sheet sheet = workbook.getSheetAt(sheetNum); // 获取表格

            //初始化行列索引
            int rowNum = sheet.getPhysicalNumberOfRows() - 1;//获取有记录的行数，即：最后有数据的行是第n行，前面有m行是空行没数据，则返回n-m；

            //遍历每一行测试数据
            for(int rowIndex=1;rowIndex<=rowNum;rowIndex++){

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                TestDataDto testDataDto = new TestDataDto();
                //处理Cell
                int columnIndex = 4; //查询关键字
                Cell cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys = cell.getStringCellValue();
                    testDataDto.setKeys(keys);
                }
                columnIndex = 5; //查询关键字(近似词替换)
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String keys_similar = cell.getStringCellValue();
                    testDataDto.setKeys_similar(keys_similar);
                }
                columnIndex = 7; //查询位置
                cell = row.getCell(columnIndex);
                if(cell!=null){
                    String longlat = cell.getStringCellValue();
                    testDataDto.setLonglat_similar(longlat);
                }
                testDataDtos.add(testDataDto);

            }

        }

        return testDataDtos;

    }

    //写入表格
    public void writeExcel(String filePath, String[] headName, List<TestDataDto> testDataDtos){
        //创建表格
        Workbook workbook = writeXls(headName,testDataDtos);
        FileOutputStream outputStream=null;
        try {
            //写入表格
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    //关闭写入
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("测试数据生成成功！");

    }
    //创建表格
    public Workbook writeXls(String[] headName, List<TestDataDto> testDataDtos){

        try{
            //创建文档
            HSSFWorkbook workbook = new HSSFWorkbook();
            //创建表单
            HSSFSheet sheet = workbook.createSheet("testData");
            //初始化行列索引
            int rowIndex = 0, columnIndex = 0;
            //创建表头
            Row row = sheet.createRow(rowIndex++);
            for (String head : headName) {
                Cell cell = row.createCell(columnIndex++);
                cell.setCellValue(head);
            }
            //创建内容
            if (testDataDtos != null && !testDataDtos.isEmpty()) {

                for (TestDataDto testDataDto : testDataDtos){
                    row = sheet.createRow(rowIndex++);
                    columnIndex = 0;
                    //加入序号
                    Cell cell = row.createCell(columnIndex++);
                    String key = testDataDto.getNum()+"";
                    cell.setCellValue(key);
                    //加入根节点uri
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getUri();
                    cell.setCellValue(key);
                    //加入根节点id
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getId()+"";
                    cell.setCellValue(key);
                    //加入根节点经纬度
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getLonglat();
                    cell.setCellValue(key);
                    //加入查询关键字
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getKeys();
                    cell.setCellValue(key);
                    //加入查询关键字(近似词替换)
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getKeys_similar();
                    cell.setCellValue(key);
                    //近似词替换数量
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getKeys_similar_num()+"";
                    cell.setCellValue(key);
                    //加入查询位置经纬度
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getLonglat_similar();
                    cell.setCellValue(key);
                    //加入查询位置和根节点的距离(km)
                    cell = row.createCell(columnIndex++);
                    key = testDataDto.getDistance()+"";
                    cell.setCellValue(key);
                }
            }

            return workbook;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }


    //判断关键字是否存在词向量
    public boolean hasWordEmbedding(String temp, CloseableHttpClient client) throws IOException {

        boolean flag = true;

        //使用curl命令获取服务器端的词向量
        String address = "http://10.196.80.173:5000/word2vec/model?word="+temp;
        String result = "";
        HttpGet request = new HttpGet(address);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream stream = entity.getContent()) {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result = result+line;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(result.equals("null") || result.length()<=2 || result.equals("{\"message\": \"Internal Server Error\"}")){
            flag = false;
        }
        else{
            flag = true;
        }

        return flag;

    }


    //判断查询结果是否太多
    public boolean isOverFlow(String keyword) throws IOException {

        String rootI =  "D:\\学习\\毕业设计\\demo";
        String pathI = rootI + File.separator + "src\\main\\java\\com\\example\\demo\\invertedindex\\index"; //索引库位置

        boolean flag = false;

        Directory directory = FSDirectory.open( new File(pathI).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = new TermQuery(new Term("content", keyword));
        int maxnum = 10000; //查询返回的最大记录数
        TopDocs topDocs = indexSearcher.search(query, maxnum);
        long num = topDocs.totalHits.value;

        if(num>maxnum){
            flag = true;
        }

        return flag;

    }

    //随机获得keywordsnum个关键字
    public String getRandKeys(int keywordsnum,Set<String> keys, CloseableHttpClient client ) throws IOException {

        //随机的关键字列表
        StringBuilder randKeys = new StringBuilder();

        //如果当前关键字个数不够，返回null
        if(keys.size()<keywordsnum){
            return null;
        }

        //随机获得关键字
        int i=keywordsnum;
        for(String str:keys){
            if(i==0){break;}
            //判断关键字是否有词向量
            if(!hasWordEmbedding(str,client)){
                continue;
            }
            //判断是否在倒排索引中过多出现
            if(isOverFlow(str)){
                continue;
            }
            randKeys.append(str + ",");
            i--;
        }

        if(i!=0){
            return null;
        }

        randKeys.deleteCharAt(randKeys.lastIndexOf(","));
        return randKeys.toString();
    }

    //随机获得最多keywordsnum个节点
    List<Integer> getRandNode(int min,int max,int subTreeNum){

        Set<Integer> randNode = new HashSet<Integer>();

        //获得min-max范围内随机的节点个数
        Random rand =new Random();
        int nodeNum;
        if(subTreeNum >= max){
            nodeNum=rand.nextInt(max-min+1)+min;
        }
        else{
            nodeNum=rand.nextInt(subTreeNum-min+1)+min;
        }


        //获得1-min范围内随机的节点个数
        Random rand2 =new Random();
        int nodeNum2;
        if(nodeNum >= min*2){
            if(min==0){
                nodeNum2=rand2.nextInt(min*2+1)+1;
            }
            else{
                nodeNum2=rand2.nextInt(min*2)+1;
            }
        }
        else{
            nodeNum2=rand2.nextInt(nodeNum)+1;
        }

        Random rand3 = new Random();

        //随机获得nodeNum个节点的序号
        while(randNode.size()<nodeNum){
            int tempNum = rand3.nextInt(subTreeNum);
            randNode.add(tempNum);
        }

        //从nodeNum个节点的序号中随机获得nodeNum2个节点的序号
        List<Integer> list = new ArrayList(randNode);
        while(list.size()>nodeNum2){
            int randomIndex = new Random().nextInt(list.size());
            Integer randomItem = list.get(randomIndex);
            list.remove(randomItem);
        }

        return list;

    }
}
