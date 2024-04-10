package com.example.Task.dao;

import com.example.Task.model.Contact;
import com.example.Task.model.Message;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageMapper implements RowMapper<String> {

    @Override
    public String mapRow(ResultSet p0, int p1) throws SQLException {
        Message message = new Message();
        message.setName(p0.getString("name"));
        message.setTime(p0.getString("time"));
        message.setMessageBody(p0.getString("message"));
        return message.toString();
    }
}
