package com.myGroup.elasticsearch.service;

import com.myGroup.elasticsearch.document.MeetingRoomDoc;
import com.myGroup.elasticsearch.repository.MeetingRoomSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MeetingRoomSearchService {
    @Autowired
    private final MeetingRoomSearchRepository searchRepository;

    public MeetingRoomSearchService(MeetingRoomSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public List<MeetingRoomDoc> searchByKeyword(String keyword) {
        // 调用 Repository 的全文搜索方法
        List<MeetingRoomDoc> ls = new ArrayList<>();
        ls.addAll(searchRepository.searchByRoomNamePhrase(keyword));
        ls.addAll(searchRepository.searchByDescriptionPhrase(keyword));
        return ls;
    }

    public List<MeetingRoomDoc> searchByCapacityRange(Integer min, Integer max) {
        return searchRepository.findByCapacityBetween(min, max);
    }

    // 可根据需要添加更多搜索方法
}