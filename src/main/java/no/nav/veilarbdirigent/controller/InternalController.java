package no.nav.veilarbdirigent.controller;

import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestUtils;
import no.nav.common.health.selftest.SelftTestCheckResult;
import no.nav.common.health.selftest.SelftestHtmlGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static no.nav.common.health.selftest.SelfTestUtils.checkAllParallel;
import static no.nav.veilarbdirigent.utils.DbUtils.checkDbHealth;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final JdbcTemplate db;

    private final List<SelfTestCheck> selftestChecks;

    public InternalController(JdbcTemplate db) {
        this.db = db;
        this.selftestChecks = Arrays.asList(
                new SelfTestCheck("Database ping", true, () -> checkDbHealth(db))
        );
    }

    @GetMapping("/isReady")
    public void isReady() { }

    @GetMapping("/isAlive")
    public void isAlive() {
        if (checkDbHealth(db).isUnhealthy()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/selftest")
    public ResponseEntity selftest() {
        List<SelftTestCheckResult> checkResults = checkAllParallel(selftestChecks);
        String html = SelftestHtmlGenerator.generate(checkResults);
        int status = SelfTestUtils.findHttpStatusCode(checkResults, true);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

}
