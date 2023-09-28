package com.ayrlin.tasukaru.data.info;

import java.util.ArrayList;
import java.util.List;

import co.casterlabs.rakurai.json.TypeToken;

public class StringListInfo extends ListInfo<String> {
    public StringListInfo() {
        this("");
    }
    public StringListInfo(String name) {
        super(new ArrayList<String>(), name);
        this.type = new TypeToken<Info<List<String>>>() {};
        this.datatype = new TypeToken<List<String>>() {};
    }
}
