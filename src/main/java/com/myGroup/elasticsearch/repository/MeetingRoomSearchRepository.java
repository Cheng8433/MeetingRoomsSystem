package com.myGroup.elasticsearch.repository;

import com.myGroup.elasticsearch.document.MeetingRoomDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface MeetingRoomSearchRepository extends ElasticsearchRepository<MeetingRoomDoc, Integer> {

    // 根据名称或描述进行全文搜索
    //List<MeetingRoomDoc> findByRoomNameContainingOrDescriptionContaining(String roomNameKeyword, String descriptionKeyword);

    @Query("{\"match\":{\"roomName\":\"?0\"}}")
    List<MeetingRoomDoc> searchByRoomNamePhrase(String keyword);

    @Query("{\"match\":{\"description\":\"?0\"}}")
    List<MeetingRoomDoc> searchByDescriptionPhrase(String description);

    // 根据容量范围查询
    List<MeetingRoomDoc> findByCapacityBetween(Integer min, Integer max);

    // 根据状态查询
    List<MeetingRoomDoc> findByRoomStatus(Integer status);

    // 可以自定义更多查询方法，或者使用 @Query 注解编写 Elasticsearch Query DSL
}