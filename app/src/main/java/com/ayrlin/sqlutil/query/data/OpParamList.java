package com.ayrlin.sqlutil.query.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
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
        int targetSize = this.size() - 1;
        if(targetSize >= 0) {
            if (cnjs.size() < targetSize) {
                int additionalElements = targetSize - cnjs.size();
                List<Cnj> defaultValues = Collections.nCopies(additionalElements, defaultCnj);
                cnjs.addAll(defaultValues);
            }
            cnjs.subList(targetSize, cnjs.size()).clear();
        }

        this.addAll((ArrayList<OpParam>) other);
        this.cnjs.addAll(other.cnjs);
    }

    public OpParamList setDefaultCnj(Cnj c) {
        this.defaultCnj = c;
        return this;
    }

    public OpParamList addCnj(Cnj c) {
        this.cnjs.add(c);
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
