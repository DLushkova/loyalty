package com.clubloyalty.server.interfaces;

import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;

public interface ServiceInterface {
    Response execute(Request request);
}