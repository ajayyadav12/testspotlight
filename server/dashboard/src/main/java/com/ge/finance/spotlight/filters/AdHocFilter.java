package com.ge.finance.spotlight.filters;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AdHocFilter<T> implements Specification<T> {

  /**
	 *
	 */
  private static final long serialVersionUID = -1551787264861989225L;
  
  private String paramName;
  private char value;

  public AdHocFilter(String paramName, String value) {
    this.paramName = paramName;
    this.value = value.toCharArray()[0];
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    return criteriaBuilder.equal(root.get(paramName), value);
  }
}
