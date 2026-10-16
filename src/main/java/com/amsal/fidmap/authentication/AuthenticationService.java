package com.amsal.fidmap.authentication;

//import com.fasterxml.jackson.databind.ObjectMapper;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.config.FidmapConfig;
import com.amsal.fidmap.exception.UserAlreadyExistsException;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import com.amsal.fidmap.jwt.JwtService;
import com.amsal.fidmap.referral.ReferralService;
import com.amsal.fidmap.token.Token;
import com.amsal.fidmap.token.TokenRepository;
import com.amsal.fidmap.token.TokenType;
import com.amsal.fidmap.user.*;
import com.amsal.fidmap.workspace.WorkspaceDto;
import com.amsal.fidmap.workspace.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.amsal.fidmap.user.Role.OWNER;
import static com.amsal.fidmap.user.Role.SUPER_ADMIN;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final WorkspaceService workspaceService;
    private final ReferralService referralService;
    private final FidmapConfig fidmapConfig;

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]{1,64}@[^\\s@]{1,190}\\.[^\\s@]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private static String requireEmail(String email) {

        if (email == null || !EMAIL.matcher(email.trim()).matches() || email.trim().length() > 254) {
            throw new IllegalArgumentException("A valid email address is required");
        }

        return email.trim().toLowerCase();
    }

    private static void requirePassword(String password) {

        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > 128) {
            throw new IllegalArgumentException(
                    "Password must be between " + MIN_PASSWORD_LENGTH + " and 128 characters"
            );
        }
    }


    /*
    this method may be deleted later,it is just for testing
    STRICTLY FOR OFFICIALS THAT IS THE SUPER ADMINS
     */

    public ApiResponse<UserDto> createFirstUserAndWorkspace(AddFirstUser firstUser, WorkspaceDto workspaceDto) {


        if (firstUser == null || workspaceDto == null) {
            throw new IllegalArgumentException("Account and workspace details are required");
        }

        firstUser.setEmail(requireEmail(firstUser.getEmail()));
        requirePassword(firstUser.getPassword());

        if (firstUser.getFullName() == null || firstUser.getFullName().isBlank()) {
            throw new IllegalArgumentException("Your name is required");
        }

        if (workspaceDto.getName() == null || workspaceDto.getName().isBlank()
                || workspaceDto.getName().trim().length() > 100) {
            throw new IllegalArgumentException("A workspace name (max 100 characters) is required");
        }

        if (userRepository.existsByEmailIgnoreCase(firstUser.getEmail())) {
            throw new UserAlreadyExistsException("this user already exist");
        }


        User user = userMapper.toUser(firstUser);
        user.setPassword(passwordEncoder.encode(firstUser.getPassword()));
        user.setRole(OWNER);


        //Create new Workspace
        var workspaceResponse = workspaceService.createWorkspace(workspaceDto, user);


        /*
         * First-touch referral attribution.
         *
         * Invalid/missing referral codes do not
         * interfere with normal registration.
         */
        referralService.attributeWorkspace(
                workspaceResponse.getData().getId(),
                firstUser.getReferralCode()
        );

        userRepository.save(user);


        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);


        UserDto dto = userMapper.toUserDto(user);
        dto.setWorkspaceId(workspaceResponse.getData().getId());
        dto.setAccessToken(jwtToken);
        dto.setRefreshToken(refreshToken);


        return ApiResponse.success("new user and workspace is created successfully", dto);

    }


    /**
     * Creating a platform super admin is NEVER open to the public.
     *
     * Allowed only when (a) the caller is already a SUPER_ADMIN, or (b) no super
     * admin exists yet and the caller presents the one-time bootstrap secret
     * configured in fidmap.admin-bootstrap-token.
     */
    public ApiResponse<AuthenticationResponse> registerSuperAdmin(RegisterRequest request, String bootstrapToken) {

        UUID callerId = SecurityUtils.getCurrentUserIdOrNull();

        boolean callerIsSuperAdmin = callerId != null
                && userRepository.findById(callerId)
                .map(u -> u.getRole() == SUPER_ADMIN)
                .orElse(false);

        if (!callerIsSuperAdmin) {

            String configured = fidmapConfig.getAdminBootstrapToken();

            boolean bootstrapAllowed = configured != null
                    && !configured.isBlank()
                    && !userRepository.existsByRole(SUPER_ADMIN)
                    && bootstrapToken != null
                    && MessageDigest.isEqual(
                    configured.getBytes(StandardCharsets.UTF_8),
                    bootstrapToken.getBytes(StandardCharsets.UTF_8)
            );

            if (!bootstrapAllowed) {
                throw new AccessDeniedException("You are not allowed to create administrator accounts");
            }
        }

        String email = requireEmail(request.getEmail());
        requirePassword(request.getPassword());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(SUPER_ADMIN);

        var saveUser = userRepository.save(newUser);


        var jwtToken = jwtService.generateToken(saveUser);
        var refreshToken = jwtService.generateRefreshToken(saveUser);

        saveUserToken(saveUser, jwtToken);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

        return ApiResponse.success("new user and workspace is created successfully", response);


//        Map<String, Object> extraClaims = new HashMap<>();
//        extraClaims.put("userId", user.getId());
//        extraClaims.put("role", user.getRole());
//        extraClaims.put("businessId", user.getBusiness().getId());
//
//        var jwtToken = jwtService.generateToken(extraClaims,user);  //when
//        return AuthenticationResponse.builder()
//                .token(jwtToken)
//                .build();

    }

    //this shall remain in the project
    public ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {

        /*"either the user logged out or you can try after 24 hrs" I will implement this later in production
        which the user will either log out in order to relogging in another device
        or wait until the refresh token expires usually after 24 hours
        */

        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Same generic failure for unknown email and wrong password (no user enumeration).
        var checkUser = userRepository.findByEmailIgnoreCase(request.getEmail().trim()).orElse(null);
        if (checkUser == null) {

            throw new BadCredentialsException("Invalid email or password");
        }

//        if (!validTokens.isEmpty()) {
//            throw new RuntimeException("this user is already logged in another device,you can either log out or try again after 24 hours");
//        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        checkUser.getEmail(),
                        request.getPassword()
                )
        );

        var user = checkUser;

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
//        extraClaims.put("role", user.getRole());
//        extraClaims.put("authorities",user.getAuthorities());

        var jwtToken = jwtService.generateToken(extraClaims, user);  //when
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

        return ApiResponse.success("new user is authenticated successfully", response);

    }


    public void revokeAllUserTokens(User user) {

        var validUserToken = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserToken.isEmpty()) {
            return;
        }
        validUserToken.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserToken);
    }

    public void saveUserToken(User user, String jwtToken) {

        // I may store the refresh token later if required

        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    //todo
    //to be tested later
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUserName(refreshToken);

        if (userEmail != null) {
            var user = this.userRepository.findUserByEmail(userEmail);
            if (user == null) {

                throw new UserNotFoundException("user not found");
            }


            if (jwtService.isRefreshToken(refreshToken) && jwtService.isTokenValid(refreshToken, user)) {

                var accessToken = jwtService.generateToken(user);

                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }

    }


}


