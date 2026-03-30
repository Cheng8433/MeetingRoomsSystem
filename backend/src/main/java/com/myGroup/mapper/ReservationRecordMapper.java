package com.myGroup.mapper;


import com.myGroup.entity.ReservationRecord;
import com.myGroup.entity.ReservationRecordStatus;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRecordMapper {
    // 插入记录，系统调用
    int insertReservationRecord(ReservationRecord reservation);
    // 更新一条记录，用户和管理员调用
    int updateReservationRecord(ReservationRecord reservation);
    //删除一条记录，管理员调用
    int deleteReservationRecordById(int reservation_id);

    int deleteReservationRecordByUserId(int user_id);

    //查询一条记录，用户和管理员调用
    ReservationRecord selectReservationRecordById(int reservation_id);
    //查询所以记录,管理员调用
    List<ReservationRecord> selectAllReservationRecords();

    // 状态相关查询
    List<ReservationRecord> selectReservationRecordsByStatus(ReservationRecordStatus status);

    List<ReservationRecord> selectReservationRecordsByUserId(int user_id);

    List<ReservationRecord> selectReservationRecordsByRoomId(int room_id);

    // 时间相关查询
    List<ReservationRecord> selectReservationRecordsByTimeRange(LocalDateTime start, LocalDateTime end);
    List<ReservationRecord> selectConflictingReservationRecords(@Param("room_id") int room_id,
                                                                @Param("reserve_date") java.time.LocalDate reserve_date,  // 添加日期参数
                                                                @Param("start") java.time.LocalTime start,
                                                                @Param("end") java.time.LocalTime end);

    // 状态更新操作
    int updateReservationRecordStatus(@Param("reservationId") int reservationId,
                                @Param("status") ReservationRecordStatus status);

    int rejectedReservationRecord(@Param("reservationId") int reservationId,
                            @Param("reason") String reason);

    int count();

    /**
     * 分页查询所有预约记录，按创建时间降序排序
     * @param offset 起始行数（从0开始）
     * @param limit  每页数量
     * @return 当前页的预约记录列表
     */
    List<ReservationRecord> selectByPage(@Param("offset") Integer offset,
                                         @Param("limit") Integer limit);


}
