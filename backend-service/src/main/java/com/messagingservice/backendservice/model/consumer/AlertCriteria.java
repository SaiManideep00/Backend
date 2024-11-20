package com.messagingservice.backendservice.model.consumer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AlertCriteria")
public class AlertCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteriaId", nullable = false)
    private Long criteriaId;
    @ElementCollection
    @CollectionTable(name = "match_criteria",
            joinColumns = @JoinColumn(name = "id"))
    @Column(name = "fields")
    List<String> matchCriteria;
    @ManyToOne
    @JoinColumn(name = "recepient_criteria_fk")
    RecepientGroup recepientGroup;
}
