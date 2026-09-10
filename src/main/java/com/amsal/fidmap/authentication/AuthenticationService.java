package com.amsal.fidmap.authentication;

//import com.fasterxml.jackson.databind.ObjectMapper;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.jwt.JwtService;
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


    /*
    this method may be deleted later,it is just for testing
    STRICTLY FOR OFFICIALS THAT IS THE SUPER ADMINS
     */

    public ApiResponse<UserDto> createFirstUserAndWorkspace(AddFirstUser firstUser, WorkspaceDto workspaceDto) {


        if (userRepository.existsByEmail(firstUser.getEmail())) {
            throw new UserNotFoundException("this user already exist");
        }


        User user = userMapper.toUser(firstUser);
        user.setPassword(passwordEncoder.encode(firstUser.getPassword()));
        user.setRole(OWNER);


        //Create new Workspace
        var workspaceResponse = workspaceService.createWorkspace(workspaceDto, user);

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


    public ApiResponse<AuthenticationResponse> registerSuperAdmin(RegisterRequest request) {


//        var user = User
//                .builder()
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
////                .date(OffsetDateTime.now())
//                .build();


        User checkUser = userRepository.findUserByEmail(request.getEmail());
        if (checkUser != null) {
            throw new UserNotFoundException("user with the email: " + request.getEmail() + " already exist,please check the email and try again");
        }


        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
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

        var checkUser = userRepository.findUserByEmail(request.getEmail());
        if (checkUser == null) {

            throw new UserNotFoundException("user not found");
        }


        var validTokens = tokenRepository.findAllValidTokensByUser(checkUser.getId());

//        if (!validTokens.isEmpty()) {
//            throw new RuntimeException("this user is already logged in another device,you can either log out or try again after 24 hours");
//        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findUserByEmail(request.getEmail());
        if (user == null) {

            throw new UserNotFoundException("user not found");
        }

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


