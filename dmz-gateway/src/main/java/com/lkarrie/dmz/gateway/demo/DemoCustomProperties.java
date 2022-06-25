package com.lkarrie.dmz.gateway.demo;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "demo.gateway")
@Data
@RefreshScope
public class DemoCustomProperties {

    private Boolean apiAllowEnabled = true;
    private Boolean ipCheckEnabled = true;
    private Boolean findAllowUrlEnabled = false;

    /**
     * 配置常规接口（接口请求路径固定） 白名单
     */
    private Map<String,Map<String,List<String>>> appMap;
    /**
     * 配置非常规接口 白名单
     * 例如 将数据id作为path参数调用接口 /base/get/19位id | /base/get/uuid | /base/get/19位id/detail
     */
    private Map<String,Map<Pattern,List<String>>> appPathParamMap;

    private String frontSuffix;
    private White white;
    private Block block;

    @Getter
    public static class White {
        private final String defaultIpRegStr = "^(127\\.0\\.0\\.1)|(localhost)";
        private List<String> networkSegment;
        private Pattern whiteIpReg;
        public void setNetworkSegment(List<String> networkSegment) {
            this.networkSegment = networkSegment;
            // 重新生成白名单IP正则表达式
            StringBuilder stringBuilder = new StringBuilder(defaultIpRegStr);
            networkSegment.forEach(e->{
                    stringBuilder.append("|").append(e);
            });
            whiteIpReg = Pattern.compile(stringBuilder.toString());
        }
    }

    @Getter
    public static class Block {
        private List<String> ip;
        // 可以设置有意义的默认黑名单IP值
        private final String defaultIpRegStr = "^(0\\.0\\.0\\.0)";
        private Pattern blockIpReg;
        public void setIp(List<String> ip) {
            this.ip = ip;
            // 重新生成黑名单IP正则表达式
            StringBuilder stringBuilder = new StringBuilder(defaultIpRegStr);
            ip.forEach(e->{
                stringBuilder.append("|").append(e);
            });
            blockIpReg = Pattern.compile(stringBuilder.toString());
        }
    }

}
