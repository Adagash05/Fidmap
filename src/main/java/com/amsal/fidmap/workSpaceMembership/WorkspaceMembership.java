//package com.amsal.fidmap.workSpaceMembership;
//
//import com.amsal.fidmap.user.User;
//import com.amsal.fidmap.workspace.Workspace;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//
//import java.time.Instant;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//public class WorkspaceMembership {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
////    @ManyToOne
////    @JoinColumn(name = "user_id", nullable = false)
////    private User user;
//
////    @ManyToOne
////    @JoinColumn(name = "workspace_id", nullable = false)
////    private Workspace workspace;
//
//    @Enumerated(EnumType.STRING)
//    private WorkspaceRole role;
//}
