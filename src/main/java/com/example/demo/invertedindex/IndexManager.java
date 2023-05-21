package com.example.demo.invertedindex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;


public class IndexManager {

//    String root = System.getProperty("user.dir");
    String root =  "D:\\学习\\毕业设计\\demo";
    String path1 = root + File.separator + "src\\main\\java\\com\\example\\demo\\invertedindex\\index"; //索引库位置
    String temp = root.substring(0,root.lastIndexOf("\\"));
    String path2 = temp  + File.separator + "document"; //关键字文档位置

    String keywordsNode; //存储含有特定关键字的所有节点的名称，用逗号隔开

    //创建索引库
    public void createIndex() throws Exception{

        Directory directory = FSDirectory.open( new File(path1).toPath());

        Analyzer analyzer = new StandardAnalyzer(); // 标准分词器，适用于英文

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));

        File dir = new File(path2);
        String[] list = dir.list();

        for(String fn : list){

           File f = new File(path2 + File.separator + fn);

            //文件名
            String fileName = f.getName();
            //文件路径
            String filePath = f.getPath();
            //文件内容
            String fileContent = FileUtils.readFileToString(f, "utf-8");
            //文件大小
            long fileSize = FileUtils.sizeOf(f);

            Field fieldName = new TextField("name",fileName,Field.Store.YES);
            Field fieldPath = new StoredField("path",filePath);
            Field fieldContent = new TextField("content",fileContent,Field.Store.YES);
            Field fieldSizeStore = new StoredField("size",fileSize);
            Field fieldSizeValue = new LongPoint("size",fileSize);

            Document document = new Document();

            document.add(fieldName);
            document.add(fieldPath);
            document.add(fieldContent);
            document.add(fieldSizeStore);
            document.add(fieldSizeValue);

            indexWriter.addDocument(document);


        }

        indexWriter.close();

    }

    //查询索引库
    public String searchIndex(String keyword) throws Exception{

        Directory directory = FSDirectory.open( new File(path1).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = new TermQuery(new Term("content", keyword));
        int maxnum = 20000; //查询返回的最大记录数
        TopDocs topDocs = indexSearcher.search(query, maxnum);
//        System.out.println("关键字"+keyword+"查询总记录数：" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        keywordsNode = "";

        for ( ScoreDoc doc : scoreDocs ) {
            //取文档id
            int docID = doc.doc;
            //根据id取文档对象
            Document document = indexSearcher.doc(docID);
            //取文件名
            String fileName = document.get("name");

            String name = StringUtils.substringBefore(fileName, ".");

            if(keywordsNode.equals("")){

                keywordsNode = keywordsNode + name;
            }
            else{
                keywordsNode = keywordsNode + "," + name;
            }
        }

        indexReader.close();
        return (keywordsNode);
    }


    //根据查询删除文档,在name域中删除有text关键词的文档
    public void deleteDocumentByQuery(String name,String text) throws Exception{

        Directory directory = FSDirectory.open( new File(path1).toPath());

        Analyzer analyzer = new StandardAnalyzer(); // 标准分词器，适用于英文

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));

        indexWriter.deleteDocuments(new Term(name,text));

    }

    //删除全部文档
    public void deleteAllDocument() throws Exception{

        Directory directory = FSDirectory.open( new File(path1).toPath());

        Analyzer analyzer = new StandardAnalyzer(); // 标准分词器，适用于英文

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));

        indexWriter.deleteAll();

        indexWriter.close();

    }

}
