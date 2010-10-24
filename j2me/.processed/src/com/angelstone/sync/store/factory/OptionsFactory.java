package com.angelstone.sync.store.factory;

import com.angelstone.sync.option.Options;
import com.angelstone.sync.store.Storable;
import com.angelstone.sync.store.StorableFactory;

public class OptionsFactory implements StorableFactory {

    public Storable create() {
        return new Options();
    }
}
