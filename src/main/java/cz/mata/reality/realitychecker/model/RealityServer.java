package cz.mata.reality.realitychecker.model;

/*
 * @created 04/10/2021 - 10:39
 * @project RealityChecker
 * @author msejkora
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RealityServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String serverName;

    @Column
    private String description;

    @Column
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "realityServer")
    private Set<Advert> adverts;
}
