package cn.nfsn.user.controller;

import cn.nfsn.api.article.TestRemoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: gaojianjie
 * @Description TODO
 * @date 2023/8/10 16:29
 */
@Api("测试案例")
@RestController
public class TestController {
    @Autowired
    private TestRemoteService testRemoteService;

    @Value("${swagger.title}")
    private String title;
    @Value("${redis.host}")
    private String host;

    @ApiOperation("Feign测试接口")
    @GetMapping("/test")
    public String test(){
        return testRemoteService.testRemoteService();
    }

    @RequestMapping("/config/get")
    public String get() {
        return title+" "+host;
    }
}
