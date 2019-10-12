package com.zyy;

import com.zyy.response.ResponseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SpikeTest {
    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void entrySpikeTest(){

    }

}
