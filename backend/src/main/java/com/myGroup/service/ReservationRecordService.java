// 文件位置：src/main/java/com/myGroup/service/ReservationRecordService.java
package com.myGroup.service;

import com.myGroup.entity.ReservationRecord;
import com.myGroup.entity.ReservationRecordStatus;
import com.myGroup.exception.BusinessException;
import com.myGroup.mapper.ReservationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.myGroup.entity.ReservationRecordStatus.CANCELLED;
import static java.sql.Types.NULL;

@Service
@Transactional
public class ReservationRecordService{

    @Autowired
    private ReservationRecordMapper reservationMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean createReservationRecord(ReservationRecord reservation) {
        // 1. 验证预约时间合理性
        if (reservation.getStart_time() == null || reservation.getEnd_time() == null) {
            throw new BusinessException("预约时间不能为空", "TIME_EMPTY");
        }

        if (reservation.getEnd_time().isBefore(reservation.getStart_time())) {
            throw new BusinessException("结束时间不能早于开始时间", "TIME_INVALID");
        }

        // 获取当前时间，不能预约过去的时间
        LocalDateTime now = LocalDateTime.now();
        // 组合预约日期和开始时间
        LocalDateTime reservationDateTime = LocalDateTime.of(
                reservation.getReservedDate(),
                reservation.getStart_time()
        );
        if (reservationDateTime.isBefore(now)) {
            throw new BusinessException("不能预约过去的时间", "TIME_EXPIRED");
        }

        // 2. 检查时间冲突
        List<ReservationRecord> conflictingReservationRecords = reservationMapper.selectConflictingReservationRecords(
                reservation.getRoom_id(),
                reservation.getReservedDate(),
                reservation.getStart_time(),
                reservation.getEnd_time()
        );

        // 移除自身（如果是更新操作）
        conflictingReservationRecords.removeIf(conflict ->
                conflict.getReservationRecord_id() == reservation.getReservationRecord_id());

        if (!conflictingReservationRecords.isEmpty()) {
            throw new BusinessException("该时间段内会议室已被预约", "TIME_CONFLICT");
        }

        // 3. 设置默认状态和时间
        reservation.setCreated_time(LocalDateTime.now());
        reservation.setUpdated_time(LocalDateTime.now());
        reservation.setApproverId(1);

        // 4. 插入数据库
        return reservationMapper.insertReservationRecord(reservation) > 0;
    }


    public boolean confirmReservationRecord(int reservationId) {
        ReservationRecord reservation = reservationMapper.selectReservationRecordById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在", "RESERVATION_NOT_FOUND");
        }

        // 验证状态转换是否允许
        if (reservation.getReservationRecord_status() != ReservationRecordStatus.PENDING) {
            throw new BusinessException("只能确认待审核状态的预约", "STATUS_INVALID");
        }

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 组合预约日期和开始时间
        LocalDateTime reservationDateTime = LocalDateTime.of(
                reservation.getReservedDate(),
                reservation.getStart_time()
        );

        // 检查预约时间是否已过
        if (reservationDateTime.isBefore(now)) {
            throw new BusinessException("预约开始时间已过，无法确认", "TIME_EXPIRED");
        }

        reservation.setReservationRecord_status(ReservationRecordStatus.CONFIRMED);
        reservation.setUpdated_time(LocalDateTime.now());
        reservation.setApproverId(reservation.getUser_id());

