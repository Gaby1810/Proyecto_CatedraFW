package sv.edu.udb.service;

import sv.edu.udb.dto.request.LoginRequest;
import sv.edu.udb.dto.response.JwtResponse;
import sv.edu.udb.dto.response.UserProfileResponse;

public interface IAuthService {
    
    JwtResponse login(LoginRequest loginRequest);

    UserProfileResponse getAuthenticatedProfile();
}
