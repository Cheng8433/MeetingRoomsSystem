package com.myGroup.elasticsearch.service;

import com.myGroup.entity.MeetingRoom;
import com.myGroup.mapper.MeetingRoomMapper; // 你的 MyBatis Mapper
import com.myGroup.elasticsearch.document.MeetingRoomDoc;
import com.myGroup.elasticsearch.repository.MeetingRoomSearchRepository;
import com.myGroup.elasticsearch.util.MeetingRoomConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticsearchSyncRunner implements CommandLineRunner {

    private final MeetingRoomMapper meetingRoomMapper;
    private final MeetingRoomSearchRepository searchRepository;

    public ElasticsearchSyncRunner(MeetingRoomMapper meetingRoomMapper,
                                   MeetingRoomSearchRepository searchRepository) {
        this.meetingRoomMapper = meetingRoomMapper;
        this.searchRepository = searchRepository;
    }

    @Override
    public void run(String... args) {
        // 全量同步：先删除原有索引（谨慎操作，或根据业务决定是否保留）
        // searchRepository.deleteAll(); // 如果需要重建索引则打开

        List<MeetingRoom> roomList = meetingRoomMapper.selectAll(); // 需在 Mapper 中定义 selectAll 方法
        List<MeetingRoomDoc> docList = roomList.stream()
                .map(MeetingRoomConverter::toDocument)
                .collect(Collectors.toList());

        searchRepository.saveAll(docList);
        System.out.println("已同步 " + docList.size() + " 条会议室数据到 Elasticsearch");
    }
}
