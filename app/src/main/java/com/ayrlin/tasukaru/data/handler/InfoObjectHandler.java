package com.ayrlin.tasukaru.data.handler;

import java.util.ArrayList;
import java.util.List;

import com.ayrlin.tasukaru.data.InfoObject;

public abstract class InfoObjectHandler<T extends InfoObject<T>> {

    /**
     * adds infoObject to Viewerbase via SQL
     * @param infoObject
     * @return key id of new infoObject in the relevent table.
     */
    public abstract long addToVB(T infoObject);

    public abstract T getFromVB(long id);

    public abstract boolean updateToVB(T infoObject);

    public List<T> getFromVB(List<Long> ids) {
        List<T> ts = new ArrayList<>();
        for(long id : ids) {
            ts.add(this.getFromVB(id));
        }
        return ts;
    }
}
