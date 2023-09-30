package com.ayrlin.sqlutil.query.data;

import java.util.ArrayList;

import lombok.Getter;

public class OpParamList extends ArrayList<OpParam> {
    private static final Cnj DEFAULT_CNJ = Cnj.AND;

    private @Getter ArrayList<Cnj> cnjs;
    private @Getter Cnj defaultCnj;

    public OpParamList() {
        super();
        cnjs = new ArrayList<>();
        defaultCnj = DEFAULT_CNJ;
    }

    public enum Cnj {
        AND,
        OR    
    }

    public void addAll(OpParamList other) {
        this.addAll((ArrayList<OpParam>) other);
        this.cnjs.addAll(other.cnjs);
    }

    public OpParamList setDefaultCnj(Cnj c) {
        this.defaultCnj = c;
        return this;
    }

    public String getSQL() {
        return getSQL(true);
    }
    public String getSQL(boolean usingWildcard) {
        String str = "";
        int i = 0;
        for(;i < size()-1; i++) {
            str += paramSQL(i);
            Cnj nextCnj; 
            if(cnjs.size() <= i) { 
                nextCnj = defaultCnj;
            } else { 
                nextCnj = cnjs.get(i);
                if(nextCnj == null) 
                    nextCnj = defaultCnj;
            }
            str += " " + nextCnj.toString() + " ";
        }
        str += paramSQL(i);
        return str;
    }

    private String paramSQL(int index) {
        return paramSQL(index, true);
    }
    private String paramSQL(int index, boolean usingWildcard) {
        OpParam p = get(index);
        return "\"" + p.getColumn() + "\" " + p.operation.toString() + " " + (usingWildcard? "?" : "'" + p.value.toString() + "'");
    }
}
