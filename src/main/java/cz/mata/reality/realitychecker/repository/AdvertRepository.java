package cz.mata.reality.realitychecker.repository;

import cz.mata.reality.realitychecker.model.Advert;
import org.springframework.data.repository.CrudRepository;

/*
 * @created 04/10/2021 - 12:22
 * @project RealityChecker
 * @author msejkora
 */
public interface AdvertRepository extends CrudRepository<Advert, Long> {
}
