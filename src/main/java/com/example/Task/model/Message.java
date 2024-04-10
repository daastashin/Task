package com.example.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Schema(description = "Message itself")
public class Message {

    @Schema(description = "Message")
    @NotBlank
    @Size(min = 1, max = 20)
    private String messageBody;

    @Schema(description = "Author")
    private String name;

    @Schema(description = "Author")
    private String time;

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString(){
        return "[" + time + "]-" + name + ": " + messageBody;
    }
}