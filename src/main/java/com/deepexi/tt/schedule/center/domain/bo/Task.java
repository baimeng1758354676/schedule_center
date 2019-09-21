package com.deepexi.tt.schedule.center.domain.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 白猛
 */
public class Task implements Serializable {
    private Integer id;


    private String method;

    private Date createTime;

    private String data;

    private String url;

    private Integer status;

    private Date executeTime;

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", createTime=" + createTime +
                ", data='" + data + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status +
                ", executeTime=" + executeTime +
                '}';
    }
}
