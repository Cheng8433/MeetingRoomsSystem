package com.myGroup.mapper;

import com.myGroup.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictMapper {

    /**
     * 插入数据字典
     */
    int insert(SysDict sysDict);

    /**
     * 根据ID删除数据字典
     */
    int deleteById(Integer dictId);

    /**
     * 根据字典类型和字典码删除
     */
    int deleteByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 更新数据字典
     */
    int update(SysDict sysDict);

    /**
     * 根据ID查询数据字典
     */
    SysDict selectById(Integer dictId);

    /**
     * 根据字典类型和字典码查询
     */
    SysDict selectByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 查询所有数据字典
     */
    List<SysDict> selectAll();

    /**
     * 根据字典类型查询数据字典
     */
    List<SysDict> selectByType(String dictType);

    /**
     * 根据字典类型查询有效的数据字典（状态为1）
     */
    List<SysDict> selectValidByType(String dictType);

    /**
     * 根据字典码模糊查询
     */
    List<SysDict> selectByCode(String dictCode);

    /**
     * 根据字典名称模糊查询
     */
    List<SysDict> selectByName(String dictName);

    /**
     * 根据状态查询数据字典
     */
    List<SysDict> selectByStatus(Integer status);

    /**
     * 检查字典类型和字典码是否存在
     */
    boolean existsByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 检查字典类型和字典码是否存在（排除指定ID）
     */
    boolean existsByTypeAndCodeExcludeId(@Param("dictType") String dictType,
                                         @Param("dictCode") String dictCode,
                                         @Param("dictId") Integer dictId);

    /**
     * 更新数据字典状态
     */
    int updateStatus(@Param("dictId") Integer dictId, @Param("status") Integer status);

    /**
     * 更新排序
     */
    int updateSort(@Param("dictId") Integer dictId, @Param("sort") Integer sort);

    /**
     * 分页查询数据字典
     */
    List<SysDict> selectByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据条件查询数据字典
     */
    List<SysDict> selectByConditions(@Param("dictType") String dictType,
                                     @Param("dictCode") String dictCode,
                                     @Param("dictName") String dictName,
                                     @Param("status") Integer status);

    /**
     * 统计数据字典数量
     */
    int count();

    /**
     * 统计条件查询的数据字典数量
     */
    int countByConditions(@Param("dictType") String dictType,
                          @Param("dictCode") String dictCode,
                          @Param("dictName") String dictName,
                          @Param("status") Integer status);

    /**
     * 获取所有字典类型（去重）
     */
    List<String> selectDistinctTypes();

    /**
     * 根据多个字典类型查询
     */
    List<SysDict> selectByTypes(@Param("types") List<String> types);

    /**
     * 批量插入数据字典
     */
    int insertBatch(List<SysDict> sysDicts);
}
