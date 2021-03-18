package com.ge.finance.spotlight.filters;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class IdInFilter<T> implements Specification<T> {

    /**
	 *
	 */
	private static final long serialVersionUID = -7579472131074095657L;
	private String paramName;
    private String idList;

    public IdInFilter(String paramName, String idList) {
        this.paramName = paramName;
        this.idList = idList;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Long> list = new ArrayList<>();
        String[] items = idList.split(",");
        for (String item : items) {
            list.add(Long.parseLong(item));
        }
        return root.get(paramName).in(list);
    }
}
