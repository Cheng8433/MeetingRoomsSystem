package com.myGroup.entity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.myGroup.entity.ReservationRecordStatus.PENDING;
import static java.sql.Types.NULL;

public class ReservationRecord {
    private int reservation_id;
    private int room_id;
    private int user_id;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime start_time;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime end_time;
    private String purpose;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reserveDate;

    private int expectedNum;
    private int approverId;
    private LocalDateTime approvalTime;
    private int useConfirm;
    private ReservationRecordStatus reservation_status;
    private  LocalDateTime created_time;
    private  LocalDateTime updated_time;
    private String rejected_reason;

    public int getReservationRecord_id() {return reservation_id;}
    public void setReservationRecord_id(int reservation_id) {this.reservation_id = reservation_id;}

    public int getRoom_id() {return room_id;}
    public void setRoom_id(int room_id) {this.room_id = room_id;}

    public int getUser_id() {return user_id;}
    public void setUser_id(int user_id) {this.user_id = user_id;}

    public LocalTime getStart_time() {return start_time;}
    public void setStart_time(LocalTime start_time) {this.start_time = start_time;}


    public LocalTime getEnd_time() {return end_time;}
    public void setEnd_time(LocalTime end_time) {this.end_time = end_time;}

    public LocalDate getReservedDate() {return reserveDate;}
    public void setReservedDate(LocalDate reserveDate) {this.reserveDate = reserveDate;}

    public String getPurpose() {return purpose;}
    public void setPurpose(String purpose) {this.purpose = purpose;}

    public ReservationRecordStatus getReservationRecord_status() {return reservation_status;}
    public void setReservationRecord_status(ReservationRecordStatus reservation_status) {
        this.reservation_status = reservation_status;
        this.updated_time = LocalDateTime.now();
    }

    public LocalDateTime getCreated_time() {return created_time;}
    public void setCreated_time(LocalDateTime created_time) {this.created_time = created_time;}

    public LocalDateTime getUpdated_time() {return updated_time;}
    public void setUpdated_time(LocalDateTime updated_time) {this.updated_time = updated_time;}

    public String getRejected_reason() {return rejected_reason;}
    public void setRejected_reason(String rejected_reason) { this.rejected_reason = rejected_reason; }

    // 无参构造方法
    public ReservationRecord() {
        this.reservation_status = PENDING;
        this.created_time = LocalDateTime.now();
        this.updated_time = LocalDateTime.now();
    }

    //有参构造方法
    public ReservationRecord(ReservationRecord reservationRecord) {
        this.reservation_id = reservationRecord.reservation_id;
        this.room_id = reservationRecord.room_id;
        this.user_id = reservationRecord.user_id;
        this.start_time = reservationRecord.start_time;
        this.end_time = reservationRecord.end_time;
        this.expectedNum = reservationRecord.expectedNum;
        this.reserveDate = reservationRecord.reserveDate;
        this.purpose = reservationRecord.purpose;
        this.rejected_reason = null;
        this.created_time = LocalDateTime.now();
        this.updated_time = LocalDateTime.now();
        this.approverId = NULL;
        this.approvalTime = null;
        this.useConfirm = 0;
        this.reservation_status = PENDING;
    }

    public int getExpectedNum() {return expectedNum;}
    public void setExpectedNum(int expectedNum) {this.expectedNum = expectedNum;}

    public int getApproverId() {return approverId;}
    public void setApproverId(int approverId) {this.approverId = approverId;}

    public LocalDateTime getApprovalTime() {return approvalTime;}
    public void setApprovalTime(LocalDateTime approvalTime) {this.approvalTime = approvalTime;}

    public int getUseConfirm() {return useConfirm;}
    public void setUseConfirm(int useConfirm) {this.useConfirm = useConfirm;}


    @Override
    public String toString() {
        return "ReservationRecord{" +
                "reservation_id=" + reservation_id +
                ", room_id=" + room_id +
                ", user_id=" + user_id +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", purpose='" + purpose + '\'' +
                ", reserveDate=" + reserveDate +
                ", expectedNum=" + expectedNum +
                ", approverId=" + approverId +
                ", approvalTime=" + approvalTime +
                ", useConfirm=" + useConfirm +
                ", reservation_status=" + reservation_status +
                ", created_time=" + created_time +
                ", updated_time=" + updated_time +
                ", rejected_reason='" + rejected_reason + '\'' +
                '}';
    }


}
