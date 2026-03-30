package com.myGroup.mapper;

import com.myGroup.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingRoomMapper {

    /**
     * 插入会议室
     */
    int insert(MeetingRoom meetingRoom);

    /**
     * 根据ID删除会议室
     */
    int deleteById(Integer roomId);

    /**
     * 更新会议室信息
     */
    int update(MeetingRoom meetingRoom);

    /**
     * 根据ID查询会议室
     */
    MeetingRoom selectById(Integer roomId);

    /**
     * 根据会议室编号查询
     */
    MeetingRoom selectByRoomNo(String roomNo);

    /**
     * 查询所有会议室
     */
    List<MeetingRoom> selectAll();

    /**
     * 根据状态查询会议室
     */
    List<MeetingRoom> selectByStatus(Integer roomStatus);

    /**
     * 根据容量查询会议室（大于等于指定容量）
     */
    List<MeetingRoom> selectByCapacity(Integer minCapacity);

    /**
     * 根据会议室名称模糊查询
     */
    List<MeetingRoom> selectByName(String roomName);

    /**
     * 检查会议室编号是否存在
     */
    boolean existsByRoomNo(String roomNo);

    /**
     * 检查会议室编号是否存在（排除指定ID）
     */
    boolean existsByRoomNoExcludeId(@Param("roomNo") String roomNo, @Param("roomId") Integer roomId);

    /**
     * 更新会议室状态
     */
    int updateStatus(@Param("roomId") Integer roomId, @Param("roomStatus") Integer roomStatus);

    /**
     * 分页查询会议室
     */
    List<MeetingRoom> selectByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计会议室数量
     */
    int count();

    /**
     * 根据创建人查询会议室
     */
    List<MeetingRoom> selectByCreateUserId(Integer createUserId);

    /**
     * 查询可用会议室（状态为1）
     */
    List<MeetingRoom> selectAvailableRooms();
}