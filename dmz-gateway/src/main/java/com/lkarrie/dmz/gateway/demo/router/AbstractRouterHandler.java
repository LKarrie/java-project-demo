package com.lkarrie.dmz.gateway.demo.router;

import cn.hutool.core.io.FileUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.lkarrie.dmz.gateway.demo.Constants;
import com.lkarrie.dmz.gateway.demo.DemoCustomProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象 Router Handler 类
 */
@Slf4j
public abstract class AbstractRouterHandler implements InitializingBean {

    private static final String findUrlSavePath = FileUtil.getUserHomePath();
    private static List<String> suffixList = Arrays.asList(
            "bmp", "pcx", "tif", "gif",
            "jpg", "tga", "fpx", "svg",
            "psd", "png", "raw", "ico",
            "css", "htm", "js",
            "jpeg","exif"
    );

    @Autowired
    private DemoCustomProperties demoCustomProperties;

    /**
     * handle backend request
     * @param app
     * @param url
     * @param method
     */
    public void handle(String app,String url,String method){
        throw new UnsupportedOperationException();
    }

    /**
     * handle frontend file
     * @param path
     */
    public void handle(RequestPath path){
        throw new UnsupportedOperationException();
    }

    /**
     * 核心过滤方法
     * @param app
     * @param url
     * @param method
     * @return
     */
    protected Boolean isAllowUrl(String app,String url,String method){
        if(log.isDebugEnabled()){
            log.debug("[{}]网关路由策略捕获接口[{}]执行接口过滤!",app,url);
        }

        // 未配置白名单禁止访问
        Optional.ofNullable(demoCustomProperties.getAppMap().get(app)).orElseThrow(()->{
            log.warn("未配置[{}]模块 router api白名单!",app);
            if(demoCustomProperties.getFindAllowUrlEnabled()){
                //记录没有录入白名单的URL
                savePathToFile(app,url,method);
            }
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.API_FORBIDDEN);
        });

        // 存在白名单配置
        AtomicReference<Boolean> flag = new AtomicReference<>(false);
        // 匹配路径
        if(demoCustomProperties.getAppMap().get(app).get(url)!=null){
            // 匹配方法
            if(demoCustomProperties.getAppMap().get(app).get(url).contains(method)){
                // 匹配成功
                return true;
            }
        }else{
            // 正常路径无法匹配 匹配path传参路径
            Optional<Map<Pattern, List<String>>> pathParamMap = Optional.ofNullable(demoCustomProperties.getAppPathParamMap().get(app));
            pathParamMap.ifPresentOrElse(u->{
                if(u.keySet().stream().anyMatch(e->{
                    Matcher matcher = e.matcher(url);
                    // 匹配路径
                    if (matcher.find()) {
                        // 匹配方法
                        return u.get(e).contains(method);
                    }else{
                        return false;
                    }
                })){
                   flag.set(true);
                }
            },()->{
                log.warn("未配置[{}]模块 router api正则白名单!不进行模糊校验!",app);
            });
        }
        if(demoCustomProperties.getFindAllowUrlEnabled() && !flag.get()){
            //记录没有录入白名单的URL
            savePathToFile(app,url,method);
        }

        return flag.get();
    }

    /**
     * save url to file
     * easy configuration by recording rejected api
     * @param app
     * @param url
     * @param method
     */
    private static void savePathToFile(String app,String url,String method){
        StringBuilder setting = new StringBuilder(Constants.ALLOW_API_SETTING_PREFIX
                .replace("$1",url)
                .replace("$2",method)
        ).append("\n");
        FileUtil.appendUtf8String(setting.toString(),findUrlSavePath + "\\" + app + ".txt");
    }

    /**
     * 根据文件后缀 放行前端 资源文件
     * @param path
     * @return
     */
    protected Boolean isAllowWebFile(String path) {
        String suffix = path.substring(path.lastIndexOf('.') + 1);
        if(StringUtils.isNotBlank(suffix)){
            if(StringUtils.isNotBlank(demoCustomProperties.getFrontSuffix())) {
                suffixList = Arrays.asList(StringUtils.split(demoCustomProperties.getFrontSuffix(), ","));
            }
            boolean contains = suffixList.contains(suffix.toLowerCase());
            if(!contains) log.warn("前端资源文件请求[{}]被拦截!",path);
            if(demoCustomProperties.getFindAllowUrlEnabled() && !contains){
                //记录没有录入白名单的URL
                savePathToFile("web",path,"GET");
            }
            return contains;
        }
        return false;
    }
}
