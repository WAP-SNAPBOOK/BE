package com.example.easybooking.user;


import com.example.easybooking.user.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriter userWriter;
    public SignUpResponse signUp(String providerId, SignUpRequest request){
        User user = User.createNewUser(providerId, request.getNickname());
        User savedUser = userWriter.save(user);
        return new SignUpResponse(savedUser.getNickname());
    }
}
