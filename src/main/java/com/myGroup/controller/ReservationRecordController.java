package com.myGroup.controller;

import com.myGroup.dto.ApiResponse;
import com.myGroup.dto.CreateReservationDTO;
import com.myGroup.dto.PageResult;
import com.myGroup.entity.ReservationRecord;
import com.myGroup.entity.ReservationRecordStatus;
import com.myGroup.service.ReservationRecordService;
import com.tdunning.math.stats.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预约控制器
 * 提供会议室预约相关的所有REST API接口
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationRecordController {

    @Autowired
    private ReservationRecordService reservationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 创建会议室预约
     * @return 创建结果
     */

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createReservationRecord(@RequestBody CreateReservationDTO dto) {
        // 1. 生成资源唯一键
        String resourceKey = String.format("room:book:%d:%s:%s-%s",
                dto.getRoom_id(),
                dto.getReserveDate(),          // 假设格式 yyyy-MM-dd
                dto.getStart_time(),      // 假设格式 HH:mm
                dto.getEnd_time());       // 假设格式 HH:mm

        // 2. 尝试用 Redis SET NX 抢占资源（过期时间 30 秒，防止死锁）
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(resourceKey, "1", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(success)) {
            // 资源已被抢占，返回失败（可自定义错误码）
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("该时间段已被预约，请选择其他时间"));
        }

        try {
            // 3. 转换为实体类
            ReservationRecord reservation = dto.toReservationRecord();
            boolean result = reservationService.createReservationRecord(reservation);

            if (result) {
                // 预约成功，Redis 中的 key 保留作为已预约标记（过期时间可在业务完成后重新设置更长的时间）
                redisTemplate.expire(resourceKey, Duration.ofHours(24)); // 设置与预约有效期一致
                return ResponseEntity.ok(ApiResponse.success("预约创建成功", reservation));
            } else {
                // 业务失败（如数据库异常），删除 Redis key 释放资源
                redisTemplate.delete(resourceKey);
                return ResponseEntity.badRequest().body(ApiResponse.error("预约创建失败"));
            }
        } catch (Exception e) {
            // 发生异常时也需删除 key，避免资源死锁
            redisTemplate.delete(resourceKey);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("创建预约时发生错误: " + e.getMessage()));
        }
    }
    /**
     * 确认预约
     * @param reservationId 预约ID
     * @return 确认结果
     */
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<Object>> confirmReservationRecord(@PathVariable int reservationId) {
        try {
            boolean result = reservationService.confirmReservationRecord(reservationId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("预约确认成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("预约确认失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("确认预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 拒绝预约
     * @param reservationId 预约ID
     * @param reason 拒绝原因
     * @return 拒绝结果
     */
    @PutMapping("/{reservationId}/reject")
    public ResponseEntity<ApiResponse<Object>> rejectReservationRecord(@PathVariable int reservationId,
                                                                 @RequestParam String reason) {
        try {
            boolean result = reservationService.rejectReservationRecord(reservationId, reason);
            ReservationRecord record = reservationService.selectRoomReservationRecordById(reservationId);

            if (result) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        record.getRoom_id(), record.getReservedDate(), record.getStart_time(), record.getEnd_time());
                redisTemplate.delete(resourceKey);
                return ResponseEntity.ok(ApiResponse.success("预约拒绝成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("预约拒绝失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("拒绝预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 开始预约
     * @param reservationId 预约ID
     * @return 开始结果
     */
    @PutMapping("/{reservationId}/start")
    public ResponseEntity<ApiResponse<Object>> startReservationRecord(@PathVariable int reservationId) {
        try {
            boolean result = reservationService.startReservationRecord(reservationId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("预约开始成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("预约开始失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("开始预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 完成预约
     * @param reservationId 预约ID
     * @return 完成结果
     */
    @PutMapping("/{reservationId}/complete")
    public ResponseEntity<ApiResponse<Object>> completeReservationRecord(@PathVariable int reservationId) {
        try {
            boolean result = reservationService.completeReservationRecord(reservationId);
            ReservationRecord record = reservationService.selectRoomReservationRecordById(reservationId);

            if (result) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        record.getRoom_id(), record.getReservedDate(), record.getStart_time(), record.getEnd_time());
                redisTemplate.delete(resourceKey);

                return ResponseEntity.ok(ApiResponse.success("预约完成成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("预约完成失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("完成预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 取消预约
     * @param reservationId 预约ID
     * @param userId 用户ID
     * @param isAdmin 是否是管理员
     * @return 取消结果
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelReservationRecord(@PathVariable int reservationId,
                                                                 @RequestParam int userId,
                                                                 @RequestParam(defaultValue = "false") boolean isAdmin) {
        try {
            boolean result = reservationService.cancelReservationRecord(reservationId, userId, isAdmin);
            ReservationRecord record = reservationService.selectRoomReservationRecordById(reservationId);

            if (result) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        record.getRoom_id(), record.getReservedDate(), record.getStart_time(), record.getEnd_time());
                redisTemplate.delete(resourceKey);

                return ResponseEntity.ok(ApiResponse.success("预约取消成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("预约取消失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("取消预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的所有预约记录
     * @param userId 用户ID
     * @return 用户的预约列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReservationRecord>>> getUserReservationRecords(@PathVariable int userId) {
        try {
            List<ReservationRecord> reservations = reservationService.getUserReservationRecords(userId);
            return ResponseEntity.ok(ApiResponse.success("获取用户预约成功", reservations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取用户预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 获取待处理的预约列表
     * @return 待处理预约列表
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ReservationRecord>>> getPendingReservationRecords() {
        try {
            List<ReservationRecord> reservations = reservationService.getPendingReservationRecords();
            return ResponseEntity.ok(ApiResponse.success("获取待处理预约成功", reservations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取待处理预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 根据状态获取用户的预约记录
     * @param userId 用户ID
     * @param status 预约状态
     * @return 符合条件的预约列表
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ReservationRecord>>> getUserReservationRecordsByStatus(
            @PathVariable int userId,
            @PathVariable ReservationRecordStatus status) {
        try {
            List<ReservationRecord> reservations = reservationService.getUserReservationRecordsByStatus(userId, status);
            return ResponseEntity.ok(ApiResponse.success("获取用户状态预约成功", reservations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取用户状态预约时发生错误: " + e.getMessage()));
        }
    }



    /**
     * 处理过期预约记录（管理员功能）
     * @return 处理结果
     */
    @PostMapping("/process-expired")
    public ResponseEntity<ApiResponse<Object>> processExpiredReservationRecords() {
        try {
            reservationService.processExpiredReservationRecords();
            return ResponseEntity.ok(ApiResponse.success("过期预约处理完成", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("处理过期预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 获取会议室在特定时间段的预约情况
     * @param roomId 会议室ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 预约列表
     */
    @GetMapping("/room/{roomId}/schedule")
    public ResponseEntity<ApiResponse<List<ReservationRecord>>> getRoomReservationRecordsDuringTime(
            @PathVariable int roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<ReservationRecord> reservations = reservationService.getRoomReservationRecordsDuringTime(roomId, start, end);
            return ResponseEntity.ok(ApiResponse.success("获取会议室时间段预约成功", reservations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取会议室时间段预约时发生错误: " + e.getMessage()));
        }
    }

    /**
     * 分页获取所有预约记录（按创建时间排序）
     * @param page     页码（默认0，从0开始）
     * @param size     每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PageResult<ReservationRecord>>> getReservationsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {

            Pageable pageable = PageRequest.of(page, size);

            // 调用服务层分页查询
            Page<ReservationRecord> pageResult = reservationService.getAllReservations(pageable);

            // 封装为自定义分页结果
            PageResult<ReservationRecord> result = new PageResult<>();
            result.setContent(pageResult.getContent());
            result.setTotalElements(pageResult.getTotalElements());
            result.setTotalPages(pageResult.getTotalPages());
            result.setCurrentPage(pageResult.getNumber());
            result.setPageSize(pageResult.getSize());

            return ResponseEntity.ok(ApiResponse.success("获取预约列表成功", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取预约列表失败: " + e.getMessage()));
        }
    }
}