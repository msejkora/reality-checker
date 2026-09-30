package cz.mata.reality.realitychecker.service;

import cz.mata.reality.realitychecker.model.Advert;
import cz.mata.reality.realitychecker.model.RealityServer;
import cz.mata.reality.realitychecker.repository.AdvertRepository;
import cz.mata.reality.realitychecker.repository.RealityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/*
 * @created 07/10/2021 - 16:39
 * @project RealityChecker
 * @author msejkora
 */
@Service
@Slf4j
@AllArgsConstructor
public class DbService {

    private final RealityRepository realityRepository;
    private final AdvertRepository advertRepository;

    public RealityServer findByServerName(String serverName) {
        return realityRepository.findByServerName(serverName);
    }

    public void disableAdvert(Advert advert) {
        advert.setActive(false);
        advertRepository.save(advert);
    }

    public void disableAdvertGroup(Set<Advert> advertSet) {
        for (Advert advert : advertSet) {
            disableAdvert(advert);
        }
    }

    public Iterable<Advert> saveAdvertToDb(Set<Advert> advertSet) {
        return advertRepository.saveAll(advertSet);
    }

}
