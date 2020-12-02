package com.ge.finance.spotlight.filters;

import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class FilterBuilder<T> {

    private Map<String, String> filters;
    private boolean flag;

    public FilterBuilder(Map<String, String> filters) {
        this.filters = filters;
    }

    public FilterBuilder(Map<String, String> filters, Boolean flag) {
        this.filters = filters;
        this.flag = flag;
    }

    public Specification<T> build() {
        Specification<T> specification = null;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue().isBlank()) {
                continue;
            }
            Specification<T> newSpecification;
            switch (entry.getKey()) {
            case "from":
                newSpecification = new AfterTimeFilter<>("startTime", entry.getValue());
                break;
            case "to":
                newSpecification = new BeforeTimeFilter<>("startTime", entry.getValue());
                break;
            case "childId":
            case "parentId":
                newSpecification = new IdInFilter<>("id", entry.getValue());
                break;
            case "bu":
            case "altId":
                newSpecification = new StringFilter<>(entry.getKey(), entry.getValue());
                break;
            case "adHoc":
                newSpecification = new AdHocFilter<>(entry.getKey(), entry.getValue());
                break;
            default:
                newSpecification = new IdInFilter<>(entry.getKey(), entry.getValue());
                break;
            }
            if (specification != null && !flag) {
                specification = Specification.where(specification).and(newSpecification);
            } else if (flag) {
                specification = Specification.where(specification).or(newSpecification);
            } else {
                specification = newSpecification;
            }
        }
        return specification;
    }

}
