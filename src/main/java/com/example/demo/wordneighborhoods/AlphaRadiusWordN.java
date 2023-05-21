package com.example.demo.wordneighborhoods;

import com.example.demo.dto.SubNodeDto;
import com.example.demo.entity.PointP;
import com.example.demo.repository.PointPRepository;
import com.example.demo.service.ResourceService;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class AlphaRadiusWordN {

    private static ResourceService resourceService;
    @Autowired
    public void setResourceService(ResourceService resourceService){
        AlphaRadiusWordN.resourceService = resourceService;
    }

    private PointPRepository pointPRepository;

    @Autowired
    public void setResourceService(PointPRepository pointPRepository) {
        this.pointPRepository = pointPRepository;
    }


    //构造半径字邻域
    public void initNeighborhoodsP(int alpha) throws Exception {

        //遍历每一个p类型节点，构造位置节点的半径字邻域

        int pageSize = 1000; //每一页大小
        int pageIndex = 0; //当前页号
        int pageCount = resourceService.getNumP(); //p总记录数


        String root = System.getProperty("user.dir");
        String fileName = "WordNeighborhoodsP.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordneighborhoods" +File.separator  + fileName;

        clearTXT(filePath); //清空半径字邻域

        while (pageIndex < pageCount) {

            long startTime = System.currentTimeMillis();    //获取开始时间

            //分页获取pagesize个节点
            List<PointP> pplist = resourceService.getPointP(pageIndex, pageSize);

            //遍历每一个节点,遍历其子结点
            for (PointP pp : pplist) {

                //存储该点的半径字邻域
                Map<String,Integer> WNp = new HashMap<String,Integer>();

                //获得根节点的uri
                String ppUri = pp.getUri();

                //获得根节点的id
                long ppId = pp.getId();

                root = System.getProperty("user.dir");
                root= root.substring(0,root.lastIndexOf("\\"));
                fileName = "p" + ppId + ".txt";
                filePath = root + File.separator + "document" +File.separator  + fileName;

                BufferedReader br = new BufferedReader(new FileReader(filePath));

                //获得根节点的关键字列表
                String ppKeys = br.readLine();
                String temp = null;
                br.close();
                boolean flag1 = ppKeys.contains("}");
                boolean flag2 = ppKeys.contains("{");

                if(flag1){
                    ppKeys = ppKeys.substring(0, ppKeys.indexOf("}"));
                }

                if(flag2){
                    temp = ppKeys.substring(0, ppKeys.indexOf("{"));
                    ppKeys = ppKeys.substring(temp.length()+1, ppKeys.length());
                }

                if(!ppKeys.equals("")){

                    String[] strArray = ppKeys.split(",");

                    //遍历根节点的关键字列表
                    for(String s:strArray){

                        if(s.equals("") || s == null){
                            continue;
                        }

                        //如果该关键字不在WNp中，则将其加入
                        if(!WNp.containsKey(s)){
                            WNp.put(s,0);
                        }

                    }
                }


                //获得其所有子结点
                List<SubNodeDto> ppSubtree = resourceService.getSubtree(ppUri);

                //广度优先遍历每一个子结点
                for (SubNodeDto st : ppSubtree) {


                    fileName = "p"+st.getSubNodeID()+".txt";
                    filePath = root + File.separator + "documentp" +File.separator  + fileName;


                    File file = new File(filePath);

                    if (!file.exists()) {
                        fileName = "v"+st.getSubNodeID()+".txt";
                        filePath = root + File.separator + "document" +File.separator  + fileName;
                    }

                    br = new BufferedReader(new FileReader(filePath));


                    //获得子节点的关键字列表
                    String pvKeys = br.readLine();
                    br.close();

                    flag1 = pvKeys.contains("}");
                    flag2 = pvKeys.contains("{");

                    if(flag1){
                        pvKeys = pvKeys.substring(0,pvKeys.indexOf("}"));
                    }

                    if(flag2){
                        temp = pvKeys.substring(0, pvKeys.indexOf("{"));
                        pvKeys = pvKeys.substring(temp.length()+1, pvKeys.length());
                    }

                    if(!pvKeys.equals("")){

                        String[] strArray = pvKeys.split(",");

                        //遍历子节点的关键字列表
                        for(String s:strArray){

                            if(s.equals("") || s == null){
                                continue;
                            }

                            //如果该关键字不在WNp中,且该关键字到根节点的距离小于等于alpha，则将其加入
                            if(!WNp.containsKey(s) && st.getPath()<=alpha){

                                WNp.put(s,st.getPath());
                            }
                            //如果该关键字在WNp中,且该关键字到根节点的距离小于WNp中的值，修改WNp中的值
                            else if(WNp.containsKey(s) && st.getPath() < WNp.get(s)){

                                WNp.replace(s,st.getPath());
                            }

                        }

                    }


                }

                String WNpString = getMapToString(WNp);

                String name = "p" + ppId;

                writeWordNeighborhoodsP(name,WNpString);
                writeWordNeighborhoodsAllP(name,WNpString);


            }

            //当前页号改变
            pageIndex = pageIndex + pageSize;

            long endTime = System.currentTimeMillis();    //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
            System.out.println(pageIndex);

        }

    }

    //初始化空间索引的半径字邻域
    public void initRoot(RTree<String, Point> rTree) throws IOException {

        //获得根节点
        Node<String, Point> rootNode = rTree.root().get();

        //存储自底向上遍历r树节点的顺序
        Stack<Node<String, Point>> nodeStack = new Stack<>();
        Stack<Node<String, Point>> nodeStackSub = new Stack<>();

        //获取自底向上遍历r树节点的顺序
        getRtreeIndex(rootNode,nodeStack,nodeStackSub);

        //存储该点的半径字邻域
        Map<String,Integer> WNnf = new HashMap<String,Integer>();

        for(int i=1 ; i<=2 ; i++){
            Node<String, Point> nodetemp = nodeStack.get(i);
            Map<String,Integer> WNn = readWordNeighborhoodsN("n" + i);
            for(String key : WNn.keySet()){

                if(key=="" || key==" " || key==null){
                    continue;
                }

                //如果该关键字不在WNnf中，则将其加入
                if(!WNnf.containsKey(key)){

                    WNnf.put(key,WNn.get(key));

                }
                //如果该关键字在WNnf中,且WNn中的值小于WNnf中的值，修改WNnf中的值
                else if(WNnf.containsKey(key) && WNn.get(key) < WNnf.get(key)){
                    WNnf.replace(key,WNn.get(key));
                }

            }

        }

        //写入文档
        String name = "n0";
        writeWordNeighborhoodsN(name,WNnf);
    }


    //初始化空间索引的半径字邻域
    public void initNeighborhoodsN(RTree<String, Point> rTree) throws IOException {

//        String root = System.getProperty("user.dir");
//        String fileName = "RtreeIndex.txt";
//        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordneighborhoods" +File.separator  + fileName;
//
//        clearTXT(filePath); //清空r树节点及其对应序号的文档
//
//        root = System.getProperty("user.dir");
//        fileName = "WordNeighborhoodsN.txt";
//        filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordneighborhoods" +File.separator  + fileName;
//
//        clearTXT(filePath); //清空半径字邻域n


        //获得根节点
        Node<String, Point> rootNode = rTree.root().get();

        //存储自底向上遍历r树节点的顺序
        Stack<Node<String, Point>> nodeStack = new Stack<>();
        Stack<Node<String, Point>> nodeStackSub = new Stack<>();

        //获取自底向上遍历r树节点的顺序
        getRtreeIndex(rootNode,nodeStack,nodeStackSub);


        while(!nodeStack.empty()){

            Node<String, Point> node = nodeStack.peek();
            int index = nodeStack.indexOf(node);
            nodeStack.pop();


            if(index>162360){
                continue;
            }

            //如果是叶子节点
            if(node instanceof Leaf){

                //获取该节点包含的地理位置点
                List<Entry<String, Point>> ll = ((Leaf<String, Point>) node).entries();

                //存储该点的半径字邻域
                Map<String,Integer> WNn = new HashMap<String,Integer>();

                for(Entry<String, Point> l:ll){

                    //读取地理位置点的半径字邻域
                    String WNpString = readWordNeighborhoodsP(l.value());
                    WNpString = WNpString.substring(0,WNpString.indexOf("}"));
                    String temp = WNpString.substring(0, WNpString.indexOf("{"));
                    WNpString = WNpString.substring(temp.length()+1, WNpString.length());

                    Map<String,Integer> WNp = getStringToMap(WNpString);

                    for(String key : WNp.keySet()){

                        //如果该关键字不在WNn中，则将其加入
                        if(!WNn.containsKey(key)){

                            WNn.put(key,WNp.get(key));

                        }
                        //如果该关键字在WNn中,且WNp中的值小于WNn中的值，修改WNn中的值
                        else if(WNn.containsKey(key) && WNp.get(key) < WNn.get(key)){

                            WNn.replace(key,WNp.get(key));

                        }

                    }

                }

                //写入文档
//                String WNnString = getMapToString(WNn);
                String name = "n" + index;
//                writeWordNeighborhoodsN(name,WNnString);
                writeWordNeighborhoodsN(name,WNn);

            }
            //如果是非叶节点
            else{

                //获得其子节点
                int num = ((NonLeaf<String,Point>)node).children().size();
//                List<Node<String,Point>> ln = ((NonLeaf<String,Point>)node).children();

                //存储该点的半径字邻域
                Map<String,Integer> WNnf = new HashMap<String,Integer>();

                for(int i=0 ; i<num ; i++){
//                for(Node<String,Point> l:ln){

                    //读取子节点的半径字邻域
//                    String WNnString = readWordNeighborhoodsN("n" + nodeStackStr.indexOf(l.geometry().toString()));
//                    WNnString = WNnString.substring(0,WNnString.indexOf("}"));
//                    String temp = WNnString.substring(0, WNnString.indexOf("{"));
//                    WNnString = WNnString.substring(temp.length()+1, WNnString.length());
//                    Map<String,Integer> WNn = getStringToMap(WNnString);

                    Node<String, Point> nodetemp = nodeStackSub.peek();
                    int indextemp = nodeStackSub.indexOf(nodetemp);
                    nodeStackSub.pop();

                    Map<String,Integer> WNn = readWordNeighborhoodsN("n" + indextemp);
                    System.out.println("n" + indextemp);

                    for(String key : WNn.keySet()){

                        if(key=="" || key==" " || key==null){
                            continue;
                        }

                        //如果该关键字不在WNnf中，则将其加入
                        if(!WNnf.containsKey(key)){

                            WNnf.put(key,WNn.get(key));

                        }
                        //如果该关键字在WNnf中,且WNn中的值小于WNnf中的值，修改WNnf中的值
                        else if(WNnf.containsKey(key) && WNn.get(key) < WNnf.get(key)){

                            WNnf.replace(key,WNn.get(key));

                        }

                    }


                }

                //写入文档
//                String WNnfString = getMapToString(WNnf);
                String name = "n" + index;

                writeWordNeighborhoodsN(name,WNnf);
//                writeWordNeighborhoodsN(name,WNnfString);
//                writeWordNeighborhoodsAllN(name,WNnfString);

            }

            System.out.println(index);

        }

        //将r树节点及其对应序号写入文档
//        String RtreeIndexStr = getMapToString2(RtreeIndex);

//        writeRtreeIndex(RtreeIndex);


//        //读取r树节点及其对应序号
//        String result = readRtreeIndex();
//        Map<String,Integer> RtreeIndexNew = getStringToMap2(result);
//
//        for (Integer value : RtreeIndexNew.values()) {
//            System.out.println("Value = " + value);
//        }

    }

//    将r树节点及其对应序号写入文档
    public void writeRtreeIndex(Node<String, Point> rootNode) throws IOException {

        Queue<Node<String, Point>> nodeQueue = new LinkedList<>();
        Stack<Node<String, Point>> nodeStack = new Stack<>();
        Map<Integer,List<Integer>> RtreeIndex = new HashMap<>();
        nodeQueue.offer(rootNode);
        nodeStack.push(rootNode);

        while(!nodeQueue.isEmpty()){

            Node<String, Point> temp = nodeQueue.poll();
            int index = nodeStack.indexOf(temp);

            //如果a是叶子节点
            if(temp instanceof Leaf){
                break;
            }
            //如果a不是叶子节点
            else{

                List<Node<String, Point>> ln =((NonLeaf<String,Point>)temp).children();
                List<Integer> childrenIndex = new ArrayList<>();
                //遍历a子节点
                for(Node<String, Point> l : ln){
                    nodeQueue.offer(l);
                    nodeStack.push(l);
                    childrenIndex.add(nodeStack.indexOf(l));
                }

              RtreeIndex.put(index,childrenIndex);

            }

        }

        String root = System.getProperty("user.dir");
        String fileName = "RtreeIndex.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordNeighborhoods" +File.separator  + fileName;

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath,true));

        for(Map.Entry<Integer,List<Integer>> entry:RtreeIndex.entrySet()){

            String temp;

            if(entry.getValue()==null){
                temp = entry.getKey().toString();
            }
            else{
                temp = entry.getKey().toString() + "/" + entry.getValue().toString();
            }

            bw.write(temp); // 数据写入缓冲区，一般在内存中
            bw.newLine(); // 写入换行符
            bw.flush(); // 清空缓冲区，刷写数据到外部文件中
        }


    }

