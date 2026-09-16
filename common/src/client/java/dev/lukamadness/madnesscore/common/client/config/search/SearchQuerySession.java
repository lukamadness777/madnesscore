package dev.lukamadness.madnesscore.common.client.config.search;

import java.util.List;

public interface SearchQuerySession {
    List<? extends TextSource> getSearchResults(String query);
}
