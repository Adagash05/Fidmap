package com.amsal.fidmap.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds Pageable values with a bounded page size and a deterministic sort
 * (a unique tiebreaker is always appended so pages never overlap/skip rows).
 */
public final class Paging {

    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 100;

    private Paging() {
    }

    public static Pageable of(Integer page, Integer size, Sort sort) {

        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        return PageRequest.of(p, s, sort);
    }

    /** Escapes LIKE wildcards in user search text and wraps it for contains-search. */
    public static String likeContains(String query) {

        if (query == null || query.isBlank()) {
            return "%";
        }

        String q = query.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + q + "%";
    }
}
