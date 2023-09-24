package com.ayrlin.tasukaru.data;

import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@ToString
public class ViewerInfo {
    public long id;
    public String clId;
    public String clName;
    public String fallbackName;
    public long watchtime;
    public long points;
    public List<Long> accountIds;

    public ViewerInfo() {
        id = -1;
        clId = "";
        clName = "";
        fallbackName = "";
        watchtime = -1;
        points = -1;
        accountIds = new ArrayList<>();
    }

    public ViewerInfo id(long id) {
        this.id = id;
        return this;
    }

    public ViewerInfo clId(String clId) {
        this.clId = clId;
        return this;
    }

    public ViewerInfo clName(String clName) {
        this.clName = clName;
        return this;
    }

    public ViewerInfo fallbackName(String fallbackName) {
        this.fallbackName = fallbackName;
        return this;
    }

    public ViewerInfo watchtime(long watchtime) {
        this.watchtime = watchtime;
        return this;
    }

    public ViewerInfo points(long points) {
        this.points = points;
        return this;
    }

    public ViewerInfo accountIds(List<Long> accountIds) {
        this.accountIds = accountIds;
        return this;
    }

    public String getName() {
        return (clName.isEmpty()? fallbackName : clName);
    }
}
