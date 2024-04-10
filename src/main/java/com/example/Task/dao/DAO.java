package com.example.Task.dao;

import com.example.Task.handlers.JsonOperator;
import com.example.Task.model.Contact;
import com.example.Task.handlers.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dao")
public class DAO {

    private final JdbcTemplate jdbcTemplate;
    public DAO(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void addContact(String name, String password, String secretWord) {
        StringBuffer sf = new StringBuffer();
        sf.append("INSERT INTO public.\"contacts\" (name,  password, secret_word, authorized) VALUES ('")
                .append(name + "','")
                .append(password + "','")
                .append(secretWord + "',")
                .append("false);");
        jdbcTemplate.update(sf.toString());
    }

    public boolean authorize(Contact requestContact) {
        Contact existingContact = jdbcTemplate.query("SELECT * FROM public.\"contacts\" WHERE name=?",
                new Object[]{requestContact.getName()},
                new ContactMapper()).stream().findAny().orElse(null);
        if (requestContact.getPassword().equals(existingContact.getPassword())) {
            jdbcTemplate.update("UPDATE public.\"contacts\" SET authorized=? WHERE name=?",
                    true, existingContact.getName());
            return true;
        } else
            return false;
    }

    public boolean userHasAccess(String name, String token) {
        Contact contact = jdbcTemplate.query("SELECT * FROM public.\"contacts\" WHERE name=?",
                new Object[]{name},
                new ContactMapper()).stream().findAny().orElse(null);
        if (contact != null &&
                contact.getAuthorized() &&
                (TokenGenerator.validateToken(token, contact.getSecretWord()))) {
            return true;
        } else {
            unAuthorize(name);
            return false;
        }
    }

    public void postMessage(String name, String jsonMessage) {
        String message = JsonOperator.getSmthFromReq(jsonMessage, "message");
        String time = String.valueOf(java.time.LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO public.\"messages\" (name, message, time) VALUES ('"
                + name + "','" + message + "',' " + time + "');");
    }

    public ArrayList<String> getMessages() {
        return new ArrayList<String>(jdbcTemplate.query("SELECT * FROM public.\"messages\"",new MessageMapper()));
    }

    public void deleteContact(String name) {
        jdbcTemplate.update("DELETE FROM public.\"messages\" WHERE name=?", new Object[]{name});
        jdbcTemplate.update("DELETE FROM public.\"contacts\" WHERE name=?", new Object[]{name});
    }

    public boolean changePassword(String name, String oldEnteredPass, String newPass) {
        String existingPassword = jdbcTemplate.query("SELECT * FROM public.\"contacts\" WHERE name=?",
                new Object[]{name},
                new ContactMapper()).stream().findAny().orElse(null).getPassword();
        if (existingPassword.equals(oldEnteredPass)) {
            jdbcTemplate.update("UPDATE public.\"contacts\" SET password=? WHERE name=?",
                    newPass, name);
            return true;
        } else
            return false;
    }

    public String getPassword(String name) {
        return jdbcTemplate.query("SELECT * FROM public.\"contacts\" WHERE name=?",
                new Object[]{name},
                new ContactMapper()).stream().findAny().orElse(null).getPassword();
    }

    public void unAuthorize(String name) {
        jdbcTemplate.update("UPDATE public.\"contacts\" SET authorized=? WHERE name=?",
                false, name);
    }
}
