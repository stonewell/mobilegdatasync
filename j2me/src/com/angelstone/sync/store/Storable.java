package com.angelstone.sync.store;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Storable {

    public byte recordType;
    public int recordId;

    protected Storable(byte recordType) {
        this.recordType = recordType;
    }

    public abstract void readRecord(DataInputStream in) throws IOException;

    public abstract void writeRecord(DataOutputStream out) throws IOException;

}
