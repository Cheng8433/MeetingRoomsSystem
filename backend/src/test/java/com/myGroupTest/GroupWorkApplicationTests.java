package com.myGroupTest;
import org.xidian.group_work.GroupWorkApplication; // 导入主应用类
import com.myGroup.entity.*;
import com.myGroup.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GroupWorkApplication.class)
class GroupWorkApplicationTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    @Autowired
    private SysDictMapper sysDictMapper;

    /**
     * 测试数据库连接是否正常
     */
    @Test
    void contextLoads() {
        // 如果Spring上下文能够正常加载，说明基本配置正确
        assertTrue(true, "Spring上下文加载成功");
    }

    /**
     * 测试sys_user表查询
     */
    @Test
    void testSysUserQuery() {
        System.out.println("=== 测试sys_user表查询 ===");

        try {
            List<SysUser> users = sysUserMapper.selectAllSysUsers();
            assertNotNull(users, "用户列表不应为null");

            if (!users.isEmpty()) {
                SysUser user = users.get(0);
                assertNotNull(user.getId(), "用户ID不应为null");
                assertNotNull(user.getSysUsername(), "用户名不应为null");
                System.out.println("查询到用户: " + user.getSysUsername() + " (ID: " + user.getId() + ")");
            } else {
                System.out.println("sys_user表中暂无数据");
            }

            System.out.println("sys_user表查询测试通过 ✓");
        } catch (Exception e) {
            fail("sys_user表查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试meeting_room表查询
     */
    @Test
    void testMeetingRoomQuery() {
        System.out.println("=== 测试meeting_room表查询 ===");

        try {
            List<MeetingRoom> rooms = meetingRoomMapper.selectAll();
            assertNotNull(rooms, "会议室列表不应为null");

            if (!rooms.isEmpty()) {
                MeetingRoom room = rooms.get(0);
                assertNotNull(room.getRoomId(), "会议室ID不应为null");
                assertNotNull(room.getRoomName(), "会议室名称不应为null");
                System.out.println("查询到会议室: " + room.getRoomName() + " (ID: " + room.getRoomId() + ")");
            } else {
                System.out.println("meeting_room表中暂无数据");
            }

            System.out.println("meeting_room表查询测试通过 ✓");
        } catch (Exception e) {
            fail("meeting_room表查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试reservation_record表查询
     */
    @Test
    void testReservationRecordQuery() {
        System.out.println("=== 测试reservation_record表查询 ===");

        try {
            List<ReservationRecord> records = reservationRecordMapper.selectAllReservationRecords();
            assertNotNull(records, "预约记录列表不应为null");

            if (!records.isEmpty()) {
                ReservationRecord record = records.get(0);
                assertNotNull(record.getReservationRecord_id(), "预约记录ID不应为null");
                assertNotNull(record.getPurpose(), "会议主题不应为null");
                System.out.println("查询到预约记录: " + record.getPurpose() + " (ID: " + record.getReservationRecord_id() + ")");
            } else {
                System.out.println("reservation_record表中暂无数据");
            }

            System.out.println("reservation_record表查询测试通过 ✓");
        } catch (Exception e) {
            fail("reservation_record表查询失败: " + e.getMessage());
        }
    }



    /**
     * 测试sys_oper_log表查询
     */
    @Test
    void testSysOperLogQuery() {
        System.out.println("=== 测试sys_oper_log表查询 ===");

        try {
            List<SysOperLog> logs = sysOperLogMapper.selectAll();
            assertNotNull(logs, "操作日志列表不应为null");

            if (!logs.isEmpty()) {
                SysOperLog log = logs.get(0);
                assertNotNull(log.getLogId(), "日志ID不应为null");
                assertNotNull(log.getOperContent(), "操作内容不应为null");
                System.out.println("查询到操作日志: " + log.getOperContent() + " (ID: " + log.getLogId() + ")");
            } else {
                System.out.println("sys_oper_log表中暂无数据");
            }

            System.out.println("sys_oper_log表查询测试通过 ✓");
        } catch (Exception e) {
            fail("sys_oper_log表查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试sys_dict表查询
     */
    @Test
    void testSysDictQuery() {
        System.out.println("=== 测试sys_dict表查询 ===");

        try {
            List<SysDict> dicts = sysDictMapper.selectAll();
            assertNotNull(dicts, "数据字典列表不应为null");

            if (!dicts.isEmpty()) {
                SysDict dict = dicts.get(0);
                assertNotNull(dict.getDictId(), "字典ID不应为null");
                assertNotNull(dict.getDictType(), "字典类型不应为null");
                assertNotNull(dict.getDictName(), "字典名称不应为null");
                System.out.println("查询到数据字典: " + dict.getDictName() + " (类型: " + dict.getDictType() + ")");
            } else {
                System.out.println("sys_dict表中暂无数据");
            }

            System.out.println("sys_dict表查询测试通过 ✓");
        } catch (Exception e) {
            fail("sys_dict表查询失败: " + e.getMessage());
        }
    }

    /**
     * 综合测试 - 测试所有表的查询
     */
    @Test
    void testAllTablesQuery() {
        System.out.println("=== 开始综合测试所有表查询 ===");

        // 依次执行各个表的测试
        testSysUserQuery();
        testMeetingRoomQuery();
        testReservationRecordQuery();
        testSysOperLogQuery();
        testSysDictQuery();

        System.out.println("=== 所有表查询测试完成 ===");
    }

    /**
     * 测试数据统计
     */
    @Test
    void testDataStatistics() {
        System.out.println("=== 数据统计测试 ===");

        try {
            int userCount = sysUserMapper.count();
            int roomCount = meetingRoomMapper.count();
            int reservationCount = reservationRecordMapper.count();
            int logCount = sysOperLogMapper.count();
            int dictCount = sysDictMapper.count();

            System.out.println("数据统计结果:");
            System.out.println("用户数量: " + userCount);
            System.out.println("会议室数量: " + roomCount);
            System.out.println("预约记录数量: " + reservationCount);
            System.out.println("操作日志数量: " + logCount);
            System.out.println("数据字典数量: " + dictCount);

            // 验证统计方法正常工作
            assertTrue(userCount >= 0, "用户数量统计异常");
            assertTrue(roomCount >= 0, "会议室数量统计异常");
            assertTrue(reservationCount >= 0, "预约记录数量统计异常");
            assertTrue(logCount >= 0, "操作日志数量统计异常");
            assertTrue(dictCount >= 0, "数据字典数量统计异常");

            System.out.println("数据统计测试通过 ✓");
        } catch (Exception e) {
            fail("数据统计测试失败: " + e.getMessage());
        }
    }
}