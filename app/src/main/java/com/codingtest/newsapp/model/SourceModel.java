package com.codingtest.newsapp.model;

import java.io.Serializable;

public class SourceModel implements Serializable {

    String id ="";
    String name="";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}