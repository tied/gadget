package com.avrethem.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class dataGeneratorModel {

    @XmlElement(name = "value")
    private String message;


    public dataGeneratorModel() {
    }

    public dataGeneratorModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}