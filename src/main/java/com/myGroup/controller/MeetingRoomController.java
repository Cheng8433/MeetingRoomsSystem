package com.myGroup.controller;

import com.myGroup.dto.ApiResponse;
import com.myGroup.entity.MeetingRoom;
import com.myGroup.exception.BusinessException;
import com.myGroup.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/meeting-rooms")
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;

    // 辅助方法：创建成功响应
    private <T> ResponseEntity<ApiResponse<T>> successResponse(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return successResponse(data, "操作成功");
    }

    // 辅助方法：创建错误响应
    private <T> ResponseEntity<ApiResponse<T>> errorResponse(String message, String code, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.error(message, code);
        return ResponseEntity.status(status).body(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return errorResponse(message, "400", HttpStatus.BAD_REQUEST);
    }

    private <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return errorResponse(message, "404", HttpStatus.NOT_FOUND);
    }

    private <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        return errorResponse(message, "500", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 创建会议室
     * POST /api/meeting-rooms
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MeetingRoom>> createMeetingRoom(@Valid @RequestBody MeetingRoom meetingRoom) {
        try {
            MeetingRoom createdRoom = meetingRoomService.addMeetingRoom(meetingRoom);
            ApiResponse<MeetingRoom> response = ApiResponse.success("会议室创建成功", createdRoom);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return internalError("创建会议室失败，请稍后重试");
        }
    }

    /**
     * 更新会议室信息
     * PUT /api/meeting-rooms/{roomId}
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<MeetingRoom>> updateMeetingRoom(
            @PathVariable Integer roomId,
            @Valid @RequestBody MeetingRoom meetingRoom) {
        try {
            // 确保路径参数与请求体中的ID一致
            if (meetingRoom.getRoomId() == null) {
                meetingRoom.setRoomId(roomId);
            } else if (!meetingRoom.getRoomId().equals(roomId)) {
                return badRequest("请求参数不一致");
            }

            MeetingRoom updatedRoom = meetingRoomService.updateMeetingRoom(meetingRoom);
            return successResponse(updatedRoom, "会议室更新成功");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return internalError("更新会议室失败，请稍后重试");
        }
    }


    /**
     * 更新会议室状态
     * PATCH /api/meeting-rooms/{roomId}/status?status={status}
     */
    @PatchMapping("/{roomId}/status")
    public ResponseEntity<ApiResponse<MeetingRoom>> updateMeetingRoomStatus(
            @PathVariable Integer roomId,
            @RequestParam Integer status) {
        try {
            MeetingRoom updatedRoom = meetingRoomService.updateMeetingRoomStatus(roomId, status);
            ApiResponse<MeetingRoom> response = ApiResponse.success(
                    "会议室状态更新成功，当前状态为：" + (status == 1 ? "可用" : "不可用"),
                    updatedRoom);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            // 业务异常
            ApiResponse<MeetingRoom> response = ApiResponse.error(e.getMessage(), "400");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // 系统异常
            ApiResponse<MeetingRoom> response = ApiResponse.error("更新会议室状态失败，请稍后重试", "500");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取会议室详情
     * GET /api/meeting-rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<MeetingRoom>> getMeetingRoomById(@PathVariable Integer roomId) {
        try {
            MeetingRoom meetingRoom = meetingRoomService.getMeetingRoomById(roomId);
            if (meetingRoom == null) {
                return notFound("会议室不存在");
            }
            return successResponse(meetingRoom);
        } catch (Exception e) {
            return internalError("获取会议室信息失败");
        }
    }

    /**
     * 根据ID删除会议室
     * DELETE /api/meeting-rooms/{roomId}
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeetingRoom(@PathVariable Integer roomId) {
        try {
            meetingRoomService.deleteMeetingRoom(roomId);
            // 删除成功时，返回空数据体
            ApiResponse<Void> response = ApiResponse.success("会议室删除成功", null);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            // 业务异常
            ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "400");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // 系统异常
            ApiResponse<Void> response = ApiResponse.error("删除会议室失败，请稍后重试", "500");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有可用会议室
     * GET /api/meeting-rooms/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MeetingRoom>>> getAvailableRooms() {
        try {
            List<MeetingRoom> availableRooms = meetingRoomService.getAvailableRooms();
            return successResponse(availableRooms);
        } catch (Exception e) {
            return internalError("获取可用会议室列表失败");
        }
    }

    /**
     * 根据容量筛选会议室
     * GET /api/meeting-rooms/capacity?minCapacity={minCapacity}
     */
    @GetMapping("/capacity")
    public ResponseEntity<ApiResponse<List<MeetingRoom>>> getRoomsByCapacity(
            @RequestParam(required = false) Integer minCapacity) {
        try {
            // 如果没有提供容量参数，返回错误
            if (minCapacity == null) {
                return badRequest("请输入筛选容量");
            }

            // 验证容量参数有效性
            if (minCapacity <= 0) {
                return badRequest("容量参数必须大于0");
            }

            List<MeetingRoom> rooms = meetingRoomService.getRoomsByCapacity(minCapacity);
            return successResponse(rooms);
        } catch (Exception e) {
            return internalError("根据容量筛选会议室失败");
        }
    }

    /**
     * 分页查询会议室
     * GET /api/meeting-rooms/page?page={page}&size={size}
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<List<MeetingRoom>>> getRoomsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 验证分页参数
            if (page == null || page <= 0) {
                return badRequest("页码必须大于0");
            }
            if (size == null || size <= 0) {
                return badRequest("每页大小必须大于0");
            }
            if (size > 100) {
                return badRequest("每页大小不能超过100");
            }

            List<MeetingRoom> rooms = meetingRoomService.getRoomsByPage(page, size);
            return successResponse(rooms);
        } catch (Exception e) {
            return internalError("分页查询会议室失败");
        }
    }

    /**
     * 组合查询：获取可用且满足容量的会议室
     * GET /api/meeting-rooms/available/capacity?minCapacity={minCapacity}
     */
    @GetMapping("/available/capacity")
    public ResponseEntity<ApiResponse<List<MeetingRoom>>> getAvailableRoomsByCapacity(
            @RequestParam(required = false) Integer minCapacity) {
        try {
            // 获取所有可用会议室
            List<MeetingRoom> availableRooms = meetingRoomService.getAvailableRooms();

            // 如果有容量筛选条件，过滤结果
            if (minCapacity != null && minCapacity > 0) {
                availableRooms.removeIf(room -> room.getCapacity() < minCapacity);
            }

            return successResponse(availableRooms);
        } catch (Exception e) {
            return internalError("查询会议室失败");
        }
    }
}