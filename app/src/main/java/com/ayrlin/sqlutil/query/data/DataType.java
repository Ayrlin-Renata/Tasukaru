package com.ayrlin.sqlutil.query.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DataType {
    STRING("TEXT"),
    INT("INTEGER"),
    TIMESTAMP("TIMESTAMP");

    private @Getter String str;

    @Override
    public String toString() {
        return this.str;
    }
}
