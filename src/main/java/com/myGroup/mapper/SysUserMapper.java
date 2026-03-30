package com.myGroup.mapper;
import com.myGroup.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper  // 确保有这个注解
public interface SysUserMapper {
    //查询用户信息，用户使用
    SysUser selectSysUserById(int id);

    //通过输入自己姓名查询用户信息，用户使用
    SysUser selectSysUserByName(String name);

    // 查询所有用户的方法，管理员使用
    List<SysUser> selectAllSysUsers();

    //用户可以自己注册
    void insertSysUser(SysUser user);

    //用户可以更新自己信息
    int updateSysUser(SysUser user);

    //通过姓名删除一个用户的信息，用户可以自己注销账号，管理员也可以主动删除
    int deleteSysUserById(int  id);

    int count(); //统计用户数量
}
