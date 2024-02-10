package com.example.train_ticketing_flow.controller;

import com.example.train_ticketing_flow.dto.RegisterUserDto;
import com.example.train_ticketing_flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {
    private final UserQueueService userQueueService;


    //등록 할 수 있는 API path
    @PostMapping("")
    public Mono<?> registerUser(@RequestParam(name = "queue", defaultValue = "default") String queue
            ,@RequestParam(name = "user_id") Long userId){
        return userQueueService.RegisterWaitQue(queue,userId).map(i-> new RegisterUserDto(i));
    }

}
