package com.oidioomstudio.torchmanator.client;

import com.oidioomstudio.olib.client.OModMenuApi;
import com.oidioomstudio.torchmanator.TorchmanatorMod;

public class ModMenuIntegration extends OModMenuApi {
    @Override
    protected String getModId() {
        return TorchmanatorMod.MOD_ID;
    }
}
