package com.shutterflow.core.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_contacts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientContact {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String name;

    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String relation;
}
