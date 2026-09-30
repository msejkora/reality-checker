package cz.mata.reality.realitychecker.sreality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SrealitySearchResponse(
        @JsonProperty("status_code") Integer statusCode,
        @JsonProperty("status_message") String statusMessage,
        Pagination pagination,
        List<Listing> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(
            int limit,
            int offset,
            int total
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Listing(
            @JsonProperty("hash_id") long hashId,
            @JsonProperty("price_czk") BigDecimal priceCzk
    ) {
    }
}
