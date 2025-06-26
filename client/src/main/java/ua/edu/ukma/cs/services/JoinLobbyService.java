package ua.edu.ukma.cs.services;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.encryption.ISymmetricEncryptionService;
import ua.edu.ukma.cs.tcp.decoders.IDecoder;
import ua.edu.ukma.cs.tcp.encoders.IEncoder;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;

@RequiredArgsConstructor
public class JoinLobbyService {

    private final HttpService httpService;
    private final IEncoder<PacketOut> encoder;
    private final IDecoder<PacketIn> decoder;
    private final ISymmetricEncryptionService symmetricEncryptionService;
    private final IAsymmetricEncryptionService asymmetricEncryptionService;

    @SneakyThrows
    public LobbyConnection joinLobby(UUID lobbyId, String token) {
        byte[] publicKey = httpService.get("/public-key").body();
        LobbyConnection lobbyConnection = new LobbyConnection(encoder, decoder, asymmetricEncryptionService, symmetricEncryptionService);
        lobbyConnection.init(lobbyId, token, publicKey);
        return lobbyConnection;
    }
}
