package com.example.Task.controller;

import com.example.Task.dao.DAO;
import com.example.Task.handlers.JsonOperator;
import com.example.Task.model.Contact;
import com.example.Task.handlers.TokenGenerator;
import com.example.Task.mailer.Mailer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/")
@Tag(name = "Contacts controller", description = "all methods")
public class TaskController {

    private static int indexStart = 0;
    private static int indexEnd = 0;
    private static int step = 5;
    private static final String SECRET_KEY = "BlaBlaBlaSymbolsToMeetTheRequirementsForSecretKey";
    private DAO dao;
    Mailer mailer;

public TaskController(@Autowired DAO dao, @Autowired Mailer mailer) {
    this.dao = dao;
    this.mailer = mailer;
}

    private String tempCode;

    private List<String> paginate(ArrayList<String> ar, String direction){
        if (direction.equals("down")) {

            if (indexEnd + step >= ar.size()) {
                indexEnd = ar.size();
            } else {
                indexEnd = indexEnd + step;
            }
            indexStart = indexEnd - step;
        } else {
            if (indexStart - step <= 0) {
                indexStart = 0;
            } else {
                indexStart = indexStart - step;
            }
            indexEnd = indexStart + step;
        }

        return ar.stream().filter(it -> (ar.indexOf(it) >= indexStart) && (ar.indexOf(it) < indexEnd)).toList();
    }

    @GetMapping("/chat")
    @Operation(summary = "Get all contacts")
    public List<String> getAllContactsForward(HttpServletResponse response,
                                              @RequestParam("direction") String direction,
                                              @RequestBody String json,
                                              @RequestHeader("Authorization") String token) {
        String name = JsonOperator.getSmthFromReq(json,"name");
        if (dao.userHasAccess(name, token)) {
            return paginate(dao.getMessages(), direction);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new ArrayList<>(List.of(new String[]{"invalid token"}));
        }
    }

    @PostMapping("/")
    @Operation(summary = "Test Connection")
    public void testConnection(HttpServletRequest req,
                             HttpServletResponse response,
                             @RequestBody Contact contact,
                             @RequestHeader("Authorization") String authField) {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "Registration")
    public String signup(HttpServletResponse response,
                         @RequestBody String json) {
        String authCode = JsonOperator.getSmthFromReq(json,"code");
        if (tempCode.equals(authCode)) {
            String name = JsonOperator.getSmthFromReq(json,"name");
            String secretWord = SECRET_KEY + name;
            dao.addContact(name, JsonOperator.getSmthFromReq(json,"password"), secretWord);
            String refreshToken = TokenGenerator.generateRefreshToken(name,secretWord);
            tempCode = "";
            response.setStatus(HttpServletResponse.SC_OK);
            return refreshToken;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Wrong code";
        }
    }

    @GetMapping("/signup")
    @Operation(summary = "Registration request")
    public void signupRequest(@RequestBody String jsonEmail) {
        String email = JsonOperator.getSmthFromReq(jsonEmail,"email");
        tempCode = String.valueOf(new Random().nextInt(99999));
        mailer.sendEmail(email,tempCode);
    }

    @PostMapping("/auth")
    @Operation(summary = "Authorization")
    public String signin(HttpServletResponse response, @RequestBody Contact contact) {
        if (dao.authorize(contact)) {
            String token = TokenGenerator.generateAccessToken(contact.getName(), SECRET_KEY);
            response.setStatus(HttpServletResponse.SC_OK);
            return token;
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "incorrect login/password";
        }
    }

    @PutMapping("/auth")
    @Operation(summary = "Sign out")
    public String signOut(HttpServletResponse response,
                           @RequestBody String json) {
        String name = JsonOperator.getSmthFromReq(json,"name");
        dao.unAuthorize(name);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return "Signed out";
    }

    @PostMapping("/chat")
    @Operation(summary = "Post message and receive all messages")
    public List<String> postMessage(HttpServletResponse response,
                                    @RequestBody String json,
                                    @RequestHeader("Authorization") String token) {
        String name = JsonOperator.getSmthFromReq(json,"name");
        if (dao.userHasAccess(name, token)) {
            dao.postMessage(name, json);
            return paginate(dao.getMessages(),"down");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return List.of("Invalid token");
        }
    }

    @DeleteMapping("/profile")
    @Operation(summary = "delete account")
    public String deleteContact(HttpServletResponse response,
                                @RequestBody String json,
                                @RequestHeader("Authorization") String token) {
        String name = JsonOperator.getSmthFromReq(json,"name");
        String password = JsonOperator.getSmthFromReq(json,"password");
        if (dao.userHasAccess(name, token)) {
            if (dao.getPassword(name).equals(password)) {
                dao.deleteContact(name);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                return "profile deleted";
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "Incorrect password";
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Invalid token";
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Change Contact password")
    public String changePassword(HttpServletResponse response,
                               @RequestBody String jsonMessage,
                               @RequestHeader("Authorization") String token) {
        String name = JsonOperator.getSmthFromReq(jsonMessage, "name");
        if (dao.userHasAccess(name, token)) {
            String oldPass = JsonOperator.getSmthFromReq(jsonMessage, "oldPassword");
            String newPass = JsonOperator.getSmthFromReq(jsonMessage, "newPassword");
            if (dao.changePassword(name, oldPass, newPass)) {
                response.setStatus(HttpServletResponse.SC_OK);
                return "Password is changed";
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "Old password is incorrect";
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Invalid token";
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get contact password. Obviously it's unsafe. " +
            "just left it for a possibility to restore the pass")
    public String getPassword(HttpServletResponse response,
                                 @RequestBody String json,
                                 @RequestHeader("Authorization") String token) {
        String name = JsonOperator.getSmthFromReq(json,"name");
        if (dao.userHasAccess(name, token)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return dao.getPassword(name);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "invalid token";
        }
    }
}
