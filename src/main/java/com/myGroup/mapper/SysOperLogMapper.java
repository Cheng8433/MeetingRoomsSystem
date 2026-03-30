package com.myGroup.mapper;

import com.myGroup.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysOperLogMapper {

    /**
     * 插入操作日志
     */
    int insert(SysOperLog sysOperLog);

    /**
     * 根据ID删除操作日志
     */
    int deleteById(Integer logId);

    /**
     * 批量删除操作日志
     */
    int deleteByIds(List<Integer> logIds);

    /**
     * 清空操作日志
     */
    int deleteAll();

    /**
     * 根据ID查询操作日志
     */
    SysOperLog selectById(Integer logId);

    /**
     * 查询所有操作日志
     */
    List<SysOperLog> selectAll();

    /**
     * 根据用户ID查询操作日志
     */
    List<SysOperLog> selectByUserId(Integer operUserId);

    /**
     * 根据用户名查询操作日志
     */
    List<SysOperLog> selectByUserName(String operUserName);

    /**
     * 根据操作模块查询操作日志
     */
    List<SysOperLog> selectByModule(String operModule);

    /**
     * 根据操作类型查询操作日志
     */
    List<SysOperLog> selectByType(String operType);

    /**
     * 根据IP地址查询操作日志
     */
    List<SysOperLog> selectByIp(String operIp);

    /**
     * 根据时间范围查询操作日志
     */
    List<SysOperLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询操作日志
     */
    List<SysOperLog> selectByPage(@Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    /**
     * 根据条件查询操作日志
     */
    List<SysOperLog> selectByConditions(@Param("operUserName") String operUserName,
                                        @Param("operModule") String operModule,
                                        @Param("operType") String operType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作日志数量
     */
    int count();

    /**
     * 统计条件查询的操作日志数量
     */
    int countByConditions(@Param("operUserName") String operUserName,
                          @Param("operModule") String operModule,
                          @Param("operType") String operType,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期操作日志
     */
    int deleteExpiredLogs(@Param("days") Integer days);

    /**
     * 获取操作模块列表（去重）
     */
    List<String> selectDistinctModules();

    /**
     * 获取操作类型列表（去重）
     */
    List<String> selectDistinctTypes();

    /**
     * 获取最近操作日志
     */
    List<SysOperLog> selectLatestLogs(@Param("limit") Integer limit);
}