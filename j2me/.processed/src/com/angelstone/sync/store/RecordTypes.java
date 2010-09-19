package com.angelstone.sync.store;

import com.angelstone.sync.store.factory.OptionsFactory;

public class RecordTypes implements RecordTypeMapper {

    public static final byte OPTIONS = 1;

    private OptionsFactory optionsFactory = new OptionsFactory();

    public StorableFactory getFactory(byte recordType) {
        switch (recordType) {
            case OPTIONS:
                return optionsFactory;
            default:
                throw new StoreException("Unknown record type " + recordType);
        }
    }
}