//    读取r树节点及其对应序号
    public Map<Integer,List<Integer>> readRtreeIndex() throws IOException {

        String root = System.getProperty("user.dir");
        String fileName = "RtreeIndex.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordNeighborhoods" +File.separator  + fileName;

        BufferedReader br = new BufferedReader( new FileReader(filePath));

        Map<Integer,List<Integer>> RtreeIndex = new HashMap<>();
        String temp;

        while ((temp = br.readLine())!=null){

            //根据":"截取字符串数组
            String[] str = temp.split("/");

            //str[0]为KEY,str[1]为值
            int key = Integer.parseInt(str[0].trim());

            if (str.length == 2){

                String valueStr =  str[1];
                valueStr = valueStr.substring(0,valueStr.indexOf("]"));
                String tempStr = valueStr.substring(0, valueStr.indexOf("["));
                valueStr = valueStr.substring(tempStr.length()+1);
                String[] valueArray = valueStr.split(",");
                List<Integer> value = new ArrayList<>();
                for(String i:valueArray){
                    value.add(Integer.parseInt(i.trim()));
                }
                RtreeIndex.put(key,value);

            }else{
                RtreeIndex.put(key,null);
            }

        }

        br.close();

        return RtreeIndex;

    }


    //获取自底向上遍历r树节点的顺序
    public void getRtreeIndex(Node<String, Point> root,Stack<Node<String, Point>> nodeStack,Stack<Node<String, Point>> nodeStackSub) throws IOException {

        Queue<Node<String, Point>> temp = new LinkedList<>();
        Node<String, Point> a;
        temp.offer(root);
        nodeStack.push(root);
        nodeStackSub.push(root);


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
                    nodeStack.push(l);
                    nodeStackSub.push(l);
                }
            }
        }

    }


    public void writeWordNeighborhoodsN(String name, Map<String,Integer> WNn) throws IOException {

        String root = System.getProperty("user.dir");
        root= root.substring(0,root.lastIndexOf("\\"));
        String fileName = name + ".txt";

        String filePath = root + File.separator + "wordNeighborhoodsN" +File.separator  + fileName;

        clearTXT(filePath); //清空文档

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));

        for(Map.Entry<String,Integer> entry:WNn.entrySet()){

            String temp;

            if(entry.getValue()==null){
                temp = entry.getKey().trim();
            }
            else{
                temp = entry.getKey().trim() + ":" + entry.getValue();
            }

            if(temp!="" || temp!=" " || temp!=null){
                bw.write(temp); // 数据写入缓冲区，一般在内存中
                bw.newLine(); // 写入换行符
                bw.flush(); // 清空缓冲区，刷写数据到外部文件中
            }

        }
        bw.close();

    }

    public Map<String,Integer> readWordNeighborhoodsN(String name) throws IOException {

        String root = System.getProperty("user.dir");
        root= root.substring(0,root.lastIndexOf("\\"));
        String fileName = name + ".txt";

        String filePath = root + File.separator + "wordNeighborhoodsN" +File.separator  + fileName;

        BufferedReader br = new BufferedReader( new FileReader(filePath));

        Map<String,Integer> WNn = new HashMap<String,Integer>();
        String temp;

        while ((temp = br.readLine())!=null){

            //根据":"截取字符串数组
            String[] str = temp.split(":");

            //str[0]为KEY,str[1]为值
            String key = str[0].trim();

            if (str.length == 2){
                int value =  Integer.parseInt(str[1]);
                WNn.put(key,value);
            }else{
                WNn.put(key,null);
            }

        }

        br.close();

        return WNn;

    }

    public void writeWordNeighborhoodsP(String name, String WNpString) throws IOException {

        String root = System.getProperty("user.dir");
        root= root.substring(0,root.lastIndexOf("\\"));
        String fileName = name + ".txt";

        String filePath = root + File.separator + "wordNeighborhoodsP" +File.separator  + fileName;

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
        bw.write(name +":{"+WNpString+"}");
        bw.close();


    }

    public String readWordNeighborhoodsP(String name) throws IOException {

        String root = System.getProperty("user.dir");
        root= root.substring(0,root.lastIndexOf("\\"));
        String fileName = name + ".txt";

        String filePath = root + File.separator + "wordNeighborhoodsP" +File.separator  + fileName;

        BufferedReader br = new BufferedReader( new FileReader(filePath));
        String WNpString = br.readLine();
        br.close();

        return WNpString;
    }


    public void writeWordNeighborhoodsAllP(String name, String WNpString) throws IOException {

        String root = System.getProperty("user.dir");
        String fileName = "WordNeighborhoodsP.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordneighborhoods" +File.separator  + fileName;


        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath,true));
        bw.write(name +":{"+WNpString+"}");
        bw.newLine();
        bw.close();

    }

    public void writeWordNeighborhoodsAllN(String name, String WNpString) throws IOException {

        String root = System.getProperty("user.dir");
        String fileName = "WordNeighborhoodsN.txt";
        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordneighborhoods" +File.separator  + fileName;


        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath,true));
        bw.write(name +":{"+WNpString+"}");
        bw.newLine();
        bw.close();

    }

    public void clearTXT(String filePath) throws IOException {

        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
        bw.write("");
        bw.close();

    }


    public String getMapToString(Map<String,Integer> map){
        Set<String> keySet = map.keySet();
        //将set集合转换为数组
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        //给数组排序(升序)
        Arrays.sort(keyArray);
        //因为String拼接效率很低，转用StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // 参数值为空，则不参与签名 这个方法trim()是去空格
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append(":").append(String.valueOf(map.get(keyArray[i])).trim());
            }
            if(i != keyArray.length-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public Map<String,Integer> getStringToMap(String str){

        //根据逗号截取字符串数组
        String[] str1 = str.split(",");
        //创建Map对象
        Map<String,Integer> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split(":");

            //str2[0]为KEY,str2[1]为值
            String key = str2[0].trim();

            if (str2.length == 2){
                int value =  Integer.parseInt(str2[1]);
                map.put(key,value);
            }else{
                map.put(key,null);
            }

        }
        return map;
    }

    public String getMapToString2(Map<String,Integer> map){
        Set<String> keySet = map.keySet();
        //将set集合转换为数组
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        //给数组排序(升序)
        Arrays.sort(keyArray);
        //因为String拼接效率很低，转用StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // 参数值为空，则不参与签名 这个方法trim()是去空格
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append(":").append(String.valueOf(map.get(keyArray[i])).trim());
            }
            if(i != keyArray.length-1){
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public Map<String,Integer> getStringToMap2(String str){

        //根据逗号截取字符串数组
        String[] str1 = str.split("/");
        //创建Map对象
        Map<String,Integer> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split(":");

            //str2[0]为KEY,str2[1]为值
            String key = str2[0].trim();
            int value =  Integer.parseInt(str2[1]);

            if (str2.length == 2){
                map.put(key,value);
            }else{
                map.put(key,null);
            }

        }
        return map;
    }

}
