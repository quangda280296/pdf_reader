package com.pdfreader.editor.model;

import java.io.Serializable;

public class Reading implements Serializable {

    public long id;
    public int page;
    public String path;

    public Reading() {
    }
    public Reading(long id, int page, String path) {
        this.id = id;
        this.page = page;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
