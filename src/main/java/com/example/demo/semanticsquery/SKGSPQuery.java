package com.example.demo.semanticsquery;

import com.example.demo.dataGenerator.RandVector;
import com.example.demo.dto.TestResultDto;
import com.example.demo.service.ResourceService;
import com.example.demo.topkskyline.EBNLTopK;
import com.example.demo.wordneighborhoods.AlphaRadiusWordN;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

@Component
public class SKGSPQuery {

    private static ResourceService resourceService;

    @Autowired
    public void setResourceService(ResourceService resourceService){
        SKGSPQuery.resourceService = resourceService;
    }


    int k; // top k
    Integer[] K; //top k
    String  requestKeyString; //请求关键字列表_字符串形式
    ArrayList<String> requestKeyList; //请求关键字列表_字符串数组形式
    float requestLongitude; //请求用户所在经度
    float requestLatitude; //请求用户所在纬度
    double maxdistance = 1000; //空间查询最大半径
    int maxcount = 200; //空间查询最大返回数量


    //接收用户请求
    public void initTestData(String keys,float longitude,float latitude,int num) throws Exception {

        requestLongitude = longitude;
        requestLatitude = latitude;
        String[] keyArray = keys.split(",");
        requestKeyList = new ArrayList<String>(Arrays.asList(keyArray));
        k = num;

    }

    //接收用户请求2
    public void initTestData2(String keys,float longitude,float latitude,Integer[] num) throws Exception {

        requestLongitude = longitude;
        requestLatitude = latitude;
        String[] keyArray = keys.split(",");
        requestKeyList = new ArrayList<String>(Arrays.asList(keyArray));
        K = num;

    }

    //skg-sp算法,输出队列hk
    public List<TestResultDto> SkgspQuery2(RTree<String, Point> rTree, double max) throws Exception {

        List<TestResultDto> testResultDtos = new LinkedList<>(); //存放测试结果
        long runtime=0; ////运行时间
        long runtime_sem = 0; //构建语义位置时间
        long runtime_other; //其他时间
        int result_num; //实际返回结果数
        String result_root=""; //返回结果根节点
        double sem_score_ave = 0; //平均相似度
        double ltp_score_ave = 0; //平均松散度
        double f_scp_score_ave = 0;  //平均相似度/松散度
        double dis_score_ave = 0; //平均空间距离
        int dis_index_num = 0; //访问空间索引节点数

        int sem_num = 0; //构建语义位置数

        long startTime = System.currentTimeMillis();

        CloseableHttpClient client = HttpClientBuilder.create().build();

        //查询位置
        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);

//        Map<String,String> semAndltpMap = new HashMap<>(); //存储节点的相似度与松散度，键为节点名


        //根据查询地点，获得附近地理位置节点，按距离升序排序
        List<Entry<String, Point>> plist = resourceService.getPointPByDis(rTree,point,maxdistance,maxcount);

        ArrayList<Object> skylinelist = new ArrayList<>();

        Map<String,String> skynode = new HashMap<>();

        long endTime = System.currentTimeMillis();
        runtime = runtime + endTime - startTime;

        //按距离升序遍历地理位置节点
        for(Entry<String, Point> it : plist){

            startTime = System.currentTimeMillis();

            dis_index_num++;

            //获取当前地理位置节点的名字
            String s = it.value();

            //获取当前地理位置节点距离查询地点的空间位置
            double distance = it.geometry().distance(point);
            distance = (double)Math.round(distance*10000)/10000;

            endTime = System.currentTimeMillis();
            runtime = runtime + endTime - startTime;

            //获取当前地理位置同查询关键字的语义相关度
            String semStr = getSemRelevanceKeys(s,max,client);
            String[] semStrs = semStr.split(",");
            double semRelevance = Double.parseDouble(semStrs[0]);

            runtime=runtime+Long.parseLong(semStrs[1]);
            runtime_sem=runtime_sem+Long.parseLong(semStrs[1]);

            if(semRelevance == -1){
                continue;
            }

            sem_num++;

            if(semRelevance<max){
                continue;
            }

            double LTp = Double.parseDouble(semStrs[2]);

            double f = semRelevance/LTp;

            startTime = System.currentTimeMillis();

            RandVector temp = new RandVector(new double[] { f, distance });
            temp.setName(s);
            skylinelist.add(temp);

            skynode.put(s,f+","+distance+","+semRelevance+","+LTp);

            endTime = System.currentTimeMillis();
            runtime = runtime + endTime - startTime;

        }

