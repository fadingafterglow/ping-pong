package ua.edu.ukma.cs.services;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.encryption.AesEncryptionService;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyRequest;

import java.net.InetAddress;
import java.util.UUID;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.utils.SharedObjectMapper;

import java.io.OutputStream;
import java.net.Socket;

@RequiredArgsConstructor
public class JoinLobbyService {
    private final HttpService httpService;
    private final AesEncryptionService aesEncryptionService;
    private final RsaEncryptionService rsaEncryptionService;

    @SneakyThrows
    public Socket joinLobby(UUID lobbyId) {
        JoinLobbyRequest joinLobbyRequest = formJoinLobbyRequest(lobbyId);
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), 10101);
        byte[] requestBytes = SharedObjectMapper.S.writeValueAsBytes(joinLobbyRequest);
        OutputStream out = socket.getOutputStream();
        out.write(requestBytes);
        out.flush();
        return socket;
    }

    @SneakyThrows
    private JoinLobbyRequest formJoinLobbyRequest(UUID lobbyId) {
        byte[] publicKey = httpService.get("/public-key").body();
        byte[] symmetricKey = aesEncryptionService.generateKey();
        byte[] encryptedSymmetricKey = rsaEncryptionService.encrypt(symmetricKey, publicKey);
        return new JoinLobbyRequest(lobbyId, AppState.getJwtToken(), encryptedSymmetricKey);
    }
}
