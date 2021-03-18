package com.ge.finance.spotlight.repositories;

import java.util.List;

import com.ge.finance.spotlight.models.ModuleFilter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleFilterRepository extends JpaRepository<ModuleFilter, Long>, ModuleFilterRepositoryExtension {

    List<ModuleFilter> findByUserIdAndModuleNameAndSettingsIsNotNull(Long userId, String moduleName);
}