        List<ArrayList<Object>> skylinelists = new ArrayList<>();

        for(int i=0;i<K.length;i++){
            ArrayList<Object> temp = new ArrayList<>();
            temp.addAll(skylinelist);
            skylinelists.add(temp);
        }

        for(int i=0;i<K.length;i++){

            result_root="";
            sem_score_ave = 0;
            dis_score_ave = 0;
            ltp_score_ave = 0;

            k=K[i];
            startTime = System.currentTimeMillis();
            EBNLTopK ebnl = new EBNLTopK(skylinelists.get(i), k);
            ArrayList<Object> result = ebnl.getResult();


            endTime = System.currentTimeMillis();
            runtime = runtime + endTime - startTime;
            runtime_other = runtime - runtime_sem;

            result_num = result.size();
            if(result_num==0){
                sem_score_ave = -1;
                dis_score_ave = -1;
                result_root = "null";
            }
            else{
                for(Object o:result){

                    String temp = skynode.get(o.toString());
                    String[] temps = temp.split(",");

                    f_scp_score_ave = f_scp_score_ave + Double.parseDouble(temps[0]);
                    dis_score_ave = dis_score_ave + Double.parseDouble(temps[1]);
                    sem_score_ave = sem_score_ave + Double.parseDouble(temps[2]);
                    ltp_score_ave = ltp_score_ave + Double.parseDouble(temps[3]);

                    System.out.println(temps[0]+"   "+temps[1]+"   "+temps[2]+"   "+temps[3]);

                    if(result_root.equals("")){
                        result_root = o.toString();
                    }else{
                        result_root = o.toString() + "," + result_root;
                    }
                }
                f_scp_score_ave = f_scp_score_ave/result_num;
                dis_score_ave = dis_score_ave/result_num;
                sem_score_ave = sem_score_ave/result_num;
                ltp_score_ave = ltp_score_ave/result_num;


            }

            TestResultDto testResultDto = new TestResultDto(); //存放测试结果
            testResultDto.setRuntime(runtime);
            testResultDto.setRuntime_sem(runtime_sem);
            testResultDto.setRuntime_other(runtime_other);
            testResultDto.setResult_num(result_num);
            testResultDto.setResult_root(result_root);
            testResultDto.setF_score_ave(f_scp_score_ave);
            testResultDto.setLtp_score_ave(ltp_score_ave);
            testResultDto.setSem_score_ave(sem_score_ave);
            testResultDto.setDis_score_ave(dis_score_ave);
            testResultDto.setDis_index_num(dis_index_num);
            testResultDto.setSem_num(sem_num);
            testResultDto.setResult(result);
            testResultDto.setRuntime_prepare(0);
            System.out.println(runtime + "    "+runtime_sem+"    "+runtime_other);

            testResultDtos.add(testResultDto);


        }

