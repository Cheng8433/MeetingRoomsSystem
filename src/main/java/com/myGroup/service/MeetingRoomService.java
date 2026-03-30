package com.myGroup.service;

import com.myGroup.elasticsearch.document.MeetingRoomDoc;
import com.myGroup.elasticsearch.repository.MeetingRoomSearchRepository;
import com.myGroup.elasticsearch.util.MeetingRoomConverter;
import com.myGroup.entity.MeetingRoom;
import com.myGroup.exception.BusinessException;
import com.myGroup.mapper.MeetingRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.concurrent.TimeUnit;



@Service
public class MeetingRoomService {

    // 1. 声明 Logger（必须要有这一行）
    private static final Logger logger = LoggerFactory.getLogger(MeetingRoomService.class);

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    @Autowired
    private MeetingRoomSearchRepository searchRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    private static final String CACHE_PREFIX = "meeting_room:page:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    /**
     * 添加会议室
     */
    public MeetingRoom addMeetingRoom(MeetingRoom meetingRoom) {
        // 检查会议室编号是否已存在
        if (meetingRoomMapper.existsByRoomNo(meetingRoom.getRoomNo())) {
            throw new BusinessException("会议室编号已存在");
        }

        // 设置默认状态为可用
        if (meetingRoom.getRoomStatus() == null) {
            meetingRoom.setRoomStatus(1);
        }

        meetingRoomMapper.insert(meetingRoom);
        // 2. 同步到 Elasticsearch
        MeetingRoomDoc doc = MeetingRoomConverter.toDocument(meetingRoom);
        searchRepository.save(doc);
        return meetingRoom;
    }

    /**
     * 更新会议室信息
     */
    public MeetingRoom updateMeetingRoom(MeetingRoom meetingRoom) {
        // 检查会议室编号是否与其他会议室冲突
        if (meetingRoomMapper.existsByRoomNoExcludeId(meetingRoom.getRoomNo(), meetingRoom.getRoomId())) {
            throw new BusinessException("会议室编号已存在");
        }

        meetingRoomMapper.update(meetingRoom);
        //elasticsearch
        MeetingRoomDoc doc = MeetingRoomConverter.toDocument(meetingRoom);
        searchRepository.save(doc);
        return meetingRoomMapper.selectById(meetingRoom.getRoomId());
    }


    /**
     * 更新会议室状态
     * @param roomId 会议室ID
     * @param roomStatus 会议室状态 (0:不可用/已被占用, 1:可用)
     * @return 更新后的会议室信息
     */
    @Transactional
    public MeetingRoom updateMeetingRoomStatus(Integer roomId, Integer roomStatus) {
        // 验证状态参数
        if (roomStatus == null) {
            throw new BusinessException("会议室状态不能为空");
        }

        // 验证状态值是否合法
        if (roomStatus != 0 && roomStatus != 1) {
            throw new BusinessException("会议室状态值不合法，只能是0或1");
        }

        // 检查会议室是否存在
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(roomId);
        if (meetingRoom == null) {
            throw new BusinessException("会议室不存在");
        }

        // 检查当前状态是否与目标状态相同
        if (meetingRoom.getRoomStatus() != null && meetingRoom.getRoomStatus().equals(roomStatus)) {
            throw new BusinessException("会议室状态已经是" + (roomStatus == 1 ? "可用" : "不可用"));
        }

        // 执行状态更新
        int result = meetingRoomMapper.updateStatus(roomId, roomStatus);
        if (result == 0) {
            throw new BusinessException("更新会议室状态失败");
        }

        // 返回更新后的会议室信息
        return meetingRoomMapper.selectById(roomId);
    }


    /**
     * 根据ID获取会议室详情
     */
    public MeetingRoom getMeetingRoomById(Integer roomId) {
        return meetingRoomMapper.selectById(roomId);
    }

    /**
     * 获取所有可用会议室
     */
    public List<MeetingRoom> getAvailableRooms() {
        return meetingRoomMapper.selectAvailableRooms();
    }

    /**
     * 根据容量筛选会议室
     */
    public List<MeetingRoom> getRoomsByCapacity(Integer minCapacity) {
        return meetingRoomMapper.selectByCapacity(minCapacity);
    }

    /**
     *分页查询会议室
     */
    public List<MeetingRoom> getRoomsByPage(Integer page, Integer size) {
        logger.info("开始分页查询会议室 - 页码: {}, 每页大小: {}", page, size);

        String cacheKey = CACHE_PREFIX + page + ":" + size;

        // 1. 尝试从缓存获取（带异常保护）
        List<MeetingRoom> cachedRooms = null;
        try {
            cachedRooms = (List<MeetingRoom>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRooms != null) {
                if (!cachedRooms.isEmpty()) {
                    logger.info("缓存命中! Key: {}, 返回缓存数据", cacheKey);
                    return cachedRooms;
                } else {
                    logger.warn("缓存命中但数据为空列表，Key: {}", cacheKey);
                }
            } else {
                logger.debug("缓存未命中，Key: {}", cacheKey);
            }
        } catch (Exception e) {
            // Redis 不可用或读取异常时，仅记录日志，继续查数据库
            logger.error("读取缓存异常，Key: {}, 将直接查询数据库", cacheKey, e);
        }

        // 2. 缓存不存在或读取失败，查询数据库
        List<MeetingRoom> rooms;
        try {
            long dbStartTime = System.currentTimeMillis();
            rooms = meetingRoomMapper.selectByPage((page - 1) * size, size);
            long dbEndTime = System.currentTimeMillis();
            logger.info("数据库查询完成，耗时: {}ms, 数据条数: {}", dbEndTime - dbStartTime,
                    rooms != null ? rooms.size() : 0);
        } catch (Exception e) {
            logger.error("数据库查询失败", e);
            throw new RuntimeException("数据库查询失败", e);
        }

        if (rooms == null) {
            return null;
        }

        // 3. 尝试将结果存入缓存（已包含异常保护，不影响返回）
        try {
            redisTemplate.opsForValue().set(cacheKey, rooms, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            logger.info("数据已缓存，Key: {}", cacheKey);
        } catch (Exception e) {
            logger.error("缓存写入失败，Key: {}", cacheKey, e);
        }

        return rooms;
    }

    /**
     * 根据ID删除会议室
     */
    @Transactional
    public void deleteMeetingRoom(Integer roomId) {
        // 先检查会议室是否存在
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(roomId);
        if (meetingRoom == null) {
            throw new BusinessException("会议室不存在，无法删除");
        }

        // 可以在这里添加业务逻辑，如检查会议室是否有预定等
        // 示例：检查会议室是否正在使用中（状态为已预定或使用中）
        if (meetingRoom.getRoomStatus() != null && meetingRoom.getRoomStatus() != 1) {
            throw new BusinessException("会议室当前不可删除，请确保会议室状态为可用");
        }

        int result = meetingRoomMapper.deleteById(roomId);
        if (result == 0) {
            throw new BusinessException("删除会议室失败");
        }
        searchRepository.deleteById(roomId);
    }
}
