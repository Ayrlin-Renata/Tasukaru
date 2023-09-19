package com.ayrlin.sqlutil.query.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class SCol {
    public String column;
    public DataType type;
}
