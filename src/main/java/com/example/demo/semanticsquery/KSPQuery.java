package com.example.demo.semanticsquery;


import com.example.demo.dto.*;
import com.example.demo.entity.PointP;
import com.example.demo.invertedindex.IndexManager;
import com.example.demo.service.ResourceService;
import com.example.demo.wordneighborhoods.AlphaRadiusWordN;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class KSPQuery {

    private static ResourceService resourceService;
    @Autowired
    public void setResourceService(ResourceService resourceService){
        KSPQuery.resourceService = resourceService;
    }

    AlphaRadiusWordN alphaRadiusWordN = new AlphaRadiusWordN();
    IndexManager indexManager = new IndexManager();
    com.example.demo.spatialIndex.IndexManager spatialindex = new com.example.demo.spatialIndex.IndexManager();
    int k; // top k
    String  requestKeyString; //请求关键字列表_字符串形式
    ArrayList<String> requestKeyList; //请求关键字列表_字符串数组形式
    double requestLongitude; //请求用户所在经度
    double requestLatitude; //请求用户所在纬度
    double maxdistance = 1000; //空间查询最大半径
    int maxcount = 1000; //空间查询最大返回数量

    HashMap<String, String> KeywordsNodesList; //包含查询关键字的节点的集合，键为顶点，值为顶点包含的用户查询关键字信息


    //初始化索引
    public void initIndex() throws Exception {
        //生成关键字文档
        resourceService.setKeyWordsListP();
        //生成倒排索引
        resourceService.setInvertedIndex();
        //生成空间索引
        resourceService.setSpatialIndex();
    }

    //接收用户请求
    public void initData(QueryProcessDto queryProcessDto) throws Exception {

        requestLongitude = queryProcessDto.longitude;
        requestLatitude = queryProcessDto.latitude;
        requestKeyList = queryProcessDto.keyList;
        k = queryProcessDto.k;

    }

    //接收用户请求
    public void initTestData(String keys,double longitude,double latitude,int num) throws Exception {

        requestLongitude = longitude;
        requestLatitude = latitude;
        String[] keyArray = keys.split(",");
        requestKeyList = new ArrayList<String>(Arrays.asList(keyArray));
        k = num;

    }

    //接收用户请求
    public void initTestData2(String keys,double longitude,double latitude) throws Exception {

        requestLongitude = longitude;
        requestLatitude = latitude;
        String[] keyArray = keys.split(",");
        requestKeyList = new ArrayList<String>(Arrays.asList(keyArray));

    }


    //基于倒排索引表和请求关键字构建包含请求关键字的节点的集合
    public void getKeywordsNodes() throws Exception {

        KeywordsNodesList = new HashMap<>(); //初始化哈希表

        for(String temp : requestKeyList){ //循环遍历每一个请求关键字


            String tempNodes = indexManager.searchIndex(temp); //倒排索引查包含当前请求关键字的节点集合_字符串形式

            if(tempNodes == "" || tempNodes == null ){
                continue;
            }

            String[] tempNodeList = tempNodes.split(","); //包含当前请求关键字的节点集合_字符数组形式
            for(String tempnode : tempNodeList){ //循环遍历每一个包含当前请求关键字的节点

                tempnode = tempnode.substring(1);

                if(KeywordsNodesList.containsKey(tempnode)){ //如果表中包含该节点，将当前关键字添加到该键对应的值中

                    KeywordsNodesList.computeIfPresent(tempnode, (key, value) -> value + "," + temp);

                }
                else{ //如果表中没有该节点，以该节点名为主键，当前关键字为值创建键值对
                    KeywordsNodesList.put(tempnode,temp);
                }
            }
        }

        System.out.println("包含请求关键字的节点集合已生成！");
//        遍历包含查询关键字的节点的集合
//        String key;
//        String value;
//        // 获取键集合的迭代器
//        Iterator it = KeywordsNodesList.keySet().iterator();
//        while (it.hasNext()) {
//            key = (String) it.next();
//            value = (String) KeywordsNodesList.get(key);
//            System.out.println(key + ":{" + value + "}");
//        }

    }

    //查找根节点的关键字
    public void searchKeywordRoot(ArrayList<String> rqk,ArrayList<String> ppk){

        rqk.removeAll(ppk); //求差集

    }

    //查找子节点的关键字
    public int searchKeywordNode(ArrayList<String> rqk,ArrayList<String> snk,int path){

        int n = 0;

        ArrayList<String> temp = new ArrayList<>(rqk); //复制当前请求关键字列表

        temp.retainAll(snk); //求交集

        n = path * temp.size(); //求松散度加分

        rqk.removeAll(temp); //求差集

        return n;

    }


    //获得符合条件的语义位置以及松散度
    public SubTreeDto getSemanticPlace1(String p) throws IOException {


        //初始化松散度
        int LTp = 1;

        //存放子树的节点
        ArrayList<String> subTree = new ArrayList<>();

        //拷贝请求关键字列表
        ArrayList<String> rqk = new ArrayList(requestKeyList);

        boolean flag = false; //判断是否找到所有关键字

        //获得根节点
        PointP pp = resourceService.getPointPByName(p);

        //获得根节点的uri
        String ppUri = pp.getUri();

        //获得根节点的ID
        String pID = p.substring(1);

        //如果根节点在包含请求关键字的节点集合中
        if(KeywordsNodesList.containsKey(pID)) {

            //获取根节点关键字列表
            String ppKeywords = KeywordsNodesList.get(pID);

            String[] strArray = ppKeywords.split(",");
            ArrayList<String> ppk =new ArrayList<String>(Arrays.asList(strArray));
            //查询根节点的关键字
            searchKeywordRoot(rqk, ppk);

        }

        //获得其所有子结点
        List<SubNodeDto> ppSubtree = resourceService.getSubtree(ppUri);

        //广度优先遍历每一个子结点
        for (SubNodeDto st : ppSubtree) {

            //如果找到所有关键字
            if(rqk.size() == 0){
                flag = true;
                break;
            }

            String vID =  Long.toString(st.getSubNodeID());
            String vName = "v"+ st.getSubNodeID();

//            String root = System.getProperty("user.dir");
//            root= root.substring(0,root.lastIndexOf("\\"));
//            String fileName = "p"+st.getSubNodeID()+".txt";
//            String filePath = root + File.separator + "document" +File.separator  + fileName;
//
//            File file = new File(filePath);
//
//            if (file.exists()) {
//                id = "p"+ st.getSubNodeID();
//            }
//            else{
//                id = "v"+ st.getSubNodeID();
//            }

            //将当前节点加入子树
            subTree.add(vName);

            //如果当前节点在包含请求关键字的节点集合中
            if(KeywordsNodesList.containsKey(vID)) {

                //获取子节点关键字列表
                String snKeywords = KeywordsNodesList.get(vID);
                String[] strArray = snKeywords.split(",");
                ArrayList<String> snk =new ArrayList<String>(Arrays.asList(strArray));

                //获取子节点的深度
                int path = st.getPath();

                LTp = LTp + searchKeywordNode(rqk,snk,path);


            }
            else{ continue; }

        }

        if(!flag){ //没有找到所有关键字

            LTp = (int) Double.POSITIVE_INFINITY; //正无穷
            subTree.clear(); //清空

        }

        SubTreeDto sbt = new SubTreeDto(LTp,subTree);


        return sbt;

    }



    public SubTreeDto getSemanticPlace2(String p , double LwTp) throws IOException {

        //初始化松散度
        int LTp = 1;

        //初始化松散度动态边界
        int LbTp = 0;

        //存放子树的节点
        ArrayList<String> subTree = new ArrayList<>();

        //拷贝请求关键字列表
        ArrayList<String> rqk = new ArrayList(requestKeyList);

        //判断是否找到所有关键字
        boolean flag = false;

        //判断是否超过动态边界
        boolean flag2 = false;

        //获得根节点
        PointP pp = resourceService.getPointPByName(p);

        //获得根节点的uri
        String ppUri = pp.getUri();

        //获得根节点的ID
        String pID = p.substring(1);


        //如果根节点在包含请求关键字的节点集合中
        if(KeywordsNodesList.containsKey(pID)) {

            //获取根节点关键字列表
            String ppKeywords = KeywordsNodesList.get(pID);
            String[] strArray = ppKeywords.split(",");
            ArrayList<String> ppk =new ArrayList<String>(Arrays.asList(strArray));
            //查询根节点的关键字
            searchKeywordRoot(rqk, ppk);

        }

        //获得其所有子结点
        List<SubNodeDto> ppSubtree = resourceService.getSubtree(ppUri);

        //广度优先遍历每一个子结点
        for (SubNodeDto st : ppSubtree) {

            //如果找到所有关键字
            if(rqk.size() == 0){
                flag = true;
                break;
            }


            String vID =  Long.toString(st.getSubNodeID());

            String vName = "v"+ st.getSubNodeID();

            subTree.add(vName);

            //计算动态边界
            int keysNum = rqk.size(); //剩余关键字数目
            int tempDis = st.getPath(); //当前节点到根节点的距离
            LbTp = LTp + keysNum * tempDis;

            //如果动态边界超过松散度阈值，退出循环
            if(LbTp >= (int)LwTp){
                flag2 = true;
                break;
            }

            //如果当前节点在包含请求关键字的节点集合中
            if(KeywordsNodesList.containsKey(vID)) {

                //获取子节点关键字列表
                String snKeywords = KeywordsNodesList.get(vID);
                String[] strArray = snKeywords.split(",");
                ArrayList<String> snk =new ArrayList<String>(Arrays.asList(strArray));
                //获取子节点的深度
                int path = st.getPath();

                LTp = LTp + searchKeywordNode(rqk,snk,path);

            }
            else{ continue; }

        }

        if(!flag){ //没有找到所有关键字

            LTp = (int) Double.POSITIVE_INFINITY; //正无穷
            subTree.clear(); //清空

        }

        if(!flag2){ //没有找到所有关键字

            LTp = -1; //正无穷
            subTree.clear(); //清空

        }


        //返回结果
        SubTreeDto sbt = new SubTreeDto(LTp,subTree);

        return sbt;

    }

    //获取自底向上遍历r树节点的顺序
    public void getSpatialIndex(Node<String, Point> rootNode,Stack<Node<String, Point>> nodesStack) {

        Queue<Node<String, Point>> temp = new LinkedList<>();
        Node<String, Point> a;
        temp.offer(rootNode);
        nodesStack.push(rootNode);

        while(!temp.isEmpty()){

            a = temp.poll();

            //如果a是叶子节点
            if(a instanceof Leaf){
                break;
            }
            //如果a不是叶子节点
            else{
                //遍历a子节点，加入队列
                List<Node<String,Point>> ln = ((NonLeaf<String,Point>)a).children();
                for(Node<String,Point> l:ln){
                    temp.offer(l);
                    nodesStack.push(l);
                }
            }
        }


    }

    //计算基于alph边界的排序函数值
    public double getfBalph(int index, double distance, int alph) throws IOException {

        //基于alph边界的松散度
        double LTn = 1;

        //获取当前节点的邻域半径
        Map<String,Integer> WNn = alphaRadiusWordN.readWordNeighborhoodsN("n" + index);

        //拷贝请求关键字列表
        ArrayList<String> rqk = new ArrayList(requestKeyList);

        //遍历请求关键字列表
        for(String key : rqk){

            if(WNn.get(key)==null){
                LTn = LTn + alph + 1;
            }
            else{
                LTn = LTn + WNn.get(key);
            }

        }

        double fBalph = LTn * distance;

        return fBalph;
    }


    //sp算法,输出队列hk
    public TestResultDto SpQuery(RTree<String, Point> rTree, int alph) throws Exception {

        TestResultDto testResultDto = new TestResultDto(); //存放测试结果

        long runtime; ////运行时间
        long runtime_sem = 0; //构建语义位置时间
        long runtime_other; //其他时间
        int result_num; //实际返回结果数
        String result_root=""; //返回结果根节点
        double sem_score_ave = 0; //平均松散度/相似度
        double dis_score_ave = 0; //平均空间距离
        int dis_index_num = 0; //访问空间索引节点数
        int sem_num = 0; //构建语义位置数

        Queue<TQSPDto> Hk = new PriorityQueue<>(); //初始化队列Hk

        double theta = Double.POSITIVE_INFINITY; //队列Hk中第k个TQSP的分数,初始为正无穷

        double LwTp = 0; //松散度阈值

        //获取开始时间
        long runtime_startTime = System.currentTimeMillis();

        //查询位置
        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);

        //获得空间索引根节点
        Node<String, Point> rootNode = rTree.root().get();

        //用于获得空间索引各个节点及其子节点的编号
        Map<Integer,List<Integer>> nodesMap = alphaRadiusWordN.readRtreeIndex();
        Stack<Node<String, Point>> nodesStack = new Stack<>();
        getSpatialIndex(rootNode,nodesStack);


        //按排序函数边界升序存储空间点
        Queue<fBalphNodeDto> nodesQueue = new PriorityQueue<>();
        fBalphNodeDto fBalphNodeDto;

        //当前空间索引节点
        Node<String, Point> tempNode = nodesStack.get(0);

        //计算与查询位置的距离
        double distance = tempNode.geometry().distance(point);

        //计算基于alph边界的非叶节点的排序函数值
        double fBalph = getfBalph(0,distance,alph);

        fBalphNodeDto = new fBalphNodeDto(tempNode,fBalph);

        //将根节点及其fBalph加入队列
        nodesQueue.add(fBalphNodeDto);


//        //遍历空间索引
        while(!nodesQueue.isEmpty()){

            fBalphNodeDto = nodesQueue.poll();
            tempNode = fBalphNodeDto.getNode();
            int index = nodesStack.indexOf(tempNode);

            if(fBalphNodeDto.getFBalph()>=theta){
                break;
            }

            if(sem_num>=999){
                break;
            }

            //如果是叶子节点
            if(tempNode instanceof Leaf){

                //获取其子节点
                List<Entry<String, Point>> ll = ((Leaf<String, Point>) tempNode).entries();

                //遍历其子节点
                for(Entry<String, Point> l:ll){

                    dis_index_num++;

                    //获得当前地理位置节点的名字
                    String s = l.value();

                    //对当前节点进行可达性检测
                    boolean check = reachabilityCheck(s);

                    //如果不可达，跳过该节点，遍历下一个节点
                    if (!check) {
                        continue;
                    }

                    //计算松散度阈值
                    LwTp = theta / l.geometry().distance(point);

                    long runtime_sem_startTime = System.currentTimeMillis();

                    SubTreeDto sbd;

                    //获取当前地理位置节点的子树
                    if(Hk.size()<k){
                        sbd = getSemanticPlace1(s);
                    }else{
                        sbd = getSemanticPlace2(s, LwTp);
                    }

                    long runtime_sem_endTime = System.currentTimeMillis();

                    sem_num++;
                    runtime_sem = runtime_sem + runtime_sem_endTime - runtime_sem_startTime;


                    //如果剪枝
                    if (sbd.getLTp() == -1) {
                        sem_num--;
                        continue;
                    }

                    //如果子树不是符合条件的语义位置，跳过该循环
                    if (sbd.getLTp() == (int) Double.POSITIVE_INFINITY) {
                        continue;
                    }

                    //计算当前地理位置节点及子树的分数
                    double score = getScore(sbd.getLTp(), l.geometry().distance(point));

                    //如果得分小于当前队列中最大得分，加入队列
                    if (score < theta || Hk.size()<k) {

                        TQSPDto t = new TQSPDto();
                        t.setScore(score);
                        t.setRoot(s);
                        t.setSubTree(sbd.getSubTree());
                        t.setLongitude(l.geometry().x());
                        t.setLatitude(l.geometry().y());
                        t.setSem_score(sbd.getLTp());
                        t.setDis_score(l.geometry().distance(point));
                        Hk.add(t);


                        //只保留前k个结果
                        if (Hk.size() > k) {
                            Hk.poll();
                        }

                        //更新当前队列中的最大得分
                        theta = Hk.peek().getScore();
                    }

                    if(sem_num>=999){
                        break;
                    }

                }

            }
            //如果是非叶节点
            else{
                //获取子节点序号
                List<Integer> childrenIndex = nodesMap.get(index);
                //遍历子节点
                for(int i:childrenIndex){
                    //获得当前子节点
                    Node<String, Point> child = nodesStack.get(i);

                    //计算与查询位置的距离
                    distance = child.geometry().distance(point);

                    //计算基于alph边界的非叶节点的排序函数值
                    fBalph = getfBalph(i,distance,alph);

                    if(fBalph < theta){

                        fBalphNodeDto = new fBalphNodeDto(child,fBalph);

                        //将根节点及其fBalph加入队列
                        nodesQueue.add(fBalphNodeDto);

                    }

                }

            }

        }



        long runtime_endTime = System.currentTimeMillis();    //获取结束时间

        result_num = Hk.size();

        if(result_num==0){
            sem_score_ave = -1;
            dis_score_ave = -1;
            result_root = "null";
        }
        else{

            Queue<TQSPDto> Hkk = new PriorityQueue<>();
            for (TQSPDto tqs : Hk){
                Hkk.add(tqs);
            }


            while (!Hkk.isEmpty()) {

                TQSPDto t = Hkk.poll();
                sem_score_ave = sem_score_ave + t.getSem_score();
                dis_score_ave = dis_score_ave + t.getDis_score();
                if(result_root.equals("")){
                    result_root = t.root;
                }else{
                    result_root = t.root + "," + result_root;
                }

            }

            sem_score_ave = sem_score_ave/result_num;
            dis_score_ave = dis_score_ave/result_num;

        }

        System.out.println(result_root);

        runtime = runtime_endTime-runtime_startTime;
        runtime_other = runtime - runtime_sem;

        testResultDto.setRuntime(runtime);
        testResultDto.setRuntime_sem(runtime_sem);
        testResultDto.setRuntime_other(runtime_other);
        testResultDto.setResult_num(result_num);
        testResultDto.setResult_root(result_root);
        testResultDto.setLtp_score_ave(sem_score_ave);
        testResultDto.setSem_score_ave(-1);
        testResultDto.setF_score_ave(-1);
        testResultDto.setDis_score_ave(dis_score_ave);
        testResultDto.setDis_index_num(dis_index_num);
        testResultDto.setSem_num(sem_num);
        testResultDto.setHk(Hk);

        System.out.println(runtime + "    "+runtime_sem+"    "+runtime_other);

        return testResultDto;

    }



    //spp算法,输出队列hk
    public TestResultDto SppQuery(RTree<String, Point> rTree) throws Exception {

        TestResultDto testResultDto = new TestResultDto(); //存放测试结果

        long runtime; ////运行时间
        long runtime_sem = 0; //构建语义位置时间
        long runtime_other; //其他时间
        int result_num; //实际返回结果数
        String result_root=""; //返回结果根节点
        double sem_score_ave = 0; //平均松散度/相似度
        double dis_score_ave = 0; //平均空间距离
        int dis_index_num = 0; //访问空间索引节点数
        int sem_num = 0; //构建语义位置数

        Queue<TQSPDto> Hk = new PriorityQueue<>(); //初始化队列Hk

        double theta = Double.POSITIVE_INFINITY; //队列Hk中第k个TQSP的分数,初始为正无穷

        double LwTp = 0; //松散度阈值


        long runtime_startTime = System.currentTimeMillis();


        //查询位置
        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);


        //根据查询地点，获得附近地理位置节点，按距离升序排序
        List<Entry<String, Point>> plist = spatialindex.getPointPByDis(rTree,point,maxdistance,maxcount);


        //按距离升序遍历地理位置节点
        for(Entry<String, Point> it : plist) {

            dis_index_num++;

            //如果当前地理位置节点的距离值大于等于theta值，结束遍历
            if (it.geometry().distance(point) >= theta && Hk.size()==k) {
                break;
            }

            //获得当前地理位置节点的名字
            String s = it.value();

            //对当前节点进行可达性检测
            boolean check = reachabilityCheck(s);

            //如果不可达，跳过该节点，遍历下一个节点
            if (!check) {
                continue;
            }

            //计算松散度阈值
            LwTp = theta / it.geometry().distance(point);

            SubTreeDto sbd;
            long runtime_sem_startTime = System.currentTimeMillis();

            //获取当前地理位置节点的子树
            if(Hk.size()<k){
                sbd = getSemanticPlace1(s);
            }else{
                sbd = getSemanticPlace2(s, LwTp);
            }

            long runtime_sem_endTime = System.currentTimeMillis();

            sem_num++;
            runtime_sem = runtime_sem + runtime_sem_endTime - runtime_sem_startTime;


            //如果剪枝
            if (sbd.getLTp() == -1) {
                sem_num--;
                continue;
            }

            //如果子树不是符合条件的语义位置，跳过该循环
            if (sbd.getLTp() == (int) Double.POSITIVE_INFINITY) {
                continue;
            }

            //计算当前地理位置节点及子树的分数
            double score = getScore(sbd.getLTp(), it.geometry().distance(point));

            //如果得分小于当前队列中最大得分，加入队列
            if (score < theta || Hk.size() < k) {

                TQSPDto t = new TQSPDto();
                t.setScore(score);
                t.setRoot(s);
                t.setSubTree(sbd.getSubTree());
                t.setLongitude(it.geometry().x());
                t.setLatitude(it.geometry().y());
                t.setSem_score(sbd.getLTp());
                t.setDis_score(it.geometry().distance(point));
                Hk.add(t);

                //只保留前k个结果
                if (Hk.size() > k) {
                    Hk.poll();
                }

                //更新当前队列中的最大得分
                theta = Hk.peek().getScore();

            }
        }

        long runtime_endTime = System.currentTimeMillis();

        result_num = Hk.size();

        if(result_num==0){
            sem_score_ave = -1;
            dis_score_ave = -1;
            result_root = "null";
        }
        else{

            Queue<TQSPDto> Hkk = new PriorityQueue<>();
            for (TQSPDto tqs : Hk){
                Hkk.add(tqs);
            }


            while (!Hkk.isEmpty()) {

                TQSPDto t = Hkk.poll();
                sem_score_ave = sem_score_ave + t.getSem_score();
                dis_score_ave = dis_score_ave + t.getDis_score();
                if(result_root.equals("")){
                    result_root = t.root;
                }else{
                    result_root = t.root + "," + result_root;
                }

            }

            sem_score_ave = sem_score_ave/result_num;
            dis_score_ave = dis_score_ave/result_num;

        }

        System.out.println(result_root);

        runtime = runtime_endTime-runtime_startTime;
        runtime_other = runtime - runtime_sem;

        testResultDto.setRuntime(runtime);
        testResultDto.setRuntime_sem(runtime_sem);
        testResultDto.setRuntime_other(runtime_other);
        testResultDto.setResult_num(result_num);
        testResultDto.setResult_root(result_root);
        testResultDto.setLtp_score_ave(sem_score_ave);
        testResultDto.setSem_score_ave(-1);
        testResultDto.setF_score_ave(-1);
        testResultDto.setDis_score_ave(dis_score_ave);
        testResultDto.setDis_index_num(dis_index_num);
        testResultDto.setSem_num(sem_num);
        testResultDto.setHk(Hk);

        System.out.println(runtime + "    "+runtime_sem+"    "+runtime_other);

        return testResultDto;

    }

    public boolean reachabilityCheck(String s) throws IOException {

//        String root = System.getProperty("user.dir");
//        root= root.substring(0,root.lastIndexOf("\\"));
//        String fileName = s + ".txt";
//        String filePath = root + File.separator + "document" +File.separator  + fileName;
//
//        boolean check = true; //可达为true，否则为false
//
//        ArrayList<String> rqk = new ArrayList(requestKeyList); //拷贝请求关键字列表
//
//        for(String str:rqk){
//            BufferedReader br = new BufferedReader(new FileReader(filePath));
//            String ppKeys = br.readLine();
//            br.close();
//            if(!ppKeys.contains(str)){
//                check = false;
//                break;
//            }
//        }

        return true;
    }


    //bsp算法,输出队列hk
    public TestResultDto BspQuery(RTree<String, Point> rTree) throws Exception {

        TestResultDto testResultDto = new TestResultDto(); //存放测试结果

        long runtime; ////运行时间
        long runtime_sem = 0; //构建语义位置时间
        long runtime_other; //其他时间
        int result_num; //实际返回结果数
        String result_root=""; //返回结果根节点
        double sem_score_ave = 0; //平均松散度/相似度
        double dis_score_ave = 0; //平均空间距离
        int dis_index_num = 0; //访问空间索引节点数
        int sem_num = 0; //构建语义位置数

        Queue<TQSPDto> Hk = new PriorityQueue<>(); //初始化队列Hk

        double theta = Double.POSITIVE_INFINITY; //队列Hk中第k个TQSP的分数,初始为正无穷

        //根据查询关键字，初始化关键字文档
//        getKeywordsNodes();

        long runtime_startTime = System.currentTimeMillis();

        //查询位置
        Point point = Geometries.pointGeographic(requestLongitude,requestLatitude);


        //根据查询地点，获得附近地理位置节点，按距离升序排序
        List<Entry<String, Point>> plist = spatialindex.getPointPByDis(rTree,point,maxdistance,maxcount);


        //按距离升序遍历地理位置节点
        for(Entry<String, Point> it : plist){


            dis_index_num++;

            //如果当前地理位置节点的距离值大于等于theta值，结束遍历
            if(it.geometry().distance(point)>=theta && Hk.size()==k){
                break;
            }

            //获取当前地理位置节点的名字
            String s = it.value();

            long runtime_sem_startTime = System.currentTimeMillis();

            //获取当前地理位置节点的子树
            SubTreeDto sbd = getSemanticPlace1(s);

            long runtime_sem_endTime = System.currentTimeMillis();

            sem_num++;
            runtime_sem = runtime_sem + runtime_sem_endTime - runtime_sem_startTime;

            //如果子树不是符合条件的语义位置，跳过该循环
            if(sbd.getLTp() == (int)Double.POSITIVE_INFINITY){
                continue;
            }

            //计算当前地理位置节点及子树的分数
            double score = getScore(sbd.getLTp(),it.geometry().distance(point));

            //如果得分小于当前队列中最大得分，加入队列
            if(score < theta || Hk.size()<k){

                TQSPDto t = new TQSPDto();
                t.setScore(score);
                t.setRoot(s);
                t.setSubTree(sbd.getSubTree());
                t.setLongitude(it.geometry().x());
                t.setLatitude(it.geometry().y());
                t.setSem_score(sbd.getLTp());
                t.setDis_score(it.geometry().distance(point));
                Hk.add(t);

                //只保留前k个结果
                if(Hk.size() > k){
                    Hk.poll();
                }

                //更新当前队列中的最大得分
                theta = Hk.peek().getScore();

            }

        }


        long runtime_endTime = System.currentTimeMillis();

        result_num = Hk.size();

        if(result_num==0){
            sem_score_ave = -1;
            dis_score_ave = -1;
            result_root = "null";
        }
        else{
            Queue<TQSPDto> Hkk = new PriorityQueue<>();
            for (TQSPDto tqs : Hk){
                Hkk.add(tqs);
            }

            while (!Hkk.isEmpty()) {

                TQSPDto t = Hkk.poll();
                sem_score_ave = sem_score_ave + t.getSem_score();
                dis_score_ave = dis_score_ave + t.getDis_score();
                if(result_root.equals("")){
                    result_root = t.root;
                }else{
                    result_root = t.root + "," + result_root;
                }

            }

            sem_score_ave = sem_score_ave/result_num;
            dis_score_ave = dis_score_ave/result_num;
        }



        System.out.println(result_root);

        runtime = runtime_endTime-runtime_startTime;
        runtime_other = runtime - runtime_sem;

        testResultDto.setRuntime(runtime);
        testResultDto.setRuntime_sem(runtime_sem);
        testResultDto.setRuntime_other(runtime_other);
        testResultDto.setResult_num(result_num);
        testResultDto.setResult_root(result_root);
        testResultDto.setLtp_score_ave(sem_score_ave);
        testResultDto.setSem_score_ave(-1);
        testResultDto.setF_score_ave(-1);
        testResultDto.setDis_score_ave(dis_score_ave);
        testResultDto.setDis_index_num(dis_index_num);
        testResultDto.setSem_num(sem_num);
        testResultDto.setHk(Hk);

        System.out.println(runtime + "    "+runtime_sem+"    "+runtime_other);

        return testResultDto;
    }


    //获得得分f
    public double getScore(int LTp, double distance){

        return(LTp*distance);

    }


    //测试
    public void testData() throws Exception {

//        requestKeyString = "psychological,hostility,yemen";
        requestKeyString = "syria,harim";
        String[] strArray = requestKeyString.split(",");
        requestLongitude = 39;
        requestLatitude = 38;
        requestKeyList =new ArrayList<String>(Arrays.asList(strArray)) ;
        k = 3;

        System.out.println("*****************************************************************************");
        System.out.println("查询关键字：" + requestKeyString);
        System.out.println("查询地点：" + requestLongitude + ", " +requestLatitude);
        System.out.println("返回结果数：" + k);
        System.out.println("*****************************************************************************");
    }




}
