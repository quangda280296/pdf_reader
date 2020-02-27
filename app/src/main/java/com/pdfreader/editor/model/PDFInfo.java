package com.pdfreader.editor.model;

import java.io.Serializable;


public class PDFInfo implements Serializable {

    public long id;
    public String fileName;
    public String filePath;
    public String mimetype;
    public int bgthumnails;

    public PDFInfo() {
    }
//
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public int getBgthumnails() {
        return bgthumnails;
    }

    public void setBgthumnails(int bgthumnails) {
        this.bgthumnails = bgthumnails;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
