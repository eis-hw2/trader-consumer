package com.example.taskconsumer.Service.Impl;

import com.example.taskconsumer.Domain.Entity.Broker;
import com.example.taskconsumer.Domain.Entity.BrokerSideUser;
import com.example.taskconsumer.Domain.Entity.TraderSideUser;
import com.example.taskconsumer.Service.BrokerService;
import com.example.taskconsumer.Service.BrokerSideUserService;
import com.example.taskconsumer.Service.RedisService;
import com.example.taskconsumer.Service.TraderSideUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class BrokerSideUserServiceImpl implements BrokerSideUserService {
    private static final Long DEFAULT_EXPIRATION = Duration.ofDays(365).getSeconds();

    @Autowired
    private BrokerService brokerService;
    @Autowired
    private TraderSideUserService traderSideUserService;

    private static Logger logger = LoggerFactory.getLogger("BrokerSideUserService");
    private static final String REDIS_KEY_PREFIX = "trader-gateway:BrokerSideUserService:login:";

    @Autowired
    private RedisService redisService;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Override
    public String getToken(String traderSideUsername, Integer brokerId){
        String key = getKey(traderSideUsername, brokerId);
        if (redisService.exists(key))
            return redisService.get(key, String.class);
        return login(traderSideUsername, brokerId);
    }

    @Override
    public String login(String traderSideUsername, Integer brokerId) {
        logger.info("[BrokerSideUserService.login] TraderSideUsername: " + traderSideUsername);
        logger.info("[BrokerSideUserService.login] BrokerId: " + brokerId);
        TraderSideUser traderSideUser = traderSideUserService.findByUsername(traderSideUsername);
        BrokerSideUser brokerSideUser = traderSideUser.getBrokerSideUser(brokerId);
        //logger.info("[BrokerSideUserService.login] BrokerSideUser: " + JSON.toJSONString(brokerSideUser));
        Broker broker = brokerService.findById(brokerId);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("username", brokerSideUser.getUsername());
        map.add("password", brokerSideUser.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String url = broker.getLoginApi() + "/login";
        logger.info("[BrokerSideUserSerivce.login] url: " + url);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        HttpHeaders responseHeaders = response.getHeaders();
        String token = responseHeaders.get("token").get(0);
        logger.info("[BrokerSideUserSerivce.login] token: " + token);

        String key =  getKey(traderSideUsername, brokerId);
        redisService.set(key, token, DEFAULT_EXPIRATION);
        return token;
    }

    private String getKey(String username, Integer brokerId){
        return REDIS_KEY_PREFIX + username + ":" + brokerId;
    }
}
