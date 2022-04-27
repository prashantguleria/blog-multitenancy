package se.callista.blog.service.multi_tenancy.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public final class TenantContext {

    public static final String TENANT_KEY = "TENANT-ID";

    private TenantContext() {}

    public static Mono<String> getTenantId() {
        return Mono
            .deferContextual(Mono::just)
            .filter(c -> c.hasKey(TENANT_KEY))
            .map(c -> c.get(TENANT_KEY));
    }

    public static <T> Mono<T> withTenantId(String tenantId, Mono<T> chain) {
        return chain
            .contextWrite(ctx ->
                ctx.put(TENANT_KEY, tenantId));
    }

}