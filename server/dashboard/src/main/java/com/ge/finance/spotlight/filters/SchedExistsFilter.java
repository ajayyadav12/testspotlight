package com.ge.finance.spotlight.filters;

import com.ge.finance.spotlight.dto.ScheduledSubmissionDTO;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.*;

public class SchedExistsFilter<T> implements Specification<T> {

  /**
	 *
	 */
  private static final long serialVersionUID = -1551787264861989225L;
  
  private String paramName;
  private boolean value;

  public SchedExistsFilter(String paramName, String value) {
    this.paramName = paramName;
    this.value = Boolean.parseBoolean(value);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    Join<T, ScheduledSubmissionDTO> join = root.join("scheduledSubmission");
    return criteriaBuilder.isNotNull(join);
  }
}
