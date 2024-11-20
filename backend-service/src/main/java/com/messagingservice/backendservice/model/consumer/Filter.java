package com.messagingservice.backendservice.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Filter")
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filterId")
    private Long filterId;
    @Column(nullable = false)
    private String filterKey;
    @Column(nullable = false)
    private String filterValue;
}
