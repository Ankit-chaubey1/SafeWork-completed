package com.cts.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long auditId;

    private String userAuditAction;
    private String userAuditResource;
    private Date userAuditTimeStamp;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;
}

