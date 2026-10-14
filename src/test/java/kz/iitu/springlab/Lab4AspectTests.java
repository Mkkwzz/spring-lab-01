package kz.iitu.springlab;

import kz.iitu.springlab.aspect.SlowCall;
import kz.iitu.springlab.aspect.SlowCallRegistry;
import kz.iitu.springlab.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class Lab4AspectTests {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private SlowCallRegistry slowCalls;

    @Test
    void serviceIsCglibProxy() {
        assertThat(AopUtils.isAopProxy(catalogService)).isTrue();
        assertThat(AopUtils.isCglibProxy(catalogService)).isTrue();
    }

    @Test
    void successfulCallFiresBeforeAndAfterReturning(CapturedOutput output) {
        catalogService.findById(7);

        assertThat(output).contains("[LOG] -> CatalogService.findById(..) args=[7]");
        assertThat(output).contains("[LOG] <- CatalogService.findById(..) returned Item no. 7");
        assertThat(output).doesNotContain("[AUDIT] start CATALOG_LIST at");
    }

    @Test
    void failingCallFiresAfterThrowingAndRethrows(CapturedOutput output) {
        assertThatThrownBy(() -> catalogService.remove(0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(output).contains("[LOG] !! CatalogService.remove(..) threw IllegalArgumentException");
        assertThat(output).contains("[AUDIT] CATALOG_REMOVE failure");
    }

    @Test
    void slowCallIsRecordedAndFastCallIsNot() {
        int before = slowCalls.findAll().size();

        catalogService.findById(1);
        assertThat(slowCalls.findAll()).hasSize(before);

        catalogService.findAll(3);
        assertThat(slowCalls.findAll()).hasSize(before + 1);
        SlowCall recorded = slowCalls.findAll().getLast();
        assertThat(recorded.method()).isEqualTo("CatalogService.findAll(..)");
        assertThat(recorded.executionTimeMs()).isGreaterThan(200);
        assertThat(recorded.thresholdMs()).isEqualTo(200);
    }

    @Test
    void selfInvocationBypassesAspectsUntilFixed(CapturedOutput output) {
        catalogService.removeTwice(5);
        assertThat(output).contains("[LOG] -> CatalogService.removeTwice(..)");
        assertThat(output).doesNotContain("[LOG] -> CatalogService.remove(..)");
        assertThat(output).doesNotContain("[AUDIT] start CATALOG_REMOVE");

        catalogService.removeTwiceFixed(5);
        assertThat(output).contains("[LOG] -> CatalogService.remove(..)");
        assertThat(output).contains("[AUDIT] start CATALOG_REMOVE");
    }
}
