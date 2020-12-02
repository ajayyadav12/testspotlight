package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.FeedType;
import com.ge.finance.spotlight.repositories.FeedTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/feed-types")
public class FeedTypeController {

    private FeedTypeRepository feedTypeRepository;

    public FeedTypeController(FeedTypeRepository feedTypeRepository) {
        this.feedTypeRepository = feedTypeRepository;
    }

    @GetMapping("/")
    List<FeedType> index() {
        return feedTypeRepository.findAll();
    }

}
