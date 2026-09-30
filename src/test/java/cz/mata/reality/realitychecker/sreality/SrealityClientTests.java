package cz.mata.reality.realitychecker.sreality;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SrealityClientTests {

    @Test
    void buildsNewSearchParameters() {
        SrealityClient client = new SrealityClient(RestClient.create());

        URI uri = client.buildSearchUri(filter(), 200);
        var query = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

        assertThat(uri.getPath()).isEqualTo("/api/v1/estates/search");
        assertThat(query.getFirst("limit")).isEqualTo("100");
        assertThat(query.getFirst("offset")).isEqualTo("200");
        assertThat(query.getFirst("locality_radius")).isEqualTo("5");
        assertThat(query.getFirst("block_inline_building")).isEqualTo("true");
        assertThat(query.getFirst("locality_entity_id")).isEqualTo("4822");
        assertThat(query.getFirst("price_to")).isEqualTo("12000000");
        assertThat(query.get("room_count_cb")).isEqualTo(List.of("4", "5"));
        assertThat(query.get("pois")).isEqualTo(List.of("7", "8"));
    }

    @Test
    void loadsAllPagesAndCreatesStableDetailUrls() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SrealityClient client = new SrealityClient(builder.build());
        Properties filter = filter();

        server.expect(once(), requestTo(client.buildSearchUri(filter, 0)))
                .andRespond(withSuccess("""
                        {
                          "status_code": 200,
                          "pagination": {"limit": 100, "offset": 0, "total": 2},
                          "results": [
                            {"hash_id": 671584332, "price_czk": 10290000}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(client.buildSearchUri(filter, 1)))
                .andRespond(withSuccess("""
                        {
                          "status_code": 200,
                          "pagination": {"limit": 100, "offset": 1, "total": 2},
                          "results": [
                            {"hash_id": 3091566668, "price_czk": 5450000},
                            {"hash_id": 1, "price_czk": 0}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Set<SrealityListing> listings = client.findAll(filter);

        assertThat(listings).containsExactly(
                new SrealityListing(
                        "671584332",
                        "https://www.sreality.cz/detail/prodej/dum/rodinny/x/671584332"),
                new SrealityListing(
                        "3091566668",
                        "https://www.sreality.cz/detail/prodej/dum/rodinny/x/3091566668"));
        server.verify();
    }

    private Properties filter() {
        Properties properties = new Properties();
        properties.setProperty("sreality.api-url", "https://www.sreality.cz/api/v1/estates/search");
        properties.setProperty("sreality.web-url", "https://www.sreality.cz");
        properties.setProperty("sreality.category-main", "2");
        properties.setProperty("sreality.category-sub", "37");
        properties.setProperty("sreality.category-type", "1");
        properties.setProperty("sreality.locality-country-id", "112");
        properties.setProperty("sreality.locality-entity-id", "4822");
        properties.setProperty("sreality.locality-entity-type", "municipality");
        properties.setProperty("sreality.locality-radius", "5");
        properties.setProperty("sreality.block-inline-building", "true");
        properties.setProperty("sreality.room-count", "4|5");
        properties.setProperty("sreality.pois", "7|8");
        properties.setProperty("sreality.pois-distance", "2");
        properties.setProperty("sreality.price-to", "12000000");
        return properties;
    }
}
