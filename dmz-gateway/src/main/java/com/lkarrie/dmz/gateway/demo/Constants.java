package com.lkarrie.dmz.gateway.demo;

public interface Constants {

    String UNKNOWN = "unknown";
    String SYSTEM_UNAVAILABLE="系统出现异常，请联系管理员";
    String API_FORBIDDEN="公网禁止访问,请使用私网发起当前请求！";
    String WEB_FORBIDDEN="公网禁止访问,请使用私网发请求当前页面！";
    String IP_FORBIDDEN="您当前的IP[$]禁止访问系统！";
    String ALLOW_API_SETTING_PREFIX = "\"[$1]\": \"$2\"";

    /**
     * 公网路由id
     */
    String LAST = "last";
    String API = "api";
    String BASE = "base";
    String AUTH = "auth";
    String MDATA = "mdata";

}
