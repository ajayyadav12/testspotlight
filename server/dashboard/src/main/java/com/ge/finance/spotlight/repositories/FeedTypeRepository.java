package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.FeedType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FeedTypeRepository extends CrudRepository<FeedType, Long> {

    @Override
    List<FeedType> findAll();

}
