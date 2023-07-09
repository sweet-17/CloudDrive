package com.qst.yunpan.pojo;

import java.util.List;

public class SummaryFile {
    private boolean isFile;//是不是一个文件
    private String path;//文件路径
    private String fileName;//文件名称
    private int listdiretory;//目录
    private List<SummaryFile> listFile;//文件

    public boolean getisFile() {
        return isFile;
    }

    public void setisFile(boolean file) {
        isFile = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getListdiretory() {
        return listdiretory;
    }

    public void setListdiretory(int listdiretory) {
        this.listdiretory = listdiretory;
    }

    public List<SummaryFile> getListFile() {
        return listFile;
    }

    public void setListFile(List<SummaryFile> listFile) {
        this.listFile = listFile;
    }

    //省略get/set方法
}