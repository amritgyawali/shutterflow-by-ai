package com.shutterflow.core.client;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClientSpecification {

    public static Specification<Client> filterClients(
            String search, 
            String tag, 
            String leadSource, 
            BigDecimal minSpend, 
            BigDecimal maxSpend,
            String studioId) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce multi-tenant isolation!
            predicates.add(cb.equal(root.get("studioId"), studioId));

            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate firstLike = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastLike = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstLike, lastLike, emailLike));
            }

            if (tag != null && !tag.isBlank()) {
                predicates.add(cb.like(root.get("tags"), "%" + tag + "%"));
            }

            if (leadSource != null && !leadSource.isBlank()) {
                predicates.add(cb.equal(root.get("leadSource"), leadSource));
            }

            if (minSpend != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalSpend"), minSpend));
            }

            if (maxSpend != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalSpend"), maxSpend));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
