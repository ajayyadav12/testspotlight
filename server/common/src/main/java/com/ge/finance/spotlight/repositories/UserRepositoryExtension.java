package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.User;

import java.util.Optional;

public interface UserRepositoryExtension {

    Optional<User> findOptionalBySSO(Long sso);

}
