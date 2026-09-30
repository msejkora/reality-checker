package cz.mata.reality.realitychecker.model;

/*
 * @created 04/10/2021 - 10:47
 * @project RealityChecker
 * @author msejkora
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Advert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String url;

    @Column
    private String urlId;

    @Column
    private Boolean active;

    @Column(name = "created_on")
    @CreationTimestamp
    private Timestamp createdOn;

    @Column(name = "deactivated_on")
    private Timestamp deactivatedOn;

    @ManyToOne
    @JoinColumn(name = "reality_server_id")
    private RealityServer realityServer;

    public Advert(String url, String id, Boolean active, RealityServer realityServer) {
        this.url = url;
        this.urlId = id;
        this.active = active;
        this.realityServer = realityServer;
    }
}
