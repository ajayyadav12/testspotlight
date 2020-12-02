package com.ge.finance.spotlight.filters;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class StringFilter<T> implements Specification<T> {

  private String paramName;
  private String value;

  public StringFilter(String paramName, String value) {
    this.paramName = paramName;
    this.value = value;
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    return criteriaBuilder.like(criteriaBuilder.upper(root.get(paramName)), "%" + value.toUpperCase() + "%");
  }
}
