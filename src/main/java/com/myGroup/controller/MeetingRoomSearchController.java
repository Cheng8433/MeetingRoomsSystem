package com.myGroup.controller;

import com.myGroup.dto.ApiResponse;
import com.myGroup.elasticsearch.document.MeetingRoomDoc;
import com.myGroup.elasticsearch.service.MeetingRoomSearchService;
import com.myGroup.elasticsearch.util.MeetingRoomConverter;
import com.myGroup.entity.MeetingRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class MeetingRoomSearchController {

    private final MeetingRoomSearchService searchService;

    // 辅助方法：创建成功响应
    private <T> ResponseEntity<ApiResponse<T>> successResponse(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return successResponse(data, "操作成功");
    }

    public MeetingRoomSearchController(MeetingRoomSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<MeetingRoom>>> searchRooms(@RequestParam(required = false) String searchKeyWord,
                                                                      @RequestParam(required = false) Integer minCapacity,
                                                                      @RequestParam(required = false) Integer maxCapacity) {
        if (searchKeyWord != null && !searchKeyWord.isEmpty()) {
            return successResponse(searchService.searchByKeyword(searchKeyWord).stream().map(MeetingRoomConverter::toEntity).collect(Collectors.toList()));
        } else if (minCapacity != null && maxCapacity != null) {
            return successResponse(searchService.searchByCapacityRange(minCapacity, maxCapacity).stream().map(MeetingRoomConverter::toEntity).collect(Collectors.toList()));
        }
        // 默认返回所有（生产环境应加分页）
        return successResponse(searchService.searchByKeyword("").stream().map(MeetingRoomConverter::toEntity).collect(Collectors.toList()));
    }
}