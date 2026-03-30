package com.myGroup.elasticsearch.util;

import com.myGroup.entity.MeetingRoom;
import com.myGroup.elasticsearch.document.MeetingRoomDoc;

public class MeetingRoomConverter {

    public static MeetingRoomDoc toDocument(MeetingRoom room) {
        if (room == null) return null;
        MeetingRoomDoc doc = new MeetingRoomDoc();
        doc.setRoomId(room.getRoomId());
        doc.setRoomName(room.getRoomName());
        doc.setRoomNo(room.getRoomNo());
        doc.setCapacity(room.getCapacity());
        doc.setArea(room.getArea());
        doc.setDescription(room.getDescription());
        doc.setRoomStatus(room.getRoomStatus());
        doc.setPhotoUrl(room.getPhotoUrl());
        doc.setCreateTime(room.getCreateTime());
        doc.setUpdateTime(room.getUpdateTime());
        doc.setCreateUserId(room.getCreateUserId());
        return doc;
    }

    public static MeetingRoom toEntity(MeetingRoomDoc doc) {
        if (doc == null) return null;
        MeetingRoom room = new MeetingRoom();
        room.setRoomId(doc.getRoomId());
        room.setRoomName(doc.getRoomName());
        room.setRoomNo(doc.getRoomNo());
        room.setCapacity(doc.getCapacity());
        room.setArea(doc.getArea());
        room.setDescription(doc.getDescription());
        room.setRoomStatus(doc.getRoomStatus());
        room.setPhotoUrl(doc.getPhotoUrl());
        room.setCreateTime(doc.getCreateTime());
        room.setUpdateTime(doc.getUpdateTime());
        room.setCreateUserId(doc.getCreateUserId());
        return room;
    }
}