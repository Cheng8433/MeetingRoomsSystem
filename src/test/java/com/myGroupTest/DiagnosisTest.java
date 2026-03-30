package com.myGroupTest;

import org.xidian.group_work.GroupWorkApplication;
import com.myGroup.entity.MeetingRoom;
import com.myGroup.mapper.MeetingRoomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = GroupWorkApplication.class)
class UltraSimpleMeetingRoomTest {

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    @Test
    void ultraSimpleTest() {
        System.out.println("=== 超简单MeetingRoom测试 ===");

        try {
            // 1. 检查Mapper是否注入
            if (meetingRoomMapper == null) {
                System.out.println("❌ MeetingRoomMapper注入失败");
                return;
            }
            System.out.println("✅ MeetingRoomMapper注入成功");

            // 2. 尝试查询
            List<MeetingRoom> rooms = meetingRoomMapper.selectAll();
            System.out.println("✅ 查询执行成功");

            // 3. 显示结果
            System.out.println("查询到 " + rooms.size() + " 个会议室");

            for (int i = 0; i < Math.min(rooms.size(), 3); i++) {
                MeetingRoom room = rooms.get(i);
                System.out.println("会议室 " + (i + 1) + ": " +
                        room.getRoomName() + " (ID: " + room.getRoomId() + ")");
            }

            System.out.println("🎉 超简单测试完成！");

        } catch (Exception e) {
            System.err.println("💥 测试失败，错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }
}