package com.ge.finance.spotlight.repositories;

import java.util.List;

public interface ModuleFilterRepositoryExtension {

    boolean existsAllForIdListAndSso(List<Long> idList, Long sso);

}
