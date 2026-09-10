package com.amsal.fidmap.payment.billing;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_plan", nullable = false, unique = true)
    private BillingPlan billingPlan;

    @Column(name = "max_team_members", nullable = false)
    private Integer maxTeamMembers;

    @Column(name = "max_boards")
    private Integer maxBoards;

    @Column(name = "max_feedback_posts")
    private Integer maxFeedbackPosts;

    @Column(name = "max_end_users")
    private Integer maxEndUsers;

    @Column(name = "max_roadmap_items")
    private Integer maxRoadmapItems;

    @Column(name = "max_changelog_entries")
    private Integer maxChangelogEntries;

    @Column(name = "custom_branding")
    private boolean customBranding;

    @Column(name = "remove_fidmap_branding", nullable = false)
    private boolean removeFidmapBranding;

    @Column(name = "private_boards", nullable = false)
    private boolean privateBoards;
}