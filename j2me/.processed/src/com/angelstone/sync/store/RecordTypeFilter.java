package com.angelstone.sync.store;

import javax.microedition.rms.RecordFilter;

public class RecordTypeFilter implements RecordFilter {

    public byte recordType;

    public boolean matches(byte[] bytes) {
        return (bytes.length > 0) && (bytes[0] == recordType);
    }
}
