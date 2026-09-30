package cz.mata.reality.realitychecker.service;

import cz.mata.reality.realitychecker.configuration.AppProperties;
import cz.mata.reality.realitychecker.configuration.EmailProperties;
import cz.mata.reality.realitychecker.email.EmailService;
import cz.mata.reality.realitychecker.model.Advert;
import cz.mata.reality.realitychecker.model.RealityServer;
import cz.mata.reality.realitychecker.sreality.SrealityClient;
import cz.mata.reality.realitychecker.sreality.SrealityListing;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @created 01/10/2021 - 12:41
 * @project RealityChecker
 * @author msejkora
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SbazarService {
    private static final String SERVER_NAME = "sbazar";

    private final AppProperties appProperties;
    private final EmailProperties emailProperties;
    private final SrealityClient srealityClient;
    private final DbService dbService;
    private final EmailService emailService;
    private List<Properties> filters;
    private RealityServer realityServer;

    @PostConstruct
    private void initialize() {
        filters = loadFilters();
    }

    @Transactional
    @Scheduled(fixedDelayString = "${main.repeat.interval}")
    public void callServer() {
        log.debug("Calling Sreality with {} filters", filters.size());
        initializeRealityServer();
        Set<Advert> dbAdvertList = advertsFromDb();
        Set<Advert> allActiveAdverts = new HashSet<>();
        boolean allFiltersSuccessful = true;

        for (Properties filter : filters) {
            try {
                Set<Advert> urlList = toAdverts(srealityClient.findAll(filter));
                processAdvertChanges(urlList, dbAdvertList);
                allActiveAdverts.addAll(urlList);
            } catch (RuntimeException exception) {
                allFiltersSuccessful = false;
                log.error(
                        "Sreality search failed for locality entity id={}",
                        filter.getProperty("sreality.locality-entity-id"),
                        exception);
            }
        }

        if (allFiltersSuccessful) {
            setActiveFlag(allActiveAdverts, dbAdvertList);
        } else {
            log.warn("Skipping advert deactivation because at least one Sreality search failed");
        }
    }

    private List<Properties> loadFilters() {
        File dir = new File(appProperties.getFilterDirectory());
        File[] files = dir.listFiles((d, name) -> name.startsWith(SERVER_NAME) && name.endsWith(".properties"));
        if (files == null) {
            throw new IllegalStateException("Cannot read filter directory: " + dir.getAbsolutePath());
        }
        if (files.length == 0) {
            throw new IllegalStateException("No Sreality filters found in: " + dir.getAbsolutePath());
        }

        List<Properties> loadedFilters = new ArrayList<>();
        for (File file : files) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                loadedFilters.add(properties);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot load filter file: " + file.getAbsolutePath(), exception);
            }
        }
        return loadedFilters;
    }

    private void processAdvertChanges(Set<Advert> urlList, Set<Advert> dbAdvertList) {
        Set<Advert> toCreate = urlList.stream()
                .filter(dbId -> dbAdvertList.stream().noneMatch(url -> url.getUrlId().equals(dbId.getUrlId())))
                .collect(Collectors.toSet());
        if (!toCreate.isEmpty()) {
            log.info("New adverts have been found {}", Arrays.toString(toCreate.toArray()));
            sendEmail(toCreate);
            dbService.saveAdvertToDb(toCreate);
        } else {
            log.debug("No new adverts have been found.");
        }
    }

    private void setActiveFlag(Set<Advert> allActiveAdverts, Set<Advert> dbAdvertList) {
        Set<Advert> toDeactivate = dbAdvertList.stream()
                .filter(Advert::getActive)
                .filter(urlId -> allActiveAdverts.stream().noneMatch(db -> db.getUrlId().equals(urlId.getUrlId())))
                .collect(Collectors.toSet());
        if (!toDeactivate.isEmpty()) {
            log.info("These adverts are no longer active. {}", Arrays.toString(toDeactivate.toArray()));
            dbService.saveAdvertToDb(toDeactivate.stream().map(advert -> {
                advert.setActive(false);
                advert.setDeactivatedOn(new Timestamp(System.currentTimeMillis()));
                return advert;
            }).collect(Collectors.toSet()));
        }
    }

    private void sendEmail(Set<Advert> toCreate) {
        String text = formatContent(toCreate);
        String subject = toCreate.size() > 1 ? "Nové domy na prodej" : "Nový dům na prodej";
        emailService.sendSimpleMessage(subject, text, emailProperties.getAddresses());
    }

    private String formatContent(Set<Advert> toCreate) {
        StringBuilder stringBuilder = new StringBuilder();
        toCreate.forEach(item -> stringBuilder.append(item.getUrl()).append("\n"));
        return stringBuilder.toString();
    }

    private Set<Advert> advertsFromDb() {
        Set<Advert> dbAdverts = realityServer.getAdverts();
        if (Objects.isNull(dbAdverts) || dbAdverts.isEmpty()) {
            log.info("No adverts have been found in database.");
            dbAdverts = new HashSet<>();
        }
        return dbAdverts;
    }

    private void initializeRealityServer() {
        this.realityServer = dbService.findByServerName(SERVER_NAME);
    }

    private Set<Advert> toAdverts(Set<SrealityListing> listings) {
        log.debug("{} estates have been found.", listings.size());
        return listings.stream()
                .map(item -> new Advert(item.url(), item.id(), true, realityServer))
                .collect(Collectors.toSet());
    }
}
