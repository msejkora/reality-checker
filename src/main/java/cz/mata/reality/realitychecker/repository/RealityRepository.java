package cz.mata.reality.realitychecker.repository;

import cz.mata.reality.realitychecker.model.RealityServer;
import org.springframework.data.repository.CrudRepository;

/*
 * @created 04/10/2021 - 12:22
 * @project RealityChecker
 * @author msejkora
 */
public interface RealityRepository extends CrudRepository<RealityServer, Long> {
    RealityServer findByServerName(String serverName);

}
