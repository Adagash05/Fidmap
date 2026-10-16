package com.amsal.fidmap.decision;

/** System recommendations are PRIORITIZE/CONSIDER/DEFER/INVESTIGATE. REJECT is a human-only decision. */
public enum Recommendation {
    PRIORITIZE,
    CONSIDER,
    INVESTIGATE,
    DEFER,
    REJECT
}
