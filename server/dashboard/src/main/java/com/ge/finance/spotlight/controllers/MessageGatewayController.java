package com.ge.finance.spotlight.controllers;

import java.util.ArrayList;
import java.util.List;

import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.repositories.MessageGatewayRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/message-gateway")
public class MessageGatewayController {

    private MessageGatewayRepository messageGatewayRepository;

    public MessageGatewayController(MessageGatewayRepository messageGatewayRepository) {

        this.messageGatewayRepository = messageGatewayRepository;
    }

    @GetMapping("/")
    List<MessageGateway> index() {
        return messageGatewayRepository.findAll();
    }

}