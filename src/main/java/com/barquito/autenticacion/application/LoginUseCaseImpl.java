package com.barquito.autenticacion.application;

import com.barquito.autenticacion.domain.CredencialesInvalidasException;
import com.barquito.autenticacion.domain.Usuario;
import com.barquito.autenticacion.domain.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación del caso de uso de login.
 *
 * <p>Aplica protección contra timing-attack: si el usuario no existe, se ejecuta
 * un check BCrypt ficticio con un hash conocido para que el tiempo de respuesta
 * sea indistinguible del caso en que el usuario existe pero el PIN es incorrecto.
 */
@Service
@Transactional(readOnly = true)
public class LoginUseCaseImpl implements LoginUseCase {

    /** Hash dummy usado para el check de timing-attack cuando el usuario no existe. */
    private static final String DUMMY_HASH =
            "$2a$10$xNp2a3q6xRGMdamwO2eZFuTtiuRuJmUqMRnf2PeT/mv22XVman0RO";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;

    /**
     * Construye el caso de uso con sus dependencias.
     *
     * @param usuarioRepository    repositorio de usuarios.
     * @param passwordEncoder      codificador BCrypt.
     * @param tokenGeneratorPort   puerto de salida para generación de token.
     */
    public LoginUseCaseImpl(final UsuarioRepository usuarioRepository,
                            final PasswordEncoder passwordEncoder,
                            final TokenGeneratorPort tokenGeneratorPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    /**
     * {@inheritDoc}
     *
     * <p>El nombre se busca case-insensitively en el repositorio.
     * Si el usuario no existe, se realiza un check BCrypt dummy para evitar
     * timing attacks que permitan enumerar usuarios válidos.
     */
    @Override
    public LoginResult login(final String nombre, final String pin) {
        final Optional<Usuario> optUsuario = usuarioRepository.findByNombre(nombre);

        if (optUsuario.isEmpty()) {
            // Dummy check — consume tiempo similar a un check real
            passwordEncoder.matches(pin, DUMMY_HASH);
            throw new CredencialesInvalidasException();
        }

        final Usuario usuario = optUsuario.get();

        if (!usuario.activo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordEncoder.matches(pin, usuario.passwordHash())) {
            throw new CredencialesInvalidasException();
        }

        final String token = tokenGeneratorPort.generarToken(usuario.nombre(), usuario.rol());

        return new LoginResult(
                token,
                usuario.id(),
                usuario.rol().name(),
                usuario.nombre(),
                tokenGeneratorPort.getExpirationMs()
        );
    }
}
