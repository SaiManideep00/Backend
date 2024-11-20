package com.messagingservice.backendservice.model.provider;

import lombok.*;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "connections",
        uniqueConstraints = {@UniqueConstraint(name = "URL", columnNames = "url"), @UniqueConstraint(name = "provider_connectionName", columnNames = {"connection_name", "pc_fk"})}
)
public class  Connections {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long connectionId;
    @Column(nullable = false, name = "connection_name")
    private String connectionName;
    @Column(nullable = false)
    private String connectionType; //DB/HTTP/MQ/SFTP
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean active = true;
}
