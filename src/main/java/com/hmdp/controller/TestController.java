package com.hmdp.controller;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/test/mq")
    public String testSend() {
        rocketMQTemplate.convertAndSend("test-topic", "Hello RocketMQ!");
        return "消息发送成功！";
    }
}