        return testResultDtos;

    }


    //skg-sp算法,输出队列hk
    public TestResultDto SkgspQuery(RTree<String, Point> rTree, double max) throws Exception {

        TestResultDto testResultDto = new TestResultDto(); //存放测试结果
        long runtime=0; ////运行时间
        long runtime_sem = 0; //构建语义位置时间
        long runtime_other; //其他时间
        int result_num; //实际返回结果数
        String result_root=""; //返回结果根节点
        double sem_score_ave = 0; //平均松散度/相似度
        double dis_score_ave = 0; //平均空间距离
        int dis_index_num = 0; //访问空间索引节点数
        int sem_num = 0; //构建语义位置数

        long startTime = System.currentTimeMillis();

        CloseableHttpClient client = HttpClientBuilder.create().build();

        //查询位置
        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);


        //根据查询地点，获得附近地理位置节点，按距离升序排序
        List<Entry<String, Point>> plist = resourceService.getPointPByDis(rTree,point,maxdistance,maxcount);

        ArrayList<Object> skylinelist = new ArrayList<>();

        Map<String,String> skynode = new HashMap<>();

        long endTime = System.currentTimeMillis();
        runtime = runtime + endTime - startTime;

        //按距离升序遍历地理位置节点
        for(Entry<String, Point> it : plist){

            startTime = System.currentTimeMillis();

            dis_index_num++;

            //获取当前地理位置节点的名字
            String s = it.value();

            //获取当前地理位置节点距离查询地点的空间位置
            double distance = it.geometry().distance(point);
            distance = (double)Math.round(distance*10000)/10000;

            endTime = System.currentTimeMillis();
            runtime = runtime + endTime - startTime;

            //获取当前地理位置同查询关键字的语义相关度
            String semStr = getSemRelevanceKeys(s,max,client);
            String[] semStrs = semStr.split(",");
            double semRelevance = Double.parseDouble(semStrs[0]);
            runtime=runtime+Long.parseLong(semStrs[1]);
            runtime_sem=runtime_sem+Long.parseLong(semStrs[1]);
            sem_num++;

            if(semRelevance<max){
                continue;
            }

            startTime = System.currentTimeMillis();

            RandVector temp = new RandVector(new double[] { semRelevance, distance });
            temp.setName(s);
            skylinelist.add(temp);

            skynode.put(s,semRelevance+","+distance);

            endTime = System.currentTimeMillis();
            runtime = runtime + endTime - startTime;

            System.out.println(s+"   "+skynode.get(s));

        }

        startTime = System.currentTimeMillis();

        EBNLTopK ebnl = new EBNLTopK(skylinelist, k);
        ArrayList<Object> result = ebnl.getResult();

        endTime = System.currentTimeMillis();
        runtime = runtime + endTime - startTime;

        runtime_other = runtime - runtime_sem;

        result_num = result.size();

        if(result_num==0){
            sem_score_ave = -1;
            dis_score_ave = -1;
            result_root = "null";
        }
        else{
            for(Object o:result){

                String temp = skynode.get(o.toString());
                String[] temps = temp.split(",");

                sem_score_ave = sem_score_ave + Double.parseDouble(temps[0]);
                dis_score_ave = dis_score_ave + Double.parseDouble(temps[1]);

                if(result_root.equals("")){
                    result_root = o.toString();
                }else{
                    result_root = o.toString() + "," + result_root;
                }
            }

            sem_score_ave = sem_score_ave/result_num;
            dis_score_ave = dis_score_ave/result_num;
        }

        testResultDto.setRuntime(runtime);
        testResultDto.setRuntime_sem(runtime_sem);
        testResultDto.setRuntime_other(runtime_other);
        testResultDto.setResult_num(result_num);
        testResultDto.setResult_root(result_root);
        testResultDto.setSem_score_ave(sem_score_ave);
        testResultDto.setDis_score_ave(dis_score_ave);
        testResultDto.setDis_index_num(dis_index_num);
        testResultDto.setSem_num(sem_num);
        testResultDto.setResult(result);

        System.out.println(runtime + "    "+runtime_sem+"    "+runtime_other);

        return testResultDto;


    }


    //计算位置节点关键字邻域集和查询关键字之间的语义相似度
    public String getSemRelevanceKeys(String s,double max,CloseableHttpClient client) throws IOException {

        long runTime = 0;

        long startTime = System.currentTimeMillis();    //获取开始时间

        //语义相似度
        double semRelevance = 0;

        //初始化松散度
        int LTp = 1;

        //拷贝请求关键字列表
        ArrayList<String> rqk = new ArrayList(requestKeyList);

        //获得关键字邻域集
        String WNpString = new AlphaRadiusWordN().readWordNeighborhoodsP(s);
        WNpString = WNpString.substring(0,WNpString.indexOf("}"));
        String temp = WNpString.substring(0, WNpString.indexOf("{"));
        WNpString = WNpString.substring(temp.length()+1);
        Map<String,Integer> WNp = new AlphaRadiusWordN().getStringToMap(WNpString);


        long endTime = System.currentTimeMillis();    //获取结束时间

        runTime = runTime + endTime - startTime;

        int num = 0;

        //遍历请求关键字列表
        for(String key:rqk){

            num++;
            String str = getWordEmbedding(key,client);
            String[] strs = str.split(",");
            runTime = runTime + Long.parseLong(strs[1]);

//            System.out.println(key+"   "+str);

            if(strs[0].equals("false")){
                continue;
            }

            //与当前请求关键字相似度的最高值
            double toprel = 0;

            //与当前请求关键字相似度最高的关键字
            String toprelkey = "";

            //遍历关键字邻域集
            for(String rekey:WNp.keySet()) {

                str = getWordEmbedding(rekey,client);

//                System.out.println(rekey+"   "+str);

                strs = str.split(",");
                runTime = runTime + Long.parseLong(strs[1]);

                if(strs[0].equals("false")){
                    continue;
                }

                String strres = getSimilarity(key,rekey,client);

//                System.out.println(key+"   "+rekey+"   "+strres);

                String[] strsres = strres.split(",");
                double res = Double.parseDouble(strsres[0]);
                runTime = runTime + Long.parseLong(strsres[1]);

                double res2 = Double.parseDouble(new DecimalFormat("#.00000").format(res));

//                System.out.println(key+"   "+rekey+"   "+res);

                //寻找相似度最高值
                if(res == 1 || res2 == 1){
                    toprelkey = rekey;
                    toprel = 1;
                    break;
                }
                else if(res > toprel){
                    toprelkey = rekey;
                    toprel = res;
                }

            }

            semRelevance = (semRelevance*((double) (num-1))+toprel)/(double) num;


            if(!toprelkey.equals("")){
                LTp = LTp + WNp.get(toprelkey);
            }

            if(semRelevance<max){
                semRelevance = -1;
                break;
            }

        }


        System.out.println(s+"   "+semRelevance+"   "+LTp);

        String semstr = semRelevance+","+runTime+","+LTp;

        return semstr;
    }


    //获取两个单词的相似度，不存在返回null
    public String getSimilarity(String w1, String w2,CloseableHttpClient client) throws IOException {

        long runTime = 0;
        long startTime = System.currentTimeMillis();    //获取开始时间

        double similarity;

        //使用curl命令获取服务器端的词向量
        String address = "http://10.196.80.173:5000/word2vec/similarity?w1="+w1+"&w2="+w2;

        String result = "";
        HttpGet request = new HttpGet(address);

        long endTime = System.currentTimeMillis();    //获取开始时间
        runTime = endTime-startTime;

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


        if(result.equals("{\"message\": \"Internal Server Error\"}") || result.equals("")){
            similarity = 0;
        }
        else{
            similarity = Double.parseDouble(result);
        }

        if(similarity<0){
            similarity=0;
        }

        String str = similarity+","+runTime;

        return str;

    }


    //获取单词的词向量表示，若不存在返回null
    public String getWordEmbedding(String key,CloseableHttpClient client) throws IOException {

        long runTime = 0;
        String flag;

        long startTime = System.currentTimeMillis();    //获取开始时间

        //使用curl命令获取服务器端的词向量
        String address = "http://10.196.80.173:5000/word2vec/model?word="+key;

        String result = "";
        HttpGet request = new HttpGet(address);

        long endTime = System.currentTimeMillis();    //获取开始时间
        runTime = endTime-startTime;

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
            flag = "false";
        }
        else{
            flag = "true";
        }

        return(flag+","+runTime);






    }



    //测试
    public void testData() throws Exception {

        requestKeyString = "syria,west";
        String[] strArray = requestKeyString.split(",");
        requestLongitude = 39;
        requestLatitude = 35;
        requestKeyList =new ArrayList<String>(Arrays.asList(strArray)) ;
        k = 3;

        System.out.println("*****************************************************************************");
        System.out.println("查询关键字：" + requestKeyString);
        System.out.println("查询地点：" + requestLongitude + ", " +requestLatitude);
        System.out.println("返回结果数：" + k);
        System.out.println("*****************************************************************************");
    }



}
