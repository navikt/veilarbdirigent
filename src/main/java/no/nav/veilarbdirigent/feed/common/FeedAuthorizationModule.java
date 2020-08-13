package no.nav.veilarbdirigent.feed.common;

@FunctionalInterface
public interface FeedAuthorizationModule {
    boolean isRequestAuthorized(String feedname);
}
