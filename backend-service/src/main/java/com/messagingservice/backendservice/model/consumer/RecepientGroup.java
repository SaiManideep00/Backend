package com.messagingservice.backendservice.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RecepientGroup")
public class RecepientGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "groupId", nullable = false)
    private Long groupId;
    @Column(unique = true)
    private String groupName;
    @ElementCollection
    @CollectionTable(name = "emails",
            joinColumns = @JoinColumn(name = "groupId"))
    @Column(name = "emailIds")
    private List<String> emailIds;
}
