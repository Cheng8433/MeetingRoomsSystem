package com.myGroup.service;

import com.myGroup.dto.UpdateSysUserRequest;
import com.myGroup.entity.SysUser;
import com.myGroup.mapper.SysUserMapper;
import com.myGroup.service.ReservationRecordService;
import com.myGroup.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserService.class);
    @Autowired
    private SysUserMapper SysUserMapper;
    @Autowired
    private ReservationRecordService reservationRecordService;

    /**
     * 根据用户ID查询用户信息（用户使用）
     * @param id 用户ID
     * @return 用户信息，如果不存在返回null
     */
    public SysUser getSysUserById(int id) {
        SysUser sysuser = SysUserMapper.selectSysUserById(id);
        if (sysuser == null) {
            throw new BusinessException("用户不存在，ID: " + id);
        }
        return sysuser;
    }

    /**
     * 根据用户名查询用户信息（用户使用）
     * @param name 用户名
     * @return 用户信息，如果不存在返回null
     */
    public SysUser getSysUserByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空", "USER_NAME_EMPTY");
        }
        SysUser sysuser =  SysUserMapper.selectSysUserByName(name);
        if (sysuser == null) {
            throw new BusinessException("用户不存在，Name: " + name);
        }
        return sysuser;
    }

    /**
     * 获取所有用户列表（管理员使用）
     * @return 用户列表，如果没有用户返回空列表
     */
    public List<SysUser> getAllSysUsers() {
        try {
            return SysUserMapper.selectAllSysUsers();
        } catch (Exception e) {
            throw new BusinessException("查询所有用户失败", "USER_QUERY_ALL_ERROR");
        }
    }

    /**
     * 用户注册
     * @param sysuser 用户信息
     * @return 注册成功的用户信息（包含生成的ID）
     */
    public SysUser registerSysUser(SysUser sysuser) {
        if (sysuser == null) {
            throw new BusinessException("用户信息不能为空", "USER_INFO_EMPTY");
        }

        // 基本验证
        validateSysUserForRegistration(sysuser);

        try {
            SysUserMapper.insertSysUser(sysuser);
            return sysuser;
        } catch (Exception e) {
            throw new BusinessException("用户注册失败: " + e.getMessage(), "USER_REGISTER_ERROR");
        }
    }

    /**
     * 更新用户信息
     * @param sysuser 用户信息
     * @return 更新是否成功
     */
    public boolean updateSysUser(SysUser sysuser, UpdateSysUserRequest request) {
        if (request == null) {
            throw new BusinessException("用户更新信息不能为空", "USER_INFO_EMPTY");
        }
        if(sysuser == null){
            throw new BusinessException("找不到该用户");
        }
        try {
            sysuser.setSysUsername(request.getName());
            sysuser.setEmail(request.getEmail());
            sysuser.setDepartment_name(request.getDepartment_name());
            int result = SysUserMapper.updateSysUser(sysuser);
            return result > 0;
        }
        catch (Exception e) {
            throw new BusinessException("用户信息更新失败"+ e.getMessage()+"用户名已被使用", "USER_UPDATE_ERROR");
        }

    }

    /**
     * 根据用户ID删除用户（用户注销或管理员删除）
     * @param id 用户ID
     * @return 删除是否成功
     */
    public boolean deleteSysUserById(String  id) {
        try {
            boolean success = reservationRecordService.deleteRoomReservationRecordByUserId(Integer.parseInt(id));

            int result = SysUserMapper.deleteSysUserById(Integer.parseInt(id));
            return result > 0;
        } catch (Exception e) {
            throw new BusinessException("删除用户失败，用户ID: " + e.getMessage(), "USER_DELETE_ERROR");
        }
    }

    /**
     * 检查用户名是否存在
     * @param name 用户名
     * @return 是否存在
     */
    public boolean isSysUserNameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        SysUser sysuser = SysUserMapper.selectSysUserByName(name);
        return sysuser != null;
    }

    /**
     * 验证用户登录
     * @param name 用户名
     * @param password 密码（假设SysUser实体中有password字段）
     * @return 登录成功返回用户信息，失败返回null
     */
    public SysUser login(String name, String password) {
        if (name == null || password == null) {
            throw new BusinessException("用户名和密码不能为空", "LOGIN_INFO_EMPTY");
        }

        SysUser sysuser = getSysUserByName(name);
        if (sysuser != null && sysuser.getPassword() != null &&
                sysuser.getPassword().equals(password)) {
            log.debug("登录成功");
            return sysuser;
        }
        log.debug("前端登录异常");
        throw new BusinessException("用户名或密码错误", "LOGIN_FAILED");
    }

    /**
     * 批量删除用户（管理员功能）
     * @param ids 用户ID列表
     * @return 成功删除的数量
     */
    public int batchDeleteSysUsers(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空", "USER_IDS_EMPTY");
        }

        int successCount = 0;
        for (String id : ids) {
            try {
                if (deleteSysUserById(id)) {
                    successCount++;
                }
            } catch (BusinessException e) {
                // 记录失败但继续处理其他ID
                System.err.println("删除用户失败，用户ID: " + id + ", 错误: " + e.getMessage());
            }
        }
        return successCount;
    }


    /**
     * 搜索用户（根据名称模糊搜索）
     * @param keyword 关键词
     * @return 匹配的用户列表
     */
    public List<SysUser> searchSysUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException("搜索关键词不能为空", "SEARCH_KEYWORD_EMPTY");
        }

        List<SysUser> allSysUsers = getAllSysUsers();
        return allSysUsers.stream()
                .filter(sysuser -> sysuser.getSysUsername() != null &&
                        sysuser.getSysUsername().toLowerCase().contains(keyword.toLowerCase().trim()))
                .collect(Collectors.toList());
    }

    /**
     * 统计用户数量
     * @return 用户总数
     */
    public int getSysUserCount() {
        return getAllSysUsers().size();
    }

    /**
     * 重置用户密码
     * @param sysuserId 用户ID
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    public boolean resetPassword(int sysuserId, String newPassword){
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException("新密码不能为空", "PASSWORD_EMPTY");
        }

        SysUser sysuser = getSysUserById(sysuserId);
        if (sysuser == null) {
            throw new BusinessException("用户不存在，用户ID: " + sysuserId, "USER_NOT_FOUND");
        }

        sysuser.setPassword(newPassword.trim());
        int result = SysUserMapper.updateSysUser(sysuser);
        return result > 0;
    }

    //用户注册验证
    public void validateSysUserForRegistration(SysUser sysuser) {
        if (sysuser.getSysUsername()== null || sysuser.getSysUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空", "USER_NAME_EMPTY");
        }

        // 检查用户名是否已存在
        if (isSysUserNameExists(sysuser.getSysUsername())) {
            throw new BusinessException("用户名已存在: " + sysuser.getSysUsername(), "USER_NAME_EXISTS");
        }

        // 可以根据需要添加更多验证规则
        // 例如：邮箱格式、手机号格式等
    }
}