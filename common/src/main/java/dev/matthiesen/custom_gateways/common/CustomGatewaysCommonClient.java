package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.common.matthiesen_lib.abstracts.AbstractCommonClientMod;

public final class CustomGatewaysCommonClient extends AbstractCommonClientMod {
    public static final CustomGatewaysCommonClient INSTANCE = new CustomGatewaysCommonClient();

    private CustomGatewaysCommonClient() {
        super(CustomGatewaysCommon.INSTANCE);
    }

    @Override
    public void initialize() {
    }

    public void registerRenderers() {

    }
}
