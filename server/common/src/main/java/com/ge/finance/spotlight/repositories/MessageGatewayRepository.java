package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.MessageGateway;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageGatewayRepository extends JpaRepository<MessageGateway, Long> {

    Optional<MessageGateway> findFirstByCarrierAndCountry(String carrier, String country);

}