package com.example.Task.dao;

import com.example.Task.model.Contact;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactMapper implements RowMapper<Contact> {

    @Override
    public Contact mapRow(ResultSet p0, int p1) throws SQLException {
        Contact contact = new Contact();
        contact.setName(p0.getString("name"));
        contact.setSecretWord(p0.getString("secret_word"));
        contact.setPassword(p0.getString("password"));
        contact.setAuthorized(p0.getBoolean("authorized"));
        return contact;
    }
}
