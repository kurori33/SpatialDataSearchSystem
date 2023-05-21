package com.example.demo.repository;

import com.example.demo.dto.VLabelsDto;
import com.example.demo.entity.PointV;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

public interface PointVRepository extends Neo4jRepository<PointV,String> {

    Optional<PointV> findById(Long id);

    //根据节点uri查询节点所有标签值
    @Query("MATCH (v) WHERE v.uri = $uri RETURN labels(v)")
    String getAllLabels(@Param("uri") String uri);

    //获取v类型点的总记录数
    @Query("match(n:v) return count(n)")
    int getCountV();

    //分页获取v类型点及其标签
    @Query("MATCH (n:v) RETURN n as pointV, labels(n) as labels skip $pageIndex limit $pageSize")
    List<VLabelsDto> getLabelsV(@Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);


}
