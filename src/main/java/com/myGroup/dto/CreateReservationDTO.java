package com.myGroup.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.myGroup.entity.ReservationRecord;
import com.myGroup.exception.BusinessException;
import lombok.Data;
import javax.validation.constraints.*;

import static com.myGroup.entity.ReservationRecordStatus.CONFIRMED;
import static com.myGroup.entity.ReservationRecordStatus.PENDING;
import static java.sql.Types.NULL;

@Data
public class CreateReservationDTO {

    @NotNull(message = "会议室ID不能为空")
    private Integer room_id;

    @NotNull(message = "用户ID不能为空")
    private Integer user_id;

    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "\\d{1,2}:\\d{2}:\\d{2}", message = "开始时间格式必须为 HH:mm:ss")
    private String start_time;  // 字符串格式： "03:00:00"

    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "\\d{1,2}:\\d{2}:\\d{2}", message = "结束时间格式必须为 HH:mm:ss")
    private String end_time;    // 字符串格式： "04:00:00"

    @NotBlank(message = "会议主题不能为空")
    @Size(max = 100, message = "会议主题不能超过100个字符")
    private String purpose;

    @NotBlank(message = "预约日期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd")
    private String reserveDate; // 字符串格式： "2025-12-23"

    @NotNull(message = "预计人数不能为空")
    @Min(value = 1, message = "预计人数至少为1人")
    @Max(value = 100, message = "预计人数不能超过100人")
    private Integer expectedNum;

    // 转换为实体类的方法
    public ReservationRecord toReservationRecord() {
        ReservationRecord record = new ReservationRecord();
        record.setRoom_id(this.room_id);
        record.setUser_id(this.user_id);

        // 转换时间字符串为 LocalTime
        try {
            record.setStart_time(java.time.LocalTime.parse(this.start_time));
            record.setEnd_time(java.time.LocalTime.parse(this.end_time));
        } catch (Exception e) {
            throw new BusinessException("时间格式错误，必须是HH:mm:ss格式");
        }

        // 转换日期字符串为 LocalDate
        try {
            record.setReservedDate(java.time.LocalDate.parse(this.reserveDate));
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误，必须是yyyy-MM-dd格式");
        }

        record.setPurpose(this.purpose);
        record.setExpectedNum(this.expectedNum);

        // 设置默认值
        record.setReservationRecord_status(CONFIRMED);
        record.setApproverId(1);
        record.setApprovalTime(null);
        record.setUseConfirm(0);
        record.setRejected_reason(null);

        return record;
    }
}
