package cz.mata.reality.realitychecker.sreality;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SrealityClient {
    private static final int PAGE_SIZE = 100;
    private static final String DETAIL_PATH = "/detail/prodej/dum/rodinny/x/";

    private final RestClient restClient;

    public Set<SrealityListing> findAll(Properties filter) {
        Set<SrealityListing> listings = new LinkedHashSet<>();
        int offset = 0;

        while (true) {
            SrealitySearchResponse response = restClient.get()
                    .uri(buildSearchUri(filter, offset))
                    .retrieve()
                    .body(SrealitySearchResponse.class);

            if (response == null || response.pagination() == null || response.results() == null) {
                throw new IllegalStateException("Sreality returned an incomplete search response");
            }
            if (response.statusCode() != null && response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Sreality search failed with status "
                                + response.statusCode()
                                + ": "
                                + response.statusMessage());
            }
            if (response.pagination().offset() != offset) {
                throw new IllegalStateException(
                        "Sreality returned offset "
                                + response.pagination().offset()
                                + " instead of requested "
                                + offset);
            }

            List<SrealitySearchResponse.Listing> pageResults = response.results();
            pageResults.stream()
                    .filter(item -> item.priceCzk() != null && item.priceCzk().compareTo(BigDecimal.ZERO) > 0)
                    .map(item -> new SrealityListing(
                            Long.toString(item.hashId()),
                            detailBaseUrl(filter) + DETAIL_PATH + item.hashId()))
                    .forEach(listings::add);

            int loaded = response.pagination().offset() + pageResults.size();
            if (pageResults.isEmpty() || loaded >= response.pagination().total()) {
                return listings;
            }
            offset = loaded;
        }
    }

    URI buildSearchUri(Properties filter, int offset) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(required(filter, "sreality.api-url"))
                .queryParam("limit", PAGE_SIZE)
                .queryParam("offset", offset)
                .queryParam("lang", "cs")
                .queryParam("sort", "-date")
                .queryParam("category_main_cb", required(filter, "sreality.category-main"))
                .queryParam("category_sub_cb", required(filter, "sreality.category-sub"))
                .queryParam("category_type_cb", required(filter, "sreality.category-type"))
                .queryParam("locality_country_id", required(filter, "sreality.locality-country-id"))
                .queryParam("locality_entity_id", required(filter, "sreality.locality-entity-id"))
                .queryParam("locality_entity_type", required(filter, "sreality.locality-entity-type"));

        addOptional(builder, filter, "sreality.locality-radius", "locality_radius");
        addOptional(builder, filter, "sreality.block-inline-building", "block_inline_building");
        addOptional(builder, filter, "sreality.pois-distance", "pois_distance");
        addOptional(builder, filter, "sreality.price-to", "price_to");
        addRepeated(builder, filter, "sreality.room-count", "room_count_cb");
        addRepeated(builder, filter, "sreality.pois", "pois");

        return builder.build().encode().toUri();
    }

    private String detailBaseUrl(Properties filter) {
        return required(filter, "sreality.web-url").replaceAll("/+$", "");
    }

    private void addOptional(
            UriComponentsBuilder builder,
            Properties filter,
            String propertyName,
            String queryParameter
    ) {
        String value = filter.getProperty(propertyName);
        if (value != null && !value.isBlank()) {
            builder.queryParam(queryParameter, value.trim());
        }
    }

    private void addRepeated(
            UriComponentsBuilder builder,
            Properties filter,
            String propertyName,
            String queryParameter
    ) {
        String value = filter.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            return;
        }
        for (String item : value.split("\\|")) {
            if (!item.isBlank()) {
                builder.queryParam(queryParameter, item.trim());
            }
        }
    }

    private String required(Properties filter, String propertyName) {
        String value = filter.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required filter property: " + propertyName);
        }
        return value.trim();
    }
}
