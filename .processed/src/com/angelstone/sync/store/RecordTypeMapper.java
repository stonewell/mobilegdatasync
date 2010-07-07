package com.angelstone.sync.store;

public interface RecordTypeMapper {

	StorableFactory getFactory(byte recordType);
}
