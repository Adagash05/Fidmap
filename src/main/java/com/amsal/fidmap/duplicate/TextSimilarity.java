package com.amsal.fidmap.duplicate;

import java.util.*;

/** Pure text similarity used to suggest (never auto-merge) duplicate requests. */
public final class TextSimilarity {

    private static final Set<String> STOP = Set.of(
            "a", "an", "the", "and", "or", "to", "of", "in", "on", "for", "with", "is", "are", "be", "it",
            "i", "we", "you", "my", "our", "can", "could", "would", "should", "please", "add", "have",
            "has", "want", "need", "like", "this", "that", "as", "at", "by", "from", "so", "do", "not");

    private TextSimilarity() {
    }

    public static Set<String> tokens(String text) {

        Set<String> out = new HashSet<>();

        if (text == null) {
            return out;
        }

        for (String raw : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {

            if (raw.length() < 2 || STOP.contains(raw)) {
                continue;
            }

            String t = raw;

            if (t.length() > 4 && t.endsWith("ies")) {
                t = t.substring(0, t.length() - 3) + "y";
            } else if (t.length() > 3 && t.endsWith("s") && !t.endsWith("ss")) {
                t = t.substring(0, t.length() - 1);
            }

            out.add(t);
        }

        return out;
    }

    public static double jaccard(Set<String> a, Set<String> b) {

        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }

        int shared = 0;

        for (String t : a) {
            if (b.contains(t)) {
                shared++;
            }
        }

        return shared / (double) (a.size() + b.size() - shared);
    }

    /** 0..1: title overlap weighs 70%, title+description overlap 30%. */
    public static double similarity(String titleA, String descA, String titleB, String descB) {

        Set<String> ta = tokens(titleA);
        Set<String> tb = tokens(titleB);

        Set<String> fa = new HashSet<>(ta);
        fa.addAll(tokens(descA));
        Set<String> fb = new HashSet<>(tb);
        fb.addAll(tokens(descB));

        return 0.7 * jaccard(ta, tb) + 0.3 * jaccard(fa, fb);
    }

    public static List<String> sharedTerms(String titleA, String titleB) {

        Set<String> a = tokens(titleA);
        List<String> shared = new ArrayList<>();

        for (String t : tokens(titleB)) {
            if (a.contains(t)) {
                shared.add(t);
            }
        }

        Collections.sort(shared);

        return shared;
    }
}
