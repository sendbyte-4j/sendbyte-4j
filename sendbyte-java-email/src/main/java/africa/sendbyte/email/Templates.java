package africa.sendbyte.email;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.internal.AsyncSupport;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Saved template and rendering operations. */
public final class Templates {
    private final ClientRuntime runtime;

    public Templates(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public Template create(final TemplateInput input) {
        Objects.requireNonNull(input, "input");

        return runtime.execute(
                        collectionRequest(
                                "template.create", "POST", TemplateApiRequest.from(input)),
                        TemplateApiResponse.class)
                .toTemplate();
    }

    public CompletableFuture<Template> createAsync(final TemplateInput input) {
        Objects.requireNonNull(input, "input");

        return AsyncSupport.map(
                runtime.executeAsync(
                        collectionRequest(
                                "template.create", "POST", TemplateApiRequest.from(input)),
                        TemplateApiResponse.class),
                TemplateApiResponse::toTemplate);
    }

    public Page<Template> list() {
        return runtime.execute(
                        collectionRequest("template.list", "GET", null),
                        TemplatePageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<Template>> listAsync() {
        return AsyncSupport.map(
                runtime.executeAsync(
                        collectionRequest("template.list", "GET", null),
                        TemplatePageApiResponse.class),
                TemplatePageApiResponse::toPage);
    }

    public Template get(final String id) {
        return runtime.execute(
                        itemRequest("template.get", "GET", id, null), TemplateApiResponse.class)
                .toTemplate();
    }

    public CompletableFuture<Template> getAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(
                        itemRequest("template.get", "GET", id, null), TemplateApiResponse.class),
                TemplateApiResponse::toTemplate);
    }

    public Template update(final String id, final TemplateInput input) {
        Objects.requireNonNull(input, "input");

        return runtime.execute(
                        itemRequest("template.update", "PUT", id, TemplateApiRequest.from(input)),
                        TemplateApiResponse.class)
                .toTemplate();
    }

    public CompletableFuture<Template> updateAsync(final String id, final TemplateInput input) {
        Objects.requireNonNull(input, "input");

        return AsyncSupport.map(
                runtime.executeAsync(
                        itemRequest("template.update", "PUT", id, TemplateApiRequest.from(input)),
                        TemplateApiResponse.class),
                TemplateApiResponse::toTemplate);
    }

    public TemplateDeletion delete(final String id) {
        return runtime.execute(
                        itemRequest("template.delete", "DELETE", id, null),
                        TemplateDeletionApiResponse.class)
                .toTemplateDeletion();
    }

    public CompletableFuture<TemplateDeletion> deleteAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(
                        itemRequest("template.delete", "DELETE", id, null),
                        TemplateDeletionApiResponse.class),
                TemplateDeletionApiResponse::toTemplateDeletion);
    }

    public RenderedTemplate render(final RenderTemplateRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(renderRequest(request), RenderedTemplateApiResponse.class)
                .toRenderedTemplate();
    }

    public CompletableFuture<RenderedTemplate> renderAsync(final RenderTemplateRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeAsync(renderRequest(request), RenderedTemplateApiResponse.class),
                RenderedTemplateApiResponse::toRenderedTemplate);
    }

    public RenderedTemplate preview(final String id, final PreviewTemplateRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(previewRequest(id, request), RenderedTemplateApiResponse.class)
                .toRenderedTemplate();
    }

    public CompletableFuture<RenderedTemplate> previewAsync(
            final String id, final PreviewTemplateRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeAsync(
                        previewRequest(id, request), RenderedTemplateApiResponse.class),
                RenderedTemplateApiResponse::toRenderedTemplate);
    }

    private static ClientRequest collectionRequest(
            final String operation, final String method, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method).pathSegment("v1").pathSegment("templates");
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest itemRequest(
            final String operation, final String method, final String id, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method)
                        .pathSegment("v1")
                        .pathSegment("templates")
                        .pathSegment(Emails.requireId(id, "id"));
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest renderRequest(final RenderTemplateRequest request) {
        return ClientRequest.builder("template.render", "POST")
                .pathSegment("v1")
                .pathSegment("templates")
                .pathSegment("render")
                .body(TemplateApiRequest.from(request))
                .build();
    }

    private static ClientRequest previewRequest(
            final String id, final PreviewTemplateRequest request) {
        return ClientRequest.builder("template.preview", "POST")
                .pathSegment("v1")
                .pathSegment("templates")
                .pathSegment(Emails.requireId(id, "id"))
                .pathSegment("preview")
                .body(PreviewTemplateApiRequest.from(request))
                .build();
    }
}
