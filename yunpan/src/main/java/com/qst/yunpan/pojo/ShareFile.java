package com.qst.yunpan.pojo;

//16-2-1 创建ShareFile实体类
public class ShareFile extends FileCustom {
    private String shareUser;
    private String url;
    //省略get/set方法

    public String getShareUser() {
        return shareUser;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