        return reservationMapper.updateReservationRecord(reservation) > 0;
    }

    public boolean rejectReservationRecord(int reservationId, String reason) {
        ReservationRecord reservation = reservationMapper.selectReservationRecordById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在", "RESERVATION_NOT_FOUND");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("拒绝原因不能为空", "REASON_EMPTY");
        }

        reservation.setReservationRecord_status(ReservationRecordStatus.REJECTED);
        reservation.setRejected_reason(reason);
        reservation.setUpdated_time(LocalDateTime.now());

        return reservationMapper.updateReservationRecord(reservation) > 0;
    }

    public boolean startReservationRecord(int reservationId) {
        ReservationRecord reservation = reservationMapper.selectReservationRecordById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在", "RESERVATION_NOT_FOUND");
        }

        LocalDateTime now = LocalDateTime.now();

        // 验证状态和时间的合理性
        if (reservation.getReservationRecord_status() != ReservationRecordStatus.CONFIRMED) {
            throw new BusinessException("只有已确认的预约才能开始使用", "STATUS_INVALID");
        }

        // 组合预约日期和开始时间
        LocalDateTime reservationDateTime = LocalDateTime.of(
                reservation.getReservedDate(),
                reservation.getStart_time()
        );
        if (now.isBefore(reservationDateTime.minusMinutes(15))) {
            throw new BusinessException("距离预约开始时间还有15分钟以上，无法开始使用", "TIME_TOO_EARLY");
        }

        // 组合预约日期和开始时间
        LocalDateTime reservationDateTime2 = LocalDateTime.of(
                reservation.getReservedDate(),
                reservation.getEnd_time()
        );
        if (now.isAfter(reservationDateTime2)){
            throw new BusinessException("预约结束时间已过，无法开始使用", "TIME_EXPIRED");
        }

        reservation.setReservationRecord_status(ReservationRecordStatus.IN_USE);
        reservation.setUpdated_time(now);

        return reservationMapper.updateReservationRecord(reservation) > 0;
    }

    public boolean completeReservationRecord(int reservationId) {
        ReservationRecord reservation = reservationMapper.selectReservationRecordById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在", "RESERVATION_NOT_FOUND");
        }

        // 允许从"使用中"或"已确认"状态标记为完成
        if (reservation.getReservationRecord_status() != ReservationRecordStatus.IN_USE &&
                reservation.getReservationRecord_status() != ReservationRecordStatus.CONFIRMED) {
            throw new BusinessException("只有使用中或已确认的预约才能标记为完成", "STATUS_INVALID");
        }

        reservation.setReservationRecord_status(ReservationRecordStatus.COMPLETED);
        reservation.setUpdated_time(LocalDateTime.now());

        return reservationMapper.updateReservationRecord(reservation) > 0;
    }

    public boolean cancelReservationRecord(int reservationId, int userId, boolean isAdmin) {
        ReservationRecord reservation = reservationMapper.selectReservationRecordById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在", "RESERVATION_NOT_FOUND");
        }

        // 权限验证：只有预约本人或管理员可以取消
        if (reservation.getUser_id() != userId && !isAdmin) {
            throw new BusinessException("无权取消该预约", "PERMISSION_DENIED");
        }

        // 状态验证：已完成或已取消的预约不能再次取消
        if ("COMPLETED".equals(reservation.getReservationRecord_status()) ||
                "CANCELLED".equals(reservation.getReservationRecord_status())) {
            throw new BusinessException("该预约已完成或已取消，无法再次取消", "STATUS_INVALID");
        }

        // 如果已经开始使用，需要特殊处理
        if ("IN_USE".equals(reservation.getReservationRecord_status())) {
            // 可以记录日志或发送通知
            System.out.println("警告：用户取消了正在使用中的预约");
        }

        // 设置取消状态
        reservation.setReservationRecord_status(CANCELLED);
        reservation.setUpdated_time(LocalDateTime.now());

        // 关键：根据是否是管理员设置 approver_id
        if (isAdmin) {
            // 管理员取消，记录审批人
            reservation.setApproverId(userId); // 设置管理员ID为审批人
        } else {
            // 用户自己取消，approver_id 设为 null
            reservation.setApproverId(NULL);
        }

        return reservationMapper.updateReservationRecord(reservation) > 0;
    }

    public List<ReservationRecord> getUserReservationRecords(int userId) {
        return reservationMapper.selectReservationRecordsByUserId(userId);
    }

    public List<ReservationRecord> getPendingReservationRecords() {
        return reservationMapper.selectReservationRecordsByStatus(ReservationRecordStatus.PENDING);
    }

    public List<ReservationRecord> getUserReservationRecordsByStatus(int userId, ReservationRecordStatus status) {
        List<ReservationRecord> userReservationRecords = reservationMapper.selectReservationRecordsByUserId(userId);
        userReservationRecords.removeIf(reservation -> reservation.getReservationRecord_status() != status);
        return userReservationRecords;
    }


    public void processExpiredReservationRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<ReservationRecord> allReservationRecords = reservationMapper.selectAllReservationRecords();

        for (ReservationRecord reservation : allReservationRecords) {
            // 处理未确认的过期预约
            // 组合预约日期和开始时间
            LocalDateTime reservationDateTime1 = LocalDateTime.of(
                    reservation.getReservedDate(),
                    reservation.getStart_time()
            );
            if (reservation.getReservationRecord_status() == ReservationRecordStatus.PENDING &&
                    reservationDateTime1.isBefore(now)) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        reservation.getRoom_id(), reservation.getReservedDate(), reservation.getStart_time(), reservation.getEnd_time());
                redisTemplate.delete(resourceKey);

                reservation.setReservationRecord_status(CANCELLED);
                reservation.setRejected_reason("系统自动取消：超过预约时间未确认");
                reservation.setUpdated_time(now);
                reservationMapper.updateReservationRecord(reservation);
            }

            // 处理已确认但未开始的过期预约
            // 组合预约日期和开始时间
            LocalDateTime reservationDateTime2 = LocalDateTime.of(
                    reservation.getReservedDate(),
                    reservation.getStart_time()
            );
            if (reservation.getReservationRecord_status() == ReservationRecordStatus.CONFIRMED &&
                    reservationDateTime2.isBefore(now)) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        reservation.getRoom_id(), reservation.getReservedDate(), reservation.getStart_time(), reservation.getEnd_time());
                redisTemplate.delete(resourceKey);

                reservation.setReservationRecord_status(ReservationRecordStatus.COMPLETED);
                reservation.setUpdated_time(now);
                reservationMapper.updateReservationRecord(reservation);
            }

            // 处理使用中超时的预约
            if (reservation.getReservationRecord_status() == ReservationRecordStatus.IN_USE &&
                    reservationDateTime2.isBefore(now)) {
                // 删除 Redis 标记
                String resourceKey = String.format("room:book:%d:%s:%s-%s",
                        reservation.getRoom_id(), reservation.getReservedDate(), reservation.getStart_time(), reservation.getEnd_time());
                redisTemplate.delete(resourceKey);

                reservation.setReservationRecord_status(ReservationRecordStatus.COMPLETED);
                reservation.setUpdated_time(now);
                reservationMapper.updateReservationRecord(reservation);
            }
        }
    }

    public List<ReservationRecord> getRoomReservationRecordsDuringTime(int roomId, LocalDateTime start, LocalDateTime end) {
        return reservationMapper.selectReservationRecordsByTimeRange(start, end).stream()
                .filter(reservation -> reservation.getRoom_id() == roomId)
                .toList();
    }


    public boolean deleteRoomReservationRecordByUserId(int userId) {
        try{
            return reservationMapper.deleteReservationRecordByUserId(userId) > 0;
        }
        catch (Exception e){
            throw new BusinessException("删除用户预约记录失败"+e.getMessage(),"RESERVATION_NOT_FOUND");
        }

    }
    public ReservationRecord selectRoomReservationRecordById(int reservationId) {
        try{
            return reservationMapper.selectReservationRecordById(reservationId);
        }
        catch (Exception e){
            throw new BusinessException("预约记录查找失败"+e.getMessage());
        }

    }



    public Page<ReservationRecord> getAllReservations(Pageable pageable) {
        // 计算 offset
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        // 查询当前页数据
        List<ReservationRecord> content = reservationMapper.selectByPage(offset, limit);

        // 查询总记录数
        long total = reservationMapper.count();

        // 返回 Spring Data 的 Page 对象
        return new PageImpl<>(content, pageable, total);
    }


}
