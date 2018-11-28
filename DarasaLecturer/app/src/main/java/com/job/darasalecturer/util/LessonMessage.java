package com.job.darasalecturer.util;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;
import com.job.darasalecturer.model.QRParser;
import com.job.darasalecturer.model.StudentMessage;

import java.nio.charset.Charset;

/**
 * Created by Job on Saturday : 11/24/2018.
 *
 * Used to prepare the payload for a
 * {@link com.google.android.gms.nearby.messages.Message Nearby Message}. Adds a unique id
 * to the Message payload, which helps Nearby distinguish between multiple devices with
 * the same model name.
 */

public class LessonMessage {
    private static final Gson gson = new Gson();

    private final String mUUID;
    private final String lecFirstName;
    private final String lecSecondName;
    private final QRParser qrParser;
    private final StudentMessage studentMessage;

    /**
     * Builds a new {@link Message} object using a unique identifier.
     * static : prevents creation of new instances over the network
     */
    public static Message newNearbyMessage(String instanceId,String lecFirstName,
                                           String lecSecondName,QRParser qrParser,StudentMessage studentMessage) {
        LessonMessage deviceMessage = new LessonMessage(instanceId,lecFirstName, lecSecondName, qrParser,studentMessage);
        return new Message(gson.toJson(deviceMessage).getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Creates a {@code LessonMessage} object from the string used to construct the payload to a
     * {@code Nearby} {@code Message}.
     */
    public static LessonMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                LessonMessage.class);
    }

    private LessonMessage(String uuid,String lecFirstName,String lecSecondName,QRParser qrParser,StudentMessage studentMessage){
        this.mUUID = uuid;
        this.lecFirstName = lecFirstName;
        this.lecSecondName = lecSecondName;
        this.qrParser = qrParser;
        this.studentMessage = studentMessage;
    }

    public StudentMessage getStudentMessage() {
        return studentMessage;
    }

    public QRParser getQrParser() {
        return qrParser;
    }
}
