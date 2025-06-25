package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.services.IAsymmetricDecryptionService;

public class GetPublicKeyRouteHandler extends BaseRouteHandler {
    private final IAsymmetricDecryptionService encryptionService;

    public GetPublicKeyRouteHandler(RouteContext routeContext, IAsymmetricDecryptionService encryptionService) {
        super(routeContext);
        this.encryptionService = encryptionService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        return RouteHandlerResult.bytes(encryptionService.getPublicKey());
    }
}
