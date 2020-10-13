package no.nav.veilarbdirigent.mock;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.IdentOppslag;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;

import java.util.List;

import static no.nav.veilarbdirigent.config.TestData.TEST_AKTOR_ID;
import static no.nav.veilarbdirigent.config.TestData.TEST_FNR;


public class AktorregisterClientMock implements AktorregisterClient {

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckResult.healthy();
    }

    @Override
    public Fnr hentFnr(AktorId aktorId) {
        return Fnr.of(TEST_FNR);
    }

    @Override
    public AktorId hentAktorId(Fnr fnr) {
        return AktorId.of(TEST_AKTOR_ID);
    }

    @Override
    public List<IdentOppslag> hentFnr(List<AktorId> list) {
        return null;
    }

    @Override
    public List<IdentOppslag> hentAktorId(List<Fnr> list) {
        return null;
    }

    @Override
    public List<AktorId> hentAktorIder(Fnr fnr) {
        return null;
    }
}
