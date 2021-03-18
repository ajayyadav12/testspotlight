package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.FeedType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FeedTypeRepository extends CrudRepository<FeedType, Long> {

    @Override
    List<FeedType> findAll();

    Optional<FeedType> findFirstByName(String name);

}
