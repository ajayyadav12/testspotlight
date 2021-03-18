package com.ge.finance.spotlight.filters;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BeforeTimeFilter<T> implements Specification<T> {

    /**
	 *
	 */
	private static final long serialVersionUID = -2468571321630467088L;

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private String paramName;
    private Date time;

    public BeforeTimeFilter(String paramName, String time) {
        this.paramName = paramName;
        try {
            Date date = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            this.time = calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            this.time = new Date();
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.lessThanOrEqualTo(root.get(paramName), time);
    }

}
