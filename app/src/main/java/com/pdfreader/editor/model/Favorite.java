package com.pdfreader.editor.model;

import java.io.Serializable;

public class Favorite implements Serializable {

    public long id;
    public String pathFile;
    public String nameFile;

    public Favorite() {
    }

    public Favorite(long id, String pathFile, String nameFile) {
        this.id = id;
        this.pathFile = pathFile;
        this.nameFile = nameFile;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public long getId() {
        return id;
    }
}
