package com.ayrlin.sqlutil.query.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)
public class OpParam extends Param {

    @AllArgsConstructor
    public enum Op {
        EQUAL("="),
        GREATER(">"),
        LESS("<"),
        LIKE("LIKE");
        
        private @Getter String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public Op operation;

    public OpParam(DataType type, String column, Op operation, Object value) {
        super(type, column, value);
        this.operation = operation;
    }
}
