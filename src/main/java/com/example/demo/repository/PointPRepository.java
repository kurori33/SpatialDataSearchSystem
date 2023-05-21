package com.example.demo.repository;

import com.example.demo.dto.PLabelsDto;
import com.example.demo.dto.SubNodeDto;
import com.example.demo.entity.PointP;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface PointPRepository extends Neo4jRepository<PointP,String> {

    Optional<PointP> findById(Long id);


    //根据id获取节点p
    @Query("match(n:p) where id(n)=$id return n")
    PointP getPointPById(long id);

    //获取p类型点的总记录数
    @Query("match(n:p) return count(n)")
    int getCountP();

    //分页获取p类型点及其标签
    @Query("MATCH (n:p) RETURN n as pointP, labels(n) as labels skip $pageIndex limit $pageSize")
    List<PLabelsDto> getLabelsP(@Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);

    //分页获取p类型点
    @Query("MATCH (n:p) RETURN n skip $pageIndex limit $pageSize")
    List<PointP> getPointP(@Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);


    //获得uri为uri的根节点的深度为3的所有子结点
    @Query("MATCH path = ((n:p)-[*1..3]->(nn)) where n.uri = $uri and not nn.uri = $uri RETURN id(nn) as subNodeID, length(path) as path limit 25")
    List<SubNodeDto> getSubtree(@Param("uri") String uri);

    //随机获得一个p点
    @Query("MATCH (n:p) return n skip $rand limit 1")
    PointP getRandPointP(@Param("rand") int rand);

}
