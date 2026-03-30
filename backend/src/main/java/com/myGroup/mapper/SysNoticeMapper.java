package com.myGroup.mapper;

import com.myGroup.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNoticeMapper {

    /**
     * 插入系统通知
     */
    int insert(SysNotice sysNotice);

    /**
     * 批量插入系统通知
     */
    int insertBatch(List<SysNotice> sysNotices);

    /**
     * 根据ID删除通知
     */
    int deleteById(Integer noticeId);

    /**
     * 根据用户ID删除通知
     */
    int deleteByUserId(Integer receiveUserId);

    /**
     * 更新通知信息
     */
    int update(SysNotice sysNotice);

    /**
     * 根据ID查询通知
     */
    SysNotice selectById(Integer noticeId);

    /**
     * 根据用户ID查询通知
     */
    List<SysNotice> selectByUserId(Integer receiveUserId);

    /**
     * 查询用户未读通知
     */
    List<SysNotice> selectUnreadByUserId(Integer receiveUserId);

    /**
     * 查询用户已读通知
     */
    List<SysNotice> selectReadByUserId(Integer receiveUserId);

    /**
     * 根据通知类型查询
     */
    List<SysNotice> selectByType(@Param("receiveUserId") Integer receiveUserId,
                                 @Param("noticeType") Integer noticeType);

    /**
     * 根据关联ID查询通知
     */
    List<SysNotice> selectByRelatedId(@Param("relatedId") Integer relatedId,
                                      @Param("noticeType") Integer noticeType);

    /**
     * 标记通知为已读
     */
    int markAsRead(Integer noticeId);

    /**
     * 批量标记通知为已读
     */
    int markMultipleAsRead(List<Integer> noticeIds);

    /**
     * 标记用户所有通知为已读
     */
    int markAllAsRead(Integer receiveUserId);

    /**
     * 分页查询用户通知
     */
    List<SysNotice> selectByPage(@Param("receiveUserId") Integer receiveUserId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    /**
     * 统计用户通知数量
     */
    int countByUserId(Integer receiveUserId);

    /**
     * 统计用户未读通知数量
     */
    int countUnreadByUserId(Integer receiveUserId);

    /**
     * 根据条件查询通知
     */
    List<SysNotice> selectByConditions(@Param("receiveUserId") Integer receiveUserId,
                                       @Param("noticeType") Integer noticeType,
                                       @Param("readStatus") Integer readStatus);

    /**
     * 删除过期通知
     */
    int deleteExpiredNotices(@Param("days") Integer days);

    /**
     * 获取最新通知
     */
    List<SysNotice> selectLatestNotices(@Param("receiveUserId") Integer receiveUserId,
                                        @Param("limit") Integer limit);
}
