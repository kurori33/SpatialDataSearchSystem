//package com.example.demo.wordembedding;
//
//
//import com.example.demo.entity.PointP;
//import com.example.demo.service.ResourceService;
//import com.example.demo.wordneighborhoods.AlphaRadiusWordN;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.*;
//import java.math.BigDecimal;
//import java.util.*;
//
//@Component
//public class WordEmbeddingList {
//
//    private static ResourceService resourceService;
//    @Autowired
//    public void setResourceService(ResourceService resourceService){
//        WordEmbeddingList.resourceService = resourceService;
//    }
//
//    AlphaRadiusWordN alphaRadiusWordN = new AlphaRadiusWordN();
//
//    //词向量768维
//    public String getWordEmbedding(String keyword){
//        String root = System.getProperty("user.dir");
//        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordembedding\\bert.py";
//        String result = "";
//        String temp;
//        try {
//            long startTime = System.currentTimeMillis();    //获取开始时间
//
//            String[] args1 = new String[]{"python", filePath, keyword};
//            Process proc = Runtime.getRuntime().exec(args1);//执行PY文件
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//
//            while ((temp = in.readLine())!=null){
//
//                result = result + temp;
//            }
//            in.close();
//            proc.waitFor();
//
//            long endTime = System.currentTimeMillis();    //获取结束时间
//            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//    //字符串转数组
//    public Set<String[]> strToArray(String string){
//
//        Set<String[]> wordembSet = new HashSet<String[]>(); //保存词向量的结果
//
//        String[] str = string.split("]");
//
//
//        for(int i=0 ;i<str.length;i++){
//
//            str[i] = str[i].replace("[","");
//
//            str[i] = str[i]. replaceAll(" {2,}"," ");
//
//            str[i] = str[i].trim();
//
//            String[] temp = str[i].split(" ");
//
//            wordembSet.add(temp);
//
//        }
//
//        return wordembSet;
//
//    }
//
//
//    //根据半径字邻域生成词向量集
//    public void setWEList() throws IOException {
//
//        int pageSize = 1; //每一页大小
//        int pageIndex = 0; //当前页号
//        int pageCount = resourceService.getNumP(); //p总记录数
//
//        clearWordEmbeddingListAll();
//
//        while (pageIndex < pageCount) {
//
//            long startTime = System.currentTimeMillis();    //获取开始时间
//
//            //分页获取pagesize个节点
//            List<PointP> pplist = resourceService.getPointP(pageIndex, pageSize);
//
//            //遍历每一个节点,遍历其子结点
//            for (PointP pp : pplist) {
//
//                String s = "p" + pp.getId();
//
//                //存储该点的词向量集: <768维词向量矩阵,距离根节点的距离>
//                Map<String,Integer> WEL = new HashMap<String,Integer>();
//
//                //读取p类型节点的半径字邻域
//                String WNpString = alphaRadiusWordN.readWordNeighborhoodsP(s);
//
//                //string -> map
//                WNpString = WNpString.substring(0,WNpString.indexOf("}"));
//                String temp = WNpString.substring(0, WNpString.indexOf("{"));
//                WNpString = WNpString.substring(temp.length()+1, WNpString.length());
//                Map<String,Integer> WNp = alphaRadiusWordN.getStringToMap(WNpString);
//
//                //遍历半径字邻域内的关键字
//                for (Map.Entry<String, Integer> entry : WNp.entrySet()) {
//
//                    if(entry.getKey().equals("") || entry.getKey().equals(null)){
//                        continue;
//                    }
//
//                    //获得关键字的词向量,一个关键字可能有多个词向量: heys -> hey + s
//                    String result = getWordEmbedding(entry.getKey());
//                    //将字符串形式词向量转换成set形式：[[a][b]] -> set{a,b}
//                    Set<String[]> wordembSet = strToArray(result);
//
//                    //遍历一个关键字的每一个词向量
//                    for(String[] wordemb:wordembSet){
//
//                        //科学计数法转换成普通类型：-4.81321543e-01 -> -0.481321543
//                        double[] dou = strToDouble(wordemb);
//
//                        //获得词向量字符串形式[1 2 ... 13 15]
//                        String wordembStr = "[";
//
//                        for(int i=0 ; i<dou.length ; i++){
//
//                            if(i!=dou.length-1){
//                                wordembStr = wordembStr + dou[i] + " ";
//                            }
//                            else{
//                                wordembStr = wordembStr + dou[i] + "]";
//                            }
//                        }
//
//                        //将词向量及其与根节点的语义距离加入WEL
//                        WEL.put(wordembStr,entry.getValue());
//
//                    }
//
//                }
//
//
//
//                String WELString = alphaRadiusWordN.getMapToString(WEL);
//
//                String name = s;
//
//                writeWordEmbeddingList(name,WELString);
//                writeWordEmbeddingListAll(name,WELString);
//
//            }
//
//            //当前页号改变
//            pageIndex = pageIndex + pageSize;
//
//            long endTime = System.currentTimeMillis();    //获取结束时间
//            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
//            System.out.println(pageIndex);
//
//        }
//
//    }
//
//    public String readWordEmbeddingList(String name) throws IOException {
//
//        String root = System.getProperty("user.dir");
//        root= root.substring(0,root.lastIndexOf("\\"));
//        String fileName = name + ".txt";
//
//        String filePath = root + File.separator + "wordEmbeddingList" +File.separator  + fileName;
//
//        BufferedReader br = new BufferedReader( new FileReader(filePath));
//        String WELString = br.readLine();
//        br.close();
//
//        return WELString;
//    }
//
//    public void writeWordEmbeddingList(String name, String WELString) throws IOException {
//
//        String root = System.getProperty("user.dir");
//        root= root.substring(0,root.lastIndexOf("\\"));
//        String fileName = name + ".txt";
//
//        String filePath = root + File.separator + "wordEmbeddingList" +File.separator  + fileName;
//
//        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
//        bw.write(name +":{"+WELString+"}");
//        bw.close();
//
//    }
//
//    public void writeWordEmbeddingListAll(String name, String WELString) throws IOException {
//
//        String root = System.getProperty("user.dir");
//        String fileName = "wordEmbeddingList.txt";
//        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordembedding" +File.separator  + fileName;
//
//
//        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath,true));
//        bw.write(name +":{"+WELString+"}");
//        bw.newLine();
//        bw.close();
//
//    }
//
//    public void clearWordEmbeddingListAll() throws IOException {
//
//        String root = System.getProperty("user.dir");
//        String fileName = "wordEmbeddingList.txt";
//        String filePath = root + File.separator + "src\\main\\java\\com\\example\\demo\\wordembedding" +File.separator  + fileName;
//
//        BufferedWriter bw = new BufferedWriter( new FileWriter(filePath));
//        bw.write("");
//        bw.close();
//
//    }
//
//    //计算两个词向量的语义相关度
//    public double calSemRelevance(String[] m, String[] n){
//
//        double SR = 0; //语义相关度
//        double [] dm = strToDouble(m);
//        double [] dn = strToDouble(n);
//        double mn = 0;
//        double mm = 0;
//        double nn = 0;
//
//        for(int i=0 ; i<m.length ; i++){
//
//            mn = mn + dm[i]*dn[i];
//            mm = mm + dm[i]*dm[i];
//            nn = nn + dn[i]*dn[i];
//
//        }
//
//        SR = mn / ( Math.sqrt(mm)*Math.sqrt(nn) );
//
//        return SR;
//    }
//
//    //科学计数法string数组转double数组: "3.30110461e-01" -> 0.330110461
//    public double[] strToDouble(String[] str){
//
//        double[] dou = new double[768]; //保存结果
//
//        for(int i=0 ; i<str.length ; i++){
//
//            if(!str[i].contains("e")){
//                dou[i] = Double.parseDouble(str[i]);
//                continue;
//            }
//
//            String numStr = str[i].substring(0,str[i].lastIndexOf("e"));
//            String indexStr = str[i].substring(numStr.length()+1, str[i].length());
//
//            double num = Double.parseDouble(numStr);
//            int index = Integer.parseInt(indexStr);
//
//            BigDecimal a = new BigDecimal(Double.toString(num));
//
//            BigDecimal b = new BigDecimal(Double.toString(Math.pow(10,index)));
//
//            BigDecimal result = a.multiply(b); //相乘
//
//            dou[i] = result.doubleValue();
//
//        }
//
//        return dou;
//    }
//
//}
