package com.zyy.controller;

import com.zyy.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class TestController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test")
    public void test(){
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                ResponseResult result = restTemplate.getForObject("http://127.0.0.1:8090/spike/entry/userId/2/commodity/2", ResponseResult.class);
                if (result != null){
                    System.out.println(result.toString());
                }
            }).start();
        }
    }
}
