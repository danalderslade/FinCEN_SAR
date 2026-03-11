package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_support_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivitySupportDocument {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "original_attachment_file_name", nullable = false, length = 255)
    private String originalAttachmentFileName;
}
