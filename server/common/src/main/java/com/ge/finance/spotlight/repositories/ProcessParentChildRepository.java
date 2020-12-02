package com.ge.finance.spotlight.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.ge.finance.spotlight.models.ProcessParentChild;

public interface ProcessParentChildRepository extends JpaRepository<ProcessParentChild, Long> {

    List<ProcessParentChild> findByProcessIdOrderBySeqAscIdAsc(long processId);

}
